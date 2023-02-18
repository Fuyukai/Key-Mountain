/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package tf.veriny.keymountain.api.network.packets

import okio.Buffer
import tf.veriny.keymountain.api.network.ProtocolPacket
import tf.veriny.keymountain.api.network.ProtocolPacketSerialiser

/**
 * Periodically sent to the client to ensure the connection stays alive.
 */
public class S2CKeepAlive(public val value: Long) : ProtocolPacket {
    public companion object : ProtocolPacketSerialiser<S2CKeepAlive> {
        public const val PACKET_ID: Int = 0x1F

        override fun readIn(data: Buffer): S2CKeepAlive {
            return S2CKeepAlive(data.readLong())
        }

        override fun writeOut(packet: S2CKeepAlive, data: Buffer) {
            data.writeLong(packet.value)
        }
    }

    override val id: Int get() = PACKET_ID
}