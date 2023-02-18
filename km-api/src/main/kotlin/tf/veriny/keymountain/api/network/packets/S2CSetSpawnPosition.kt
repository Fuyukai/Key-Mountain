package tf.veriny.keymountain.api.network.packets

import okio.Buffer
import tf.veriny.keymountain.api.network.ProtocolPacket
import tf.veriny.keymountain.api.network.ProtocolPacketSerialiser
import tf.veriny.keymountain.api.world.block.WorldPosition

/**
 * Tells the client where the spawn point is.
 */
public class S2CSetSpawnPosition(public val pos: WorldPosition, public val angle: Float) : ProtocolPacket {
    public companion object : ProtocolPacketSerialiser<S2CSetSpawnPosition> {
        public const val PACKET_ID: Int = 0x4C

        override fun readIn(data: Buffer): S2CSetSpawnPosition {
            val pos = data.readLong()
            val angle = Float.fromBits(data.readInt())
            return S2CSetSpawnPosition(WorldPosition(pos), angle)
        }

        override fun writeOut(packet: S2CSetSpawnPosition, data: Buffer) {
            data.writeLong(packet.pos.position)
            data.writeInt(packet.angle.toBits())
        }
    }

    override val id: Int get() = PACKET_ID
}