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
import lbmq.LinkedBlockingMultiQueue
import okio.Buffer
import okio.ByteString
import org.apache.logging.log4j.LogManager
import tf.veriny.keymountain.KeyMountainServer
import tf.veriny.keymountain.api.client.ClientReference
import tf.veriny.keymountain.api.entity.Entity
import tf.veriny.keymountain.api.entity.EntityData
import tf.veriny.keymountain.api.entity.EntityType
import tf.veriny.keymountain.api.util.Identifier
import tf.veriny.keymountain.api.world.ChunkColumnSerialiser
import tf.veriny.keymountain.api.world.DimensionInfo
import tf.veriny.keymountain.api.world.World
import tf.veriny.keymountain.api.world.block.BlockType
import tf.veriny.keymountain.api.world.block.WorldPosition
import tf.veriny.keymountain.network.ChunkColumnSerialiserImpl
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

// This is the "core" simulation unit for Key Mountain.

/**
 * A single simulated world. A world contains a (near-infinite) amount of chunk sections.
 */
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

    private val events = LinkedBlockingMultiQueue<Unit, Unit>()

    // Used to lock access to world-specific data.
    private val worldLock = ReentrantReadWriteLock()

    // mapping of entity id: entity
    private val knownEntites = Int2ObjectOpenHashMap<Entity<*, *>>()

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
    ): E {
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
    override fun <E : Entity<*, E>> getEntity(id: Int): E? {
        return knownEntites[id] as E?
    }

    override fun <E : Entity<*, E>> removeEntity(id: Int): E? {
        return knownEntites.remove(id) as E?
    }

    override fun run() {
        Thread.currentThread().name = "KeyMountain-WorldSim-${dimensionInfo.identifier.full}"

        while (true) {
            TODO()
        }
    }
}