package tf.veriny.keymountain.api.network.plugin

import tf.veriny.keymountain.api.client.ClientReference

/**
 * Functional interface for handling plugin channel
 */
public fun interface PluginPacketAction<T : PluginPacket> {
    public operator fun invoke(ref: ClientReference, packet: T)
}