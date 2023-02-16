package tf.veriny.keymountain.network

import tf.veriny.keymountain.api.client.ClientReference
import tf.veriny.keymountain.api.network.NetworkState
import tf.veriny.keymountain.api.network.ProtocolPacket

/**
 * A single incoming packet from a client.
 */
public data class IncomingPacket(
    public val lastState: NetworkState,
    public val ref: ClientReference,
    public val packet: ProtocolPacket
)

/**
 * A single outgoing packet to a client.
 */
public data class OutgoingPacket(
    public val lastState: NetworkState,
    public val packet: ProtocolPacket,
)