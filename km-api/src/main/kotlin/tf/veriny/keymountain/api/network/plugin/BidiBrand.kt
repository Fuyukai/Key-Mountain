/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package tf.veriny.keymountain.api.network.plugin

import okio.Buffer
import tf.veriny.keymountain.api.util.Identifier
import tf.veriny.keymountain.api.util.readMcString
import tf.veriny.keymountain.api.util.writeMcString

/**
 * The bi-directional brand packet.
 */
public class BidiBrand(public val brand: String) : PluginPacket {
    public companion object : PluginPacketSerialiser<BidiBrand> {
        public val ID: Identifier = Identifier("minecraft:brand")

        override fun readIn(data: Buffer): BidiBrand {
            return BidiBrand(data.readMcString())
        }

        override fun writeOut(packet: BidiBrand, data: Buffer) {
            data.writeMcString(packet.brand)
        }
    }

    override val identifier: Identifier get() = ID
}