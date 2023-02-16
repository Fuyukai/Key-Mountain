package tf.veriny.keymountain.api.network.packets

import okio.Buffer
import tf.veriny.keymountain.api.network.ProtocolPacket
import tf.veriny.keymountain.api.network.ProtocolPacketSerialiser

public class C2SPong(public val data: Int) : ProtocolPacket {
    public companion object : ProtocolPacketSerialiser<C2SPong> {
        public const val PACKET_ID: Int = 0x1F

        override fun readIn(data: Buffer): C2SPong {
            return C2SPong(data.readInt())
        }

        override fun writeOut(packet: C2SPong, data: Buffer) {
            data.writeInt(packet.data)
        }
    }

    override val id: Int get() = PACKET_ID
}