package tf.veriny.keymountain.world

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import lbmq.LinkedBlockingMultiQueue
import tf.veriny.keymountain.api.world.block.BlockType
import tf.veriny.keymountain.api.world.block.WorldPosition
import tf.veriny.keymountain.data.Data
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read

/**
 * A single simulated world. A world contains a (near-infinite) amount of chunk sections.
 */
public class World(private val data: Data) : Runnable {
    // map of (Cx << 32) | Cz => chunk section
    // probably not the best structure, but
    private val chunks = Long2ObjectOpenHashMap<ChunkSection>()

    private val events = LinkedBlockingMultiQueue<Unit, Unit>()

    // Read lock is acquired by any call to get block data or by the chunk data reader.
    // Write lock is acquired by any call to set block data.
    private val worldLock = ReentrantReadWriteLock()

    private fun getChunk(at: WorldPosition): Chunk? {
        val chunkX = at.x / 16L
        val chunkZ = at.z / 16L
        val pos = (chunkX.shl(32)).or(chunkZ)

        val chunkSection = chunks.get(pos) ?: return null
        val y = at.y / 16
        return chunkSection.chunks[y]
    }

    /**
     * Gets the block type at the specified position.
     */
    public fun getBlockType(at: WorldPosition): BlockType = worldLock.read {
        val chunk = getChunk(at) ?: error("no such chunk: $at")
        val id = chunk.getBlockTypeId(at.x.mod(16), at.y.mod(16), at.z.mod(16))
        return data.blocks.getThingFromId(id)
    }

    /**
     * Gets the raw metadata value at the specified position.
     */
    public fun getBlockMetadata(at: WorldPosition): UInt = worldLock.read {
        val chunk = getChunk(at) ?: error("no such chunk: $at")
        return chunk.getBlockMeta(at.x.mod(16), at.y.mod(16), at.z.mod(16))
    }

    override fun run() {
        Thread.currentThread().name = "KeyMountain-WorldSim-Overworld"

        while (true) {
            TODO()
        }
    }
}