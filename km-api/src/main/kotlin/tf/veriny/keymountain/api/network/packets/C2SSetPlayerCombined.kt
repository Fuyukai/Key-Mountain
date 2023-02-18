package tf.veriny.keymountain.api.network.packets

import okio.Buffer
import tf.veriny.keymountain.api.network.ProtocolPacket
import tf.veriny.keymountain.api.network.ProtocolPacketSerialiser

/**
 * A combination of [C2SSetPlayerPosition] and [C2SSetPlayerRotation].
 */
public class C2SSetPlayerCombined(
    public val position: C2SSetPlayerPosition,
    public val rotation: C2SSetPlayerRotation,
) : ProtocolPacket {
    public companion object : ProtocolPacketSerialiser<C2SSetPlayerCombined> {
        public const val PACKET_ID: Int = 0x14

        override fun readIn(data: Buffer): C2SSetPlayerCombined {
            val position = C2SSetPlayerPosition.readIn(data, withOnGround = false)
            val rotation = C2SSetPlayerRotation.readIn(data)

            return C2SSetPlayerCombined(position, rotation)
        }

        override fun writeOut(packet: C2SSetPlayerCombined, data: Buffer) {
            C2SSetPlayerPosition.writeOut(packet.position, data, withOnGround = false)
            C2SSetPlayerRotation.writeOut(packet.rotation, data)
        }
    }

    override val id: Int get() = PACKET_ID

    // onGround in position is always false cos its tacked on at the end and rotation is on the end
    public val onGround: Boolean get() = rotation.onGround
}