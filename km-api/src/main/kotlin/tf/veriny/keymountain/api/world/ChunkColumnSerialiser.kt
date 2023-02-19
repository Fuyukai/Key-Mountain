package tf.veriny.keymountain.api.world

import okio.Buffer
import tf.veriny.keymountain.api.client.ClientReference

/**
 * Has the ability to serialise a single chunk column both over the network and to disk.
 */
public interface ChunkColumnSerialiser {
    /**
     * Writes a chunk column out in network format.
     */
    public fun writeForNetwork(ref: ClientReference, sectionX: Long, sectionZ: Long, buffer: Buffer)
}