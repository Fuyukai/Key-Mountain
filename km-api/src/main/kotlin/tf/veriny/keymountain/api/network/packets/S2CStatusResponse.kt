/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package tf.veriny.keymountain.api.network.packets

import okio.Buffer
import tf.veriny.keymountain.api.util.readMcString
import tf.veriny.keymountain.api.util.writeMcString
import tf.veriny.keymountain.api.network.ProtocolPacket
import tf.veriny.keymountain.api.network.ProtocolPacketSerialiser

/**
 * Sent by the server in response to a [C2SStatusRequest].
 */
public class S2CStatusResponse(public val jsonString: String) : ProtocolPacket {
    public companion object : ProtocolPacketSerialiser<S2CStatusResponse> {
        public const val PACKET_ID: Int = 0x00

        override fun readIn(data: Buffer): S2CStatusResponse {
            return S2CStatusResponse(data.readMcString())
        }

        override fun writeOut(packet: S2CStatusResponse, data: Buffer) {
            data.writeMcString(packet.jsonString)
        }
    }

    override val id: Int get() = PACKET_ID
}