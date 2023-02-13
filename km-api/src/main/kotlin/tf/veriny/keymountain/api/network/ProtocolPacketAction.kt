package tf.veriny.keymountain.api.network

import tf.veriny.keymountain.api.client.ClientReference

/**
 * An action that is performed when a protocol packet is received.
 */
public fun interface ProtocolPacketAction<in T : ProtocolPacket> {
    public operator fun invoke(client: ClientReference, packet: T)
}