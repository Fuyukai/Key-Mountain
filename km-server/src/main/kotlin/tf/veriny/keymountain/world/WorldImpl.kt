/*
 * This file is part of Key-Mountain Server.
 *
 * Key-Mountain Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, version 3.
 *
 * Key-Mountain Server is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Key-Mountain Server. If not, see <http://www.gnu.org/licenses/>.
 */

package tf.veriny.keymountain.world

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import jdk.incubator.concurrent.StructuredTaskScope
import org.apache.logging.log4j.LogManager
import tf.veriny.keymountain.KeyMountainServer
import tf.veriny.keymountain.api.client.ClientReference
import tf.veriny.keymountain.api.entity.Entity
import tf.veriny.keymountain.api.entity.EntityData
import tf.veriny.keymountain.api.entity.EntityType
import tf.veriny.keymountain.api.entity.PlayerEntity
import tf.veriny.keymountain.api.network.packets.S2CSpawnPlayer
import tf.veriny.keymountain.api.network.packets.S2CTeleportEntity
import tf.veriny.keymountain.api.util.Identifier
import tf.veriny.keymountain.api.world.ChunkColumnSerialiser
import tf.veriny.keymountain.api.world.DimensionInfo
import tf.veriny.keymountain.api.world.World
import tf.veriny.keymountain.api.world.block.BlockType
import tf.veriny.keymountain.api.world.block.WorldPosition
import tf.veriny.keymountain.network.ChunkColumnSerialiserImpl
import java.util.*
import java.util.concurrent.CyclicBarrier
import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write
import kotlin.time.ExperimentalTime

// This is the "core" simulation unit for Key Mountain.

/**
 * A single simulated world. A world contains a (near-infinite) amount of chunk sections.
 */
