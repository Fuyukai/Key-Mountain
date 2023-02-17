package tf.veriny.keymountain.api.network.packets

import okio.Buffer
import tf.veriny.keymountain.api.network.ProtocolPacket
import tf.veriny.keymountain.api.network.ProtocolPacketSerialiser
import tf.veriny.keymountain.api.util.readBoolean
import tf.veriny.keymountain.api.util.readMcString
import tf.veriny.keymountain.api.util.readVarInt

/**
 * Sent by the client to inform us of its settings (thanks...?)
 */
public data class C2SClientInformation(
    public val localeId: String,
    public val viewDistance: Int,
    public val chatMode: Int,
    public val colours: Boolean,
    public val skinBitSet: Byte,
    public val mainHand: Int,
    public val enableTextFiltering: Boolean,
    public val allowServerListings: Boolean,
) : ProtocolPacket {
    public companion object : ProtocolPacketSerialiser<C2SClientInformation> {
        public const val PACKET_ID: Int = 0x07

        override fun readIn(data: Buffer): C2SClientInformation {
            val localeId = data.readMcString()
            val viewDistance = data.readByte().toInt()
            val chatMode = data.readVarInt()
            val colours = data.readBoolean()
            val skinBitSet = data.readByte()
            val mainHand = data.readVarInt()
            val enableTextFiltering = data.readBoolean()
            val allowServerListings = data.readBoolean()

            return C2SClientInformation(
                localeId,
                viewDistance,
                chatMode,
                colours,
                skinBitSet,
                mainHand,
                enableTextFiltering,
                allowServerListings,
            )
        }

        override fun writeOut(packet: C2SClientInformation, data: Buffer) {
            TODO("Not yet implemented")
        }
    }

    override val id: Int get() = PACKET_ID
}