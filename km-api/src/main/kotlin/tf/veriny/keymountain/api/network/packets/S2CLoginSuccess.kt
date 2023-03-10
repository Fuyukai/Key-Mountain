/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package tf.veriny.keymountain.api.network.packets

import okio.Buffer
import tf.veriny.keymountain.api.network.ProtocolPacket
import tf.veriny.keymountain.api.network.ProtocolPacketSerialiser
import tf.veriny.keymountain.api.util.writeMcString
import tf.veriny.keymountain.api.util.writeUuid
import tf.veriny.keymountain.api.util.writeVarInt
import java.util.*

/** Sent by the server when logging in is successful. */
public class S2CLoginSuccess(
    public val userUuid: UUID,
    public val username: String,
) : ProtocolPacket {
    public companion object : ProtocolPacketSerialiser<S2CLoginSuccess> {
        public const val PACKET_ID: Int = 0x02

        override fun readIn(data: Buffer): S2CLoginSuccess {
            TODO()
        }

        override fun writeOut(packet: S2CLoginSuccess, data: Buffer) {
            data.writeUuid(packet.userUuid)
            data.writeMcString(packet.username)
            // no properties...?
            data.writeVarInt(0)
        }
    }

    override val id: Int get() = PACKET_ID
}