@OptIn(ExperimentalTime::class)
public class WorldImpl(
    private val server: KeyMountainServer,
    public val dimensionInfo: DimensionInfo
) : Runnable, World {
    public companion object {
        private val LOGGER = LogManager.getLogger(WorldImpl::class.java)

        public fun generatedWorld(server: KeyMountainServer, dimensionInfo: DimensionInfo): WorldImpl {
            val world = WorldImpl(server, dimensionInfo)
            LOGGER.trace("generating example world...")
            for (chunkX in -7..7) {
                for (chunkZ in -7..7) {
                    LOGGER.trace("setting chunk ({}, {})", chunkX, chunkZ)
                    val section = ChunkColumn(chunkX, chunkZ,dimensionInfo.totalHeight / 16)
                    val id = toChunkId(chunkX.toLong(), chunkZ.toLong())
                    world.chunks[id] = section
                }
            }

            val stone = server.data.blocks.get(Identifier("minecraft:stone"))!!
            for (x in 0 until 9) {
                for (z in 0 until 9) {
                    world.setBlock(WorldPosition(x, z, 128), stone)
                }
            }


            return world
        }

        private fun toChunkId(x: Long, z: Long): Long {
            return (x.shl(32)).or(z)
        }
    }

    override val columnSerialiser: ChunkColumnSerialiser = ChunkColumnSerialiserImpl(this, server.data)

    // map of (Cx << 32) | Cz => chunk section
    // probably not the best structure, but
    private val chunks = Long2ObjectOpenHashMap<ChunkColumn>()

    // TODO: consider if this should be a multiqueue?
    private val events = LinkedBlockingQueue<WorldEvent>()

    // Used to lock access to world-specific data.
    private val worldLock = ReentrantReadWriteLock()

    // connected players, used to reflect events to other players
    private val players = mutableListOf<ClientReference>()

    // mapping of entity id: entity
    private val knownEntites = Int2ObjectOpenHashMap<Entity<*, *>>()

    /**
     * The number of ticks this world has processed. Don't set this!
     */
    public val tickCounter: AtomicLong = AtomicLong()

    /**
     * Adds a new player to this world.
     */
    override fun addPlayer(ref: ClientReference): PlayerEntity = worldLock.write {
        val entity = spawnEntity(
            PlayerEntity, WorldPosition(0, 0, 132), PlayerEntity.PlayerEntityData()
        )
        ref.entity = entity
        players.add(ref)

        events.put(PlayerSpawnEvent(ref))
        return entity
    }

    override fun removePlayer(ref: ClientReference): Unit = worldLock.write {
        val it = players.listIterator()
        for (player in it) {
            if (player == ref) {
                it.remove()

                val entity = player.entity!!
                assert(entity.world == this)
                removeEntity(entity.uniqueId)
                break
            }
        }
    }

    /**
     * Gets the chunk column at ([chunkX], [chunkZ]).
     */
    public fun getChunkColumn(chunkX: Long, chunkZ: Long): ChunkColumn? {
        val pos = toChunkId(chunkX, chunkZ)
        return chunks.get(pos)
    }

    /**
     * Gets the chunk column for the block at [pos].
     */
    public fun getChunkColumn(pos: WorldPosition): ChunkColumn? {
        return getChunkColumn(pos.x.toLong().floorDiv(16), pos.z.toLong().floorDiv(16))
    }

    /**
     * Gets the block type at the specified position.
     */
    override fun getBlockType(at: WorldPosition): BlockType {
        val column = getChunkColumn(at) ?: error("no column loaded at $at")
        val id = column.getBlockId(at.x.mod(16), at.y, at.z.mod(16))
        return server.data.blocks.getThingFromId(id)
    }

    /**
     * Gets the raw metadata value at the specified position.
     */
    override fun getBlockMetadata(at: WorldPosition): UInt {
        val column = getChunkColumn(at) ?: error("no column loaded at $at")
        return column.getBlockMetadata(at.x.mod(16), at.y, at.z.mod(16))
    }

    /**
     * Sets the block at the specified position to be the [blockType], with the specified
     * [metadata].
     */
    override fun setBlock(at: WorldPosition, blockType: BlockType, metadata: UInt): Unit {
        val column = getChunkColumn(at) ?: error("no column loaded at $at")
        val blockId = server.data.blocks.getNumericId(blockType)
        column.setBlock(at.x.mod(16), at.y, at.z.mod(16), blockId, metadata)
    }

    override fun <D : EntityData, E : Entity<D, E>> spawnEntity(
        entityType: EntityType<D, E>, pos: WorldPosition, data: D?
    ): E = worldLock.write {
        val nextId = Entity.nextEntityId()
        val entity = worldLock.write {
            val newEntity = entityType.create(nextId, this, pos, data)
            knownEntites[nextId] = newEntity
            newEntity
        }

        if (entityType.shouldSendSpawnEntityPacket) {
            // TODO: send spawn packets
        }

        return entity
    }

    // todo: type safety I guess?
    override fun <E : Entity<*, E>> getEntity(id: Int): E? = worldLock.read {
        return knownEntites[id] as E?
    }

    override fun <E : Entity<*, E>> removeEntity(id: Int): E? = worldLock.write {
        return knownEntites.remove(id) as E?
    }

    // == Event dispatch == //
    private fun sendPlayerSpawnPackets(event: PlayerSpawnEvent) {
        for (player in players) {
            if (player == event.ref) continue
            LOGGER.debug("sending a spawn event for ${event.ref.loginInfo.username} to ${player.loginInfo.username}")

            val packet = S2CSpawnPlayer.from(event.ref)
            player.enqueueProtocolPacket(packet)
        }
    }

    private fun sendEntityMovePackets(event: EntityMoveEvent) {
        for (player in players) {
            // don't send players their own update packet
            if (event.entity == player.entity) continue

            val packet = S2CTeleportEntity(
                event.entity.uniqueId,
                event.entity.position.x,
                event.entity.position.y,
                event.entity.position.z,
                event.entity.yaw,
                event.entity.pitch,
                true
            )

            player.enqueueProtocolPacket(packet)
        }
    }

    // == Simulation == //
    // We have four separate methods here working in parallel.
    // 1) The event drainer, which takes events from the queue, transforms them, and reflects
    //    them back to clients.
    // 2) The ticker, which keeps a consistent tick count by waking up the cyclic barrier every
    //    50ms (or, every multiple of 50ms).
    // 3) The block entity simulator, which uses coroutines to simulate block entities.
    // 4) The regular entity simulator, which uses coroutines to simulate regular entities.
    // 5) The player entity updater, which sends position update packets for changed player positions
    //    every tick.

    // Four parties:
    // - The two simulators
    // - The ticker
    // - Player position synchroniser
    private val synchroniser = CyclicBarrier(4)

    private fun runEntitySimulator() {
        while (true) {
            synchroniser.await()
        }
    }

    private fun runBlockEntitySimulator() {
        while (true) {
            synchroniser.await()
        }
    }

    /**
     * Sends out player position packets every tick for moving players.
     */
    private fun runPlayerPositionSyncher() {
        while (true) {
            for (player in players) {
                val entity = player.entity ?: continue
                if (entity.needsPositionSync.compareAndExchange(true, false)) {
                    // don't serialise here, save valuable tick time
                    val event = EntityMoveEvent(entity)
                    events.put(event)
                }
            }

            synchroniser.await()
        }
    }

    /**
     * Called by [runTicker] every time the tick finishes.
     */
    private fun endTick() {
        // gross misuse of cyclicbarrier
        // TODO: don't spam logs

        if (synchroniser.numberWaiting != synchroniser.parties - 1) {
            // skip waking anything up, move on until both simulators are ready
            LOGGER.warn(
                "Ticker pulse reached tick ${tickCounter.get()}, but simulators didn't catch up! " +
                "Moving onto next tick..."
            )
        } else {
            tickCounter.incrementAndGet()
            synchroniser.await()
        }
    }

    // Q: Why not just run the tick code in the scheduled executor
    // A: I want the simulators to run in their own virtual thread without having to create
    //    a new one every time.
    private fun runTicker() {
        val executor = Executors.newSingleThreadScheduledExecutor()
        executor.use {
            val future = executor.scheduleAtFixedRate(
                ::endTick,
                50L,
                50L,
                TimeUnit.MILLISECONDS
            )
            future.get()
        }
    }

    /**
     * Drains incoming world events and reflects them to clients.
     */
    private fun runEventDrainer() {
        Thread.currentThread().name = "KeyMountain-WorldSimEvents-${dimensionInfo.identifier.full}"

        while (true) {
            when (val next = events.take()) {
                is PlayerSpawnEvent -> {
                    sendPlayerSpawnPackets(next)
                }
                is EntityMoveEvent -> {
                    sendEntityMovePackets(next)
                }
            }
        }
    }

    override fun run() {
        Thread.currentThread().name = "KeyMountain-WorldSim-${dimensionInfo.identifier.full}"
        LOGGER.info("Starting world simulation thread...")

        StructuredTaskScope.ShutdownOnFailure().use {
            it.fork(::runEntitySimulator)
            it.fork(::runBlockEntitySimulator)
            it.fork(::runTicker)
            it.fork(::runEventDrainer)
            it.fork(::runPlayerPositionSyncher)

            it.join()
            it.throwIfFailed()
        }
    }
}