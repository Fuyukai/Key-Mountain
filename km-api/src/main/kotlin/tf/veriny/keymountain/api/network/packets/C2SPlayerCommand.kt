package tf.veriny.keymountain.api.network.packets

import okio.Buffer
import tf.veriny.keymountain.api.network.ProtocolPacket
import tf.veriny.keymountain.api.network.ProtocolPacketSerialiser
import tf.veriny.keymountain.api.util.readVarInt
import tf.veriny.keymountain.api.util.writeVarInt

/**
 * Sent by the client when performing a small handful of actions, mostly relating to movement.
 */
public class C2SPlayerCommand(
    public val entityId: Int, public val action: PlayerCommand,
) : ProtocolPacket {
    public enum class PlayerCommand {
        START_SNEAKING,
        STOP_SNEAKING,
        LEAVE_BED,
        START_SPRINTING,
        STOP_SPRINTING,
        START_JUMP_WITH_HORSE,
        STOP_JUMP_WITH_HORSE,
        OPEN_HORSE_INVENTORY,
        START_FLYING_WITH_ELYTRA,
        ;
    }

    public companion object : ProtocolPacketSerialiser<C2SPlayerCommand> {
        public const val PACKET_ID: Int = 0x1D

        override fun readIn(data: Buffer): C2SPlayerCommand {
            val entityId = data.readVarInt()
            val actionId = data.readVarInt()
            val action = PlayerCommand.values()[actionId]

            // discard jump boost
            data.readVarInt()


            return C2SPlayerCommand(entityId, action)
        }

        override fun writeOut(packet: C2SPlayerCommand, data: Buffer) {
            data.writeVarInt(packet.entityId)
            data.writeVarInt(packet.action.ordinal)
            data.writeVarInt(0)
        }
    }

    override val id: Int get() = PACKET_ID
}