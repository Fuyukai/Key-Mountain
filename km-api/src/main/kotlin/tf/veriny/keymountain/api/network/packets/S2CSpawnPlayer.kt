/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package tf.veriny.keymountain.api.network.packets

import okio.Buffer
import tf.veriny.keymountain.api.client.ClientReference
import tf.veriny.keymountain.api.network.ProtocolPacket
import tf.veriny.keymountain.api.network.ProtocolPacketSerialiser
import tf.veriny.keymountain.api.util.*
import java.util.*

/**
 * Sent by the server to other players to spawn the player referred to by [entityId].
 */
public class S2CSpawnPlayer(
    public val entityId: Int,
    public val uuid: UUID,
    public val x: Double,
    public val y: Double,
    public val z: Double,
    public val yaw: Float,
    public val pitch: Float,
) : ProtocolPacket {
    public companion object : ProtocolPacketSerialiser<S2CSpawnPlayer> {
        public const val PACKET_ID: Int = 0x02

        public fun from(ref: ClientReference): S2CSpawnPlayer {
            val entity = ref.entity!!
            return S2CSpawnPlayer(
                entity.uniqueId, ref.loginInfo.uuid,
                entity.position.x, entity.position.y, entity.position.z, entity.yaw, entity.pitch
            )
        }

        override fun readIn(data: Buffer): S2CSpawnPlayer {
            val entityId = data.readVarInt()
            val uuid = data.readUuid()
            val x = data.readDouble()
            val y = data.readDouble()
            val z = data.readDouble()
            val yaw = data.readFloat()
            val pitch = data.readFloat()

            return S2CSpawnPlayer(entityId, uuid, x, y, z, yaw, pitch)
        }

        override fun writeOut(packet: S2CSpawnPlayer, data: Buffer) {
            data.writeVarInt(packet.entityId)
            data.writeUuid(packet.uuid)
            data.writeDouble(packet.x)
            data.writeDouble(packet.y)
            data.writeDouble(packet.z)
            data.writeAngle(packet.yaw)
            data.writeAngle(packet.pitch)
        }
    }

    override val id: Int get() = PACKET_ID
}