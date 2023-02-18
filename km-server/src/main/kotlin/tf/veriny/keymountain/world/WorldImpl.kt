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
import tf.veriny.keymountain.api.entity.Entity
import tf.veriny.keymountain.api.entity.EntityData
import tf.veriny.keymountain.api.entity.EntityType
import tf.veriny.keymountain.api.util.Identifier
import tf.veriny.keymountain.api.world.DimensionInfo
import tf.veriny.keymountain.api.world.World
import tf.veriny.keymountain.api.world.block.BlockType
import tf.veriny.keymountain.api.world.block.WorldPosition
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write
import kotlin.math.abs

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
            // create little 3x3 platform at (0, 0, 128)
            world.setBlock(WorldPosition(0, 0, 128), stone, 0)
            world.setBlock(WorldPosition(0, 1, 128), stone, 0)
            world.setBlock(WorldPosition(0, 2, 128), stone, 0)
            world.setBlock(WorldPosition(1, 0, 128), stone, 0)
            world.setBlock(WorldPosition(1, 1, 128), stone, 0)
            world.setBlock(WorldPosition(1, 2, 128), stone, 0)
            world.setBlock(WorldPosition(2, 0, 128), stone, 0)
            world.setBlock(WorldPosition(2, 1, 128), stone, 0)
            world.setBlock(WorldPosition(2, 2, 128), stone, 0)

            world.setBlock(WorldPosition(-1, 0, 128), stone, 0)
            world.setBlock(WorldPosition(-1, 1, 128), stone, 0)


            return world
        }

        private fun toChunkId(x: Long, z: Long): Long {
            return (x.shl(32)).or(z)
        }
    }

    // map of (Cx << 32) | Cz => chunk section
    // probably not the best structure, but
    private val chunks = Long2ObjectOpenHashMap<ChunkColumn>()

    private val events = LinkedBlockingMultiQueue<Unit, Unit>()

    // Read lock is acquired by any call to get block data or by the chunk data reader.
    // Write lock is acquired by any call to set block data.
    private val worldLock = ReentrantReadWriteLock()

    // mapping of entity id: entity
    private val knownEntites = Int2ObjectOpenHashMap<Entity<*, *>>()

    private fun getChunk(at: WorldPosition): Chunk? {
        val chunkX = at.x.floorDiv(16L)
        val chunkZ = at.z.floorDiv(16L)
        val pos = toChunkId(chunkX, chunkZ)

        val chunkSection = chunks.get(pos) ?: return null
        val y = (at.y - dimensionInfo.minHeight) / 16
        return chunkSection.chunks[y]
    }

    internal fun writeChunkData(chunkX: Int, chunkZ: Int): ByteString = worldLock.read {
        val buf = Buffer()
        val sectionId = toChunkId(chunkX.toLong(), chunkZ.toLong())
        val chunkSection = chunks.get(sectionId) ?: error("no such chunk: $chunkX,$chunkZ")

        chunkSection.serialiseBlockStates(server.data.blockStates, buf)
        buf.readByteString()
    }

    /**
     * Gets the block type at the specified position.
     */
    override fun getBlockType(at: WorldPosition): BlockType = worldLock.read {
        val chunk = getChunk(at) ?: error("no such chunk: $at")
        val id = chunk.getBlockTypeId(at.x.mod(16), at.y.mod(16), at.z.mod(16))
        return server.data.blocks.getThingFromId(id)
    }

    /**
     * Gets the raw metadata value at the specified position.
     */
    override fun getBlockMetadata(at: WorldPosition): UInt = worldLock.read {
        val chunk = getChunk(at) ?: error("no such chunk: $at")
        return chunk.getBlockMeta(at.x.mod(16), at.y.mod(16), at.z.mod(16))
    }

    /**
     * Sets the block at the specified position to be the [blockType], with the specified
     * [metadata].
     */
    override fun setBlock(at: WorldPosition, blockType: BlockType, metadata: Int): Unit = worldLock.write {
        val chunkX = at.x.floorDiv(16L)
        val chunkZ = at.z.floorDiv(16L)
        val chunk = getChunk(at) ?: error("no such chunk: $at")
        val blockId = server.data.blocks.getNumericId(blockType)
        chunk.setBlock(
            at.x.mod(16), at.y.mod(16), at.z.mod(16),
            blockId, metadata
        )
    }

    override fun <D : EntityData, E : Entity<D, E>> spawnEntity(
        entityType: EntityType<D, E>, pos: WorldPosition, data: D?
    ): E {
        val nextId = Entity.nextEntityId()
        val entity = worldLock.write {
            val newEntity = entityType.create(nextId, pos, data)
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
        Thread.currentThread().name = "KeyMountain-WorldSim-Overworld"

        while (true) {
            TODO()
        }
    }
}