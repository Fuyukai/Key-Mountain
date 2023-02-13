package tf.veriny.keymountain.network

import tf.veriny.keymountain.api.client.ClientReference
import tf.veriny.keymountain.api.network.NetworkState
import tf.veriny.keymountain.api.network.ProtocolPacket
import tf.veriny.keymountain.client.ClientConnection

/**
 * A single incoming packet from a client.
 */
public data class ClientPacket(
    public val lastState: NetworkState,
    public val ref: ClientReference,
    public val packet: ProtocolPacket
)