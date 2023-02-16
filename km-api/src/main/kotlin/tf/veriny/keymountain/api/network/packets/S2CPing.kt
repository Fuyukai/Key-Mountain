package tf.veriny.keymountain.api.network.packets

import okio.Buffer
import tf.veriny.keymountain.api.network.ProtocolPacket
import tf.veriny.keymountain.api.network.ProtocolPacketSerialiser
import tf.veriny.keymountain.api.util.readVarInt

/**
 * Pings the client.
 */
public class S2CPing(public val data: Int) : ProtocolPacket {
    public companion object : ProtocolPacketSerialiser<S2CPing> {
        public const val PACKET_ID: Int = 0x2E

        public const val SYNC_COMPLETED_PING: Int = 0x009C59D1

        override fun readIn(data: Buffer): S2CPing {
            return S2CPing(data.readInt())
        }

        override fun writeOut(packet: S2CPing, data: Buffer) {
            data.writeInt(packet.data)
        }
    }

    override val id: Int get() = PACKET_ID
}