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

import java.util.concurrent.atomic.AtomicLongArray

// TODO: we use the silly unoptimised mechanism of *long[][][] for simplicity right now but we could
//       definitely optimise this further, e.g. interval lists on columns.

/**
 * A single 16x16x16 chunk.
 */
@OptIn(ExperimentalUnsignedTypes::class)
public class Chunk {
    private val blocks = Array(16) {
        Array(16) {
            AtomicLongArray(16)
        }
    }

    /**
     * Creates a fresh deep copy of this chunk.
     */
    public fun copy(): Chunk {
        val next = Chunk()

        for (x in 0 until 16) {
            for (z in 0 until 16) {
                for (y in 0 until 16) {
                    next.blocks[x][z][y] = blocks[x][z][y]
                }
            }
        }

        return next
    }

    public fun get(x: Int, y: Int, z: Int): Long {
        return blocks[x][z][y]
    }

    public fun getBlockTypeId(x: Int, y: Int, z: Int): Int {
        val rawId = blocks[x][z][y]
        return (rawId.shr(32)).toInt()
    }

    public fun getBlockMeta(x: Int, y: Int, z: Int): UInt {
        val rawId = blocks[x][z][y]
        return rawId.toUInt()
    }

    public fun setBlock(x: Int, y: Int, z: Int, block: Int, metadata: UInt) {
        val rawData = (block.toLong().shl(32)).or(metadata.toLong())
        blocks[x][z][y] = rawData
    }
}