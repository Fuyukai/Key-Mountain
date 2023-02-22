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

import tf.veriny.keymountain.api.util.Identifier
import tf.veriny.keymountain.data.Data
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

/**
 * A column of individual chunks.
 */
public class ChunkColumn(
    public val x: Int, public val z: Int,
    private val height: Int,
    public val chunks: Array<Chunk> = Array(height) { Chunk() }
) {
    public companion object {
        public fun generateDefault(
            columnX: Int, columnZ: Int, height: Int, data: Data
        ): ChunkColumn {
            // lower chunks are full stone
            val stone = data.blocks.get(Identifier("minecraft:stone"))!!
            val stoneId = data.blocks.getNumericId(stone)

            val chunks = Array(height) {
                val c = Chunk()

                if (it < height/4) {
                    for (x in 0 until 16) {
                        for (y in 0 until 16) {
                            for (z in 0 until 16) {
                                c.setBlock(x, y, z, stoneId, 0U)
                            }
                        }
                    }
                }

                c
            }

            return ChunkColumn(columnX, columnZ, height, chunks)
        }
    }


    // this is per-column to allow the client to see a consistent update state during serialisation
    // as if a lower chunk is written, but a different client then updates it during writing another
    // chunk, the client won't see it as block updates will only be sent after a chunk has been
    // fully serialised.
    /** The read/write lock that guards access to this column's data. */
    public val lock: ReentrantReadWriteLock = ReentrantReadWriteLock()

    /**
     * Gets the full block ID + block metadata bits at (x, y, z). This is preferred if you need
     * both as it is atomic.
     */
    public fun getCompleteBlockData(x: Int, y: Int, z: Int): Long = lock.read {
        val chunk = chunks[y / 16]
        return chunk.get(x, y, z)
    }

    /**
     * Gets the ID of the block at (x, y, z).
     */
    public fun getBlockId(x: Int, y: Int, z: Int): Int = lock.read {
        val chunk = chunks[y / 16]
        return chunk.getBlockTypeId(x, y, z)
    }

    /**
     * Gets the metadata of the block at (x, y, z).
     */
    public fun getBlockMetadata(x: Int, y: Int, z: Int): UInt = lock.read {
        val chunk = chunks[y / 16]
        return chunk.getBlockMeta(x, y.mod(16), z)
    }

    /**
     * Sets the block at (x, y, z) to the ID [blockId] with the specified [metadata].
     */
    public fun setBlock(x: Int, y: Int, z: Int, blockId: Int, metadata: UInt): Unit = lock.write {
        val chunk = chunks[y / 16]
        chunk.setBlock(x, y.mod(16), z, blockId, metadata)
    }
}