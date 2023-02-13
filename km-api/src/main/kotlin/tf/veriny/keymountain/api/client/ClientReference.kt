package tf.veriny.keymountain.api.client

import tf.veriny.keymountain.api.network.NetworkState
import tf.veriny.keymountain.api.network.ProtocolPacket

// ClientReference holds a PlayerClient, which is the in-world player client.
// whereas this is used for bookkeeping for a single connected player.

/**
 * A reference to a connected client to the Key Mountain server.
 */
public interface ClientReference {
    /** The network state this client is in. */
    public val state: NetworkState

    /** If incoming packets should still be processed. Otherwise, they will just be drained. */
    public val stillReceivingPackets: Boolean

    /** Changes the state of this client reference to the specified network state. */
    public fun transistionToState(state: NetworkState)

    /** Enqueues a single base packet for later sending to the client. */
    public fun enqueueBasePacket(packet: ProtocolPacket)

    /**
     * Closes this client's connection and removes them from the server.
     */
    public fun close()
}