/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package tf.veriny.keymountain.api.network.packets

import okio.Buffer
import tf.veriny.keymountain.api.network.ProtocolPacket
import tf.veriny.keymountain.api.network.ProtocolPacketSerialiser

/** Sent by the client to ascertain ping. */
public class C2SStatusPing(public val value: Long) : ProtocolPacket {
    public companion object : ProtocolPacketSerialiser<C2SStatusPing> {
        public const val PACKET_ID: Int = 0x01

        override fun readIn(data: Buffer): C2SStatusPing {
            return C2SStatusPing(data.readLong())
        }

        override fun writeOut(packet: C2SStatusPing, data: Buffer) {
            data.writeLong(packet.value)
        }
    }

    override val id: Int get() = PACKET_ID
}