/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package tf.veriny.keymountain.api.world.block

import it.unimi.dsi.fastutil.objects.Object2IntArrayMap
import tf.veriny.keymountain.api.KeyMountainException

// In a chunk, block data is stored compactly with a series of 64-bit integers.
// The upper 4 bytes represent the block type, e.g. 0 for air, 1 for stone, etc.
// The lower 4 bytes is the block's static state, corresponding to properties.
//
// Properties know their own length in bits, and how to turn their metadata field from bits into
// JVM-level objects. Properties are stored starting from the END of the
// metadata, with later properties being bit-shifted downwards, to allow indexing
//
// 1) An IntProperty of range 0..5, which requires 3 bits to store. This is the latter 3 bits of the
//    metadata. I will abbreviate this as F (for first).
// 2) A BooleanProperty, which only requires 1 bit to store. I will abbreviate this as S (for second).
// 3) An EnumProperty of 6 elements, which requires 3 bits to store. I will abbreviate this as T
//    (for third).
//
// The metadata for the block looks like such:
//
//  0000000000000000000000000TTTSFFF

// TODO: Replace these with proper
/**
 * Helper object for interacting with the metadata stored for a block.
 */
public class BlockMetadata(private val properties: LinkedHashSet<BlockProperty<*>>) {
    // property data offsets are read in from the right to aid simplicity.
    private val offsets = Object2IntArrayMap<BlockProperty<*>>().also { it.defaultReturnValue(-1) }

    init {
        var offsetCnt = 0
        for (property in properties) {
            offsets[property] = offsetCnt
            offsetCnt += property.bitSize()

            // should never happen, but alas.
            if (offsetCnt > 32) {
                throw KeyMountainException("Need more than 32 metadata bits to store this block type")
            }
        }
    }

    /** Gets a string describing the metadata for this block. */
    public fun getMetadataString(metadata: UInt): String {
        val builder = StringBuilder()
        for (prop in properties) {
            builder.append(prop)
            builder.append('=')

            val value = get(metadata, prop)
            builder.append(value)
            builder.append(", ")
        }

        return builder.toString()
    }

    /** The total number of bits used in the metadata for this block. */
    public val totalBitsUsed: Int
        get() = offsets.keys.sumOf { it.bitSize() }

    /** Gets the value of [property] from the specified [metadata]. */
    public fun <E, T : BlockProperty<E>> get(
        metadata: UInt, property: T
    ): E {
        val offset = offsets.getInt(property)
        if (offset == -1) throw KeyMountainException("no such property '$property'")

        val mask = ((1U).shl(property.bitSize())) - 1U
        val meta = (metadata.shr(offset)).and(mask)
        return property.fromBits(meta)
    }

    /** Sets [property] to [value] in the specified [metadata]. */
    public fun <E, T : BlockProperty<E>> set(
        metadata: UInt, property: T, value: E
    ): UInt {
        val offset = offsets.getInt(property)
        if (offset == -1) throw KeyMountainException("no such property '$property'")

        val data = property.toBits(value)
        val shifted = data.shl(offset)
        return metadata.or(shifted)
    }
}