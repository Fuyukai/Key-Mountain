package tf.veriny.keymountain.api.network.packets

import okio.Buffer
import tf.veriny.keymountain.api.network.ProtocolPacket
import tf.veriny.keymountain.api.network.ProtocolPacketSerialiser
import tf.veriny.keymountain.api.util.Identifier
import tf.veriny.keymountain.api.util.readMcString
import tf.veriny.keymountain.api.util.writeMcString

/**
 * A custom message sent to the client. Can be used by mods to communicate.
 */
public class S2CPluginMessage(
    public val channel: Identifier, public val data: Buffer
) : ProtocolPacket {
    public companion object : ProtocolPacketSerialiser<S2CPluginMessage> {
        public const val PACKET_ID: Int = 0x15

        override fun readIn(data: Buffer): S2CPluginMessage {
            val channel = Identifier(data.readMcString())
            val new = Buffer()
            data.read(new, data.size)
            return S2CPluginMessage(channel, data)
        }

        override fun writeOut(packet: S2CPluginMessage, data: Buffer) {
            data.writeMcString(packet.channel.full)
            data.write(packet.data, packet.data.size)
        }
    }

    override val id: Int get() = PACKET_ID
}