/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package tf.veriny.keymountain.api.network.packets

import com.github.f4b6a3.uuid.UuidCreator
import okio.Buffer
import tf.veriny.keymountain.api.network.ProtocolPacket
import tf.veriny.keymountain.api.network.ProtocolPacketSerialiser
import tf.veriny.keymountain.api.util.*
import java.util.*

/**
 * Sent by the client to initialise logging in.
 */
public class C2SLoginStart(
    public val username: String,
    uuid: UUID?
) : ProtocolPacket {
    public companion object : ProtocolPacketSerialiser<C2SLoginStart> {
        private val OFFLINE_NAMESPACE = UuidCreator.getNameBasedSha1("OfflinePlayer")

        public const val PACKET_ID: Int = 0x00

        override fun readIn(data: Buffer): C2SLoginStart {
            val username = data.readMcString()
            val hasUuid = data.readBoolean()
            val uuid = if (hasUuid) {
                data.readUuid()
            } else null

            return C2SLoginStart(username, uuid)
        }

        override fun writeOut(packet: C2SLoginStart, data: Buffer) {
            data.writeMcString(packet.username)
            if (packet.hasUuid) {
                data.writeByte(1)
                data.writeUuid(packet.uuid)
            } else {
                data.writeByte(0)
            }
        }
    }

    override val id: Int get() = PACKET_ID

    private val hasUuid: Boolean = uuid == null

    // wiki.vg says:
    //
    // The official server uses UUID v3 for offline player UUIDs, with the namespace “OfflinePlayer”
    // and the value as the player’s username. This is not a requirement however, the UUID may be
    // anything.
    public val uuid: UUID = if (hasUuid) uuid!! else {
        UuidCreator.getNameBasedSha1(OFFLINE_NAMESPACE, username)
    }
}