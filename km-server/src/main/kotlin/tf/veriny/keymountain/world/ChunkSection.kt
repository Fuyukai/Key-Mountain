package tf.veriny.keymountain.world

/**
 * A column of individual chunks.
 */
public class ChunkSection {
    public val chunks: Array<Chunk> = Array(32) { Chunk() }
}