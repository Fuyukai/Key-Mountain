package tf.veriny.keymountain.api.network.plugin

import okio.Buffer

/**
 * Responsible for turning a custom plugin channel packet into a series of bytes over the wire.
 */
public interface PluginPacketSerialiser<T : PluginPacket> {
    public fun readIn(buffer: Buffer): T
    public fun writeOut(packet: T, buffer: Buffer)
}