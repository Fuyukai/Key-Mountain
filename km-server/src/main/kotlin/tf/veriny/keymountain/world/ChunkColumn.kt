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

import okio.Buffer
import tf.veriny.keymountain.api.util.writeVarInt
import tf.veriny.keymountain.data.BlockStateData
import kotlin.math.floor

/**
 * A column of individual chunks.
 */
public class ChunkColumn(
    public val x: Int, public val z: Int,
    private val height: Int,
) {
    public val chunks: Array<Chunk> = Array(height) { Chunk() }

    /**
     * Serialises the chunk data in this section to the specified [buffer].
     */
    public fun serialiseBlockStates(
        blockStates: BlockStateData,
        buffer: Buffer,
        v: Boolean = false
    ) {
        val bitsNeededPer = blockStates.bitsPerEntry

        for (chunk in chunks) {
            var bitsUsed = 0
            var current = 0L

            // block count, lie blatantly
            buffer.writeShort(4096)

            // block states (paletted container)
            // say we use 16 bits per entry. the client seems to ignore the actual value,
            // just check if its above 9?
            buffer.writeByte(bitsNeededPer)

            // no palette data!

            // number of longs: 4096 / floor(64 / bits)
            buffer.writeVarInt(4096 / (64).floorDiv(bitsNeededPer))

            for (y in 0 until 16) {
                for (z in 0 until 16) {
                    for (x in 0 until 16) {
                        val fullId = chunk.get(x, y, z)
                        val bsId = blockStates.getBlockStateId(fullId)

                        // or on and shift along
                        val nextId = bsId.toLong().shl(bitsUsed)
                        current = current.or(nextId)
                        bitsUsed += bitsNeededPer

                        if (bitsUsed + bitsNeededPer >= 64) {
                            buffer.writeLong(current)
                            current = 0L
                            bitsUsed = 0
                        }
                    }
                }
            }

            // biomes (paletted container), single valued as we dont currently implement biomes
            // bits per entry: 0
            buffer.writeByte(0)
            // "palette", 0 for the first biome entry in the registry
            buffer.writeVarInt(0)
            // array size (0 for direct palettes)
            buffer.writeVarInt(0)
        }
    }
}