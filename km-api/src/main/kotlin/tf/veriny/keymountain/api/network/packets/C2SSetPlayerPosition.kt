package tf.veriny.keymountain.api.network.packets

import okio.Buffer
import tf.veriny.keymountain.api.network.ProtocolPacket
import tf.veriny.keymountain.api.network.ProtocolPacketSerialiser
import tf.veriny.keymountain.api.util.readBoolean

/**
 * Sent by the client to set the player position.
 */
public class C2SSetPlayerPosition(
    public val x: Double,
    public val z: Double,
    public val feetY: Double,
    public val onGround: Boolean,
) : ProtocolPacket {
    public companion object : ProtocolPacketSerialiser<C2SSetPlayerPosition> {
        public const val PACKET_ID: Int = 0x13

        public fun readIn(data: Buffer, withOnGround: Boolean = true): C2SSetPlayerPosition {
            val x = Double.fromBits(data.readLong())
            val y = Double.fromBits(data.readLong())
            val z = Double.fromBits(data.readLong())

            val onGround = if (withOnGround) {
                data.readBoolean()
            } else false

            return C2SSetPlayerPosition(x, z, y, onGround)
        }

        override fun readIn(data: Buffer): C2SSetPlayerPosition {
            return readIn(data, withOnGround = true)
        }

        public fun writeOut(packet: C2SSetPlayerPosition, data: Buffer, withOnGround: Boolean) {
            data.writeLong(packet.x.toBits())
            data.writeLong(packet.feetY.toBits())
            data.writeLong(packet.z.toBits())

            if (withOnGround) data.writeByte(if (packet.onGround) 1 else 0)
        }

        override fun writeOut(packet: C2SSetPlayerPosition, data: Buffer) {
            writeOut(packet, data, withOnGround = true)
        }
    }

    override val id: Int get() = PACKET_ID
}