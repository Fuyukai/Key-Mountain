package tf.veriny.keymountain.world

import tf.veriny.keymountain.api.world.block.BlockType

// TODO: we use the silly unoptimised mechanism of *long[][][] for simplicity right now but we could
//       definitely optimise this further, e.g. interval lists on columns.

/**
 * A single 16x16x16 chunk.
 */
@OptIn(ExperimentalUnsignedTypes::class)
public class Chunk {
    private val blocks = Array(16) {
        Array(16) {
            LongArray(16)
        }
    }

    public fun getBlockTypeId(x: Int, y: Int, z: Int): Int {
        val rawId = blocks[x][z][y]
        return (rawId.shr(32)).toInt()
    }

    public fun getBlockMeta(x: Int, y: Int, z: Int): UInt {
        val rawId = blocks[x][z][y]
        return rawId.toUInt()
    }

    public fun setBlock(x: Int, y: Int, z: Int, block: Int, metadata: Int) {
        val rawData = (block.toLong().shl(32)).or(metadata.toLong())
        blocks[x][z][y] = rawData
    }
}