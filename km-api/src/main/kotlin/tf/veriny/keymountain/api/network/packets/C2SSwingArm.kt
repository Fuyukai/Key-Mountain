/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package tf.veriny.keymountain.api.network.packets

import okio.Buffer
import tf.veriny.keymountain.api.network.ProtocolPacket
import tf.veriny.keymountain.api.network.ProtocolPacketSerialiser
import tf.veriny.keymountain.api.util.readVarInt
import tf.veriny.keymountain.api.util.writeVarInt

/**
 * Sent by the client when the player swings an arm.
 */
public class C2SSwingArm(public val armId: Int) : ProtocolPacket {
    public companion object : ProtocolPacketSerialiser<C2SSwingArm> {
        public const val PACKET_ID: Int = 0x2F

        override fun readIn(data: Buffer): C2SSwingArm {
            return C2SSwingArm(data.readVarInt())
        }

        override fun writeOut(packet: C2SSwingArm, data: Buffer) {
            data.writeVarInt(packet.armId)
        }
    }

    override val id: Int get() = PACKET_ID
}