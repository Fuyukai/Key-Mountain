/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package tf.veriny.keymountain.api.network.packets

import okio.Buffer
import tf.veriny.keymountain.api.network.ProtocolPacket
import tf.veriny.keymountain.api.network.ProtocolPacketSerialiser
import tf.veriny.keymountain.api.util.readMcString
import tf.veriny.keymountain.api.util.writeMcString

// TODO: support full chat messages
public class S2CDisconnectPlay(public val message: String) : ProtocolPacket {
    public companion object : ProtocolPacketSerialiser<S2CDisconnectPlay> {
        public const val PACKET_ID: Int = 0x17

        override fun readIn(data: Buffer): S2CDisconnectPlay {
            return S2CDisconnectPlay(data.readMcString())
        }

        override fun writeOut(packet: S2CDisconnectPlay, data: Buffer) {
            data.writeMcString(packet.message)
        }
    }

    override val id: Int get() = PACKET_ID
}