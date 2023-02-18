package tf.veriny.keymountain.api.network.packets

import okio.Buffer
import tf.veriny.keymountain.api.network.ProtocolPacket
import tf.veriny.keymountain.api.network.ProtocolPacketSerialiser
import tf.veriny.keymountain.api.util.readVarInt
import tf.veriny.keymountain.api.util.writeVarInt

/**
 * Sent by the client to confirm a forced position update.
 */
public class C2SConfirmTeleportation(public val teleportId: Int) : ProtocolPacket {
    public companion object : ProtocolPacketSerialiser<C2SConfirmTeleportation> {
        public const val PACKET_ID: Int = 0x00

        override fun readIn(data: Buffer): C2SConfirmTeleportation {
            return C2SConfirmTeleportation(data.readVarInt())
        }

        override fun writeOut(packet: C2SConfirmTeleportation, data: Buffer) {
            data.writeVarInt(packet.teleportId)
        }
    }

    override val id: Int get() = PACKET_ID
}