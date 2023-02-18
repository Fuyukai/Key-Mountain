/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package tf.veriny.keymountain.api.network.packets

import okio.Buffer
import tf.veriny.keymountain.api.network.ProtocolPacket
import tf.veriny.keymountain.api.network.ProtocolPacketSerialiser
import tf.veriny.keymountain.api.util.readBoolean

/**
 * Sent by the client to change where the player is looking.
 */
public class C2SSetPlayerRotation(
    public val yaw: Float,
    public val pitch: Float,
    public val onGround: Boolean,
) : ProtocolPacket {
    public companion object : ProtocolPacketSerialiser<C2SSetPlayerRotation> {
        public const val PACKET_ID: Int = 0x15

        override fun readIn(data: Buffer): C2SSetPlayerRotation {
            val yaw = Float.fromBits(data.readInt())
            val pitch = Float.fromBits(data.readInt())
            val onGround = data.readBoolean()

            return C2SSetPlayerRotation(yaw, pitch, onGround)
        }

        override fun writeOut(packet: C2SSetPlayerRotation, data: Buffer) {
            data.writeInt(packet.yaw.toBits())
            data.writeInt(packet.pitch.toBits())
            data.writeByte(if (packet.onGround) 1 else 0)
        }
    }

    override val id: Int get() = PACKET_ID
}