package tf.veriny.keymountain.api.network.packets

import okio.Buffer
import tf.veriny.keymountain.api.network.ProtocolPacket
import tf.veriny.keymountain.api.network.ProtocolPacketSerialiser

/** Sent by the server when the client is asking for pings. */
public class S2CStatusPong(public val value: Long) : ProtocolPacket {
    public companion object : ProtocolPacketSerialiser<S2CStatusPong>  {
        public const val PACKET_ID: Int = 0x01

        override fun readIn(data: Buffer): S2CStatusPong {
            return S2CStatusPong(data.readLong())
        }

        override fun writeOut(packet: S2CStatusPong, data: Buffer) {
            data.writeLong(packet.value)
        }
    }

    override val id: Int get() = PACKET_ID
}