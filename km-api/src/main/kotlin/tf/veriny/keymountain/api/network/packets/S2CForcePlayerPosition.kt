package tf.veriny.keymountain.api.network.packets

import okio.Buffer
import tf.veriny.keymountain.api.network.ProtocolPacket
import tf.veriny.keymountain.api.network.ProtocolPacketSerialiser
import tf.veriny.keymountain.api.util.writeVarInt

/**
 * Sets the player's position.
 */
public class S2CForcePlayerPosition(
    public val x: Double,
    public val z: Double,
    public val y: Double,
    public val yaw: Float,
    public val pitch: Float,
    public val flags: Byte,
    public val teleportId: Int,
    public val dismountVehicle: Boolean  // ?
) : ProtocolPacket {
    public companion object : ProtocolPacketSerialiser<S2CForcePlayerPosition> {
        public const val PACKET_ID: Int = 0x38

        override fun readIn(data: Buffer): S2CForcePlayerPosition {
            TODO("Not yet implemented")
        }

        override fun writeOut(packet: S2CForcePlayerPosition, data: Buffer) {
            data.writeLong(packet.x.toBits())
            data.writeLong(packet.y.toBits())
            data.writeLong(packet.z.toBits())
            data.writeInt(packet.yaw.toBits())
            data.writeInt(packet.pitch.toBits())
            data.writeByte(packet.flags.toInt())
            data.writeVarInt(packet.teleportId)
            data.writeByte(if (packet.dismountVehicle) 1 else 0)
        }

    }

    override val id: Int get() = PACKET_ID
}