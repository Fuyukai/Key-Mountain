package tf.veriny.keymountain.api.network.packets

import okio.Buffer
import tf.veriny.keymountain.api.network.ProtocolPacket
import tf.veriny.keymountain.api.network.ProtocolPacketSerialiser

/**
 * Sent by the client when it starts or stops flying.
 */
public class C2SPlayerAbilities(public val bits: Byte) : ProtocolPacket {
    public companion object : ProtocolPacketSerialiser<C2SPlayerAbilities> {
        public const val PACKET_ID: Int = 0x1B

        override fun readIn(data: Buffer): C2SPlayerAbilities {
            return C2SPlayerAbilities(data.readByte())
        }

        override fun writeOut(packet: C2SPlayerAbilities, data: Buffer) {
            data.writeByte(packet.bits.toInt())
        }
    }

    override val id: Int get() = PACKET_ID
}