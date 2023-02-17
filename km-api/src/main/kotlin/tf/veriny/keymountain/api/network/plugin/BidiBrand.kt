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

        override fun readIn(buffer: Buffer): BidiBrand {
            return BidiBrand(buffer.readMcString())
        }

        override fun writeOut(packet: BidiBrand, buffer: Buffer) {
            buffer.writeMcString(packet.brand)
        }
    }

    override val identifier: Identifier get() = ID
}