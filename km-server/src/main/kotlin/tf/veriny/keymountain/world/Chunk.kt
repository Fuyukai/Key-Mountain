@file:OptIn(ExperimentalUnsignedTypes::class)

package tf.veriny.keymountain.world

// TODO: we use the silly unoptimised mechanism of *long[][][] for simplicity right now but we could
//       definitely optimise this further, e.g. interval lists on columns.

/**
 * A single 16x16x16 chunk.
 */
public class Chunk {
    private val blocks = Array(16) {
        Array(16) {
            ULongArray(16)
        }
    }
}