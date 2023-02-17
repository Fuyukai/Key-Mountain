package tf.veriny.keymountain.world

/**
 * A column of individual chunks.
 */
public class ChunkSection(height: Int) {
    public val chunks: Array<Chunk> = Array(height) { Chunk() }
}