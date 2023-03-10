/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package tf.veriny.keymountain.api.network.packets

import okio.Buffer
import tf.veriny.keymountain.api.network.ProtocolPacket
import tf.veriny.keymountain.api.network.ProtocolPacketSerialiser
import tf.veriny.keymountain.api.util.writeAngle
import tf.veriny.keymountain.api.util.writeDouble
import tf.veriny.keymountain.api.util.writeVarInt

/**
 * Sets the position of an entity on the client.
 */
public class S2CTeleportEntity(
    public val entityId: Int,
    public val x: Double,
    public val y: Double,
    public val z: Double,
    public val yaw: Float,
    public val pitch: Float,
    public val onGround: Boolean
) : ProtocolPacket {
    public companion object : ProtocolPacketSerialiser<S2CTeleportEntity> {
        public const val PACKET_ID: Int = 0x64

        override fun readIn(data: Buffer): S2CTeleportEntity {
            TODO()
        }

        override fun writeOut(packet: S2CTeleportEntity, data: Buffer) {
            data.writeVarInt(packet.entityId)
            data.writeDouble(packet.x)
            data.writeDouble(packet.y)
            data.writeDouble(packet.z)
            data.writeAngle(packet.yaw)
            data.writeAngle(packet.pitch)
            data.writeByte(if (packet.onGround) 1 else 0)
        }
    }

    override val id: Int get() = PACKET_ID
}