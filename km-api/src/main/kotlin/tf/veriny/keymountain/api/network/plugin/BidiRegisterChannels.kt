package tf.veriny.keymountain.api.network.plugin

import okio.Buffer
import tf.veriny.keymountain.api.util.Identifier

/**
 * Sent by either side to start registering channels.
 */
public class BidiRegisterChannels(public val channels: List<Identifier>) : PluginPacket {
    public companion object : PluginPacketSerialiser<BidiRegisterChannels> {
        public val ID: Identifier = Identifier("minecraft:register")

        override fun readIn(data: Buffer): BidiRegisterChannels {
            val channels = mutableListOf<Identifier>()
            val buf = StringBuilder()
            while (data.size > 0) {
                val next = data.readByte()
                if (next == (0).toByte()) {
                    channels.add(Identifier(buf.toString()))
                    buf.clear()
                } else {
                    buf.append(next.toInt().toChar())
                }
            }

            return BidiRegisterChannels(channels)
        }

        override fun writeOut(packet: BidiRegisterChannels, data: Buffer) {
            // c-string format for some reason...?
            for (channel in packet.channels) {
                data.writeUtf8(channel.full)
                data.writeByte(0x0)
            }
        }
    }

    override val identifier: Identifier get() = ID
}