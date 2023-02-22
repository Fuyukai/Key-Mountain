package tf.veriny.keymountain.api.network.packets

import okio.Buffer
import tf.veriny.keymountain.api.network.ProtocolPacket
import tf.veriny.keymountain.api.network.ProtocolPacketSerialiser
import tf.veriny.keymountain.api.util.readBoolean

public class C2SSetPlayerOnGround(public val value: Boolean) : ProtocolPacket {
    public companion object : ProtocolPacketSerialiser<C2SSetPlayerOnGround> {
        public const val PACKET_ID: Int = 0x16

        override fun readIn(data: Buffer): C2SSetPlayerOnGround {
            return C2SSetPlayerOnGround(data.readBoolean())
        }

        override fun writeOut(packet: C2SSetPlayerOnGround, data: Buffer) {
            data.writeByte(if (packet.value) 1 else 0)
        }
    }

    override val id: Int get() = PACKET_ID
}