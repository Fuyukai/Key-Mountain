package tf.veriny.keymountain.api.network.packets

import okio.Buffer
import tf.veriny.keymountain.api.network.ProtocolPacket
import tf.veriny.keymountain.api.network.ProtocolPacketSerialiser
import tf.veriny.keymountain.api.util.Identifier
import tf.veriny.keymountain.api.util.readMcString
import tf.veriny.keymountain.api.util.writeMcString

/**
 * A custom message sent to the server. Can be used by mods to communicate.
 */
public class C2SPluginMessage(
    public val channel: Identifier, public val data: Buffer
) : ProtocolPacket {
    public companion object : ProtocolPacketSerialiser<C2SPluginMessage> {
        public const val PACKET_ID: Int = 0x0C

        override fun readIn(data: Buffer): C2SPluginMessage {
            val channel = Identifier(data.readMcString())

            val new = Buffer()
            data.read(new, data.size)
            return C2SPluginMessage(channel, new)
        }

        override fun writeOut(packet: C2SPluginMessage, data: Buffer) {
            data.writeMcString(packet.channel.full)
            data.write(packet.data, packet.data.size)
        }
    }

    override val id: Int get() = PACKET_ID
}