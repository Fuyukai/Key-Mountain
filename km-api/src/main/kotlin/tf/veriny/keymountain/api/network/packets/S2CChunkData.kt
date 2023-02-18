package tf.veriny.keymountain.api.network.packets

import com.dyescape.dataformat.nbt.databind.NBTMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import okio.Buffer
import okio.ByteString
import tf.veriny.keymountain.api.network.ProtocolPacket
import tf.veriny.keymountain.api.network.ProtocolPacketSerialiser
import tf.veriny.keymountain.api.util.writeBitSet
import tf.veriny.keymountain.api.util.writeVarInt
import java.util.BitSet

/**
 * Contains data about a single chunk column.
 */
public class S2CChunkData(
    public val chunkX: Int,
    public val chunkZ: Int,
    public val sectionData: ByteString,
    public val chunkCount: Int,
) : ProtocolPacket {
    public companion object : ProtocolPacketSerialiser<S2CChunkData> {
        private val nbt = NBTMapper().registerKotlinModule()

        public const val PACKET_ID: Int = 0x20

        override fun readIn(data: Buffer): S2CChunkData {
            TODO("Too complicated")
        }

        override fun writeOut(packet: S2CChunkData, data: Buffer) {
            data.writeInt(packet.chunkX)
            data.writeInt(packet.chunkZ)

            // heightmaps nbt... it looks like the client simply doesn't care if we omit these.
            // so we just send an empty tag compound (a TAG_End)
            val map = object : HashMap<String, String>() {}
            map["gottem"] = "idiot!"
            data.write(nbt.writeValueAsBytes(map))

            data.writeVarInt(packet.sectionData.size)
            data.write(packet.sectionData)

            // no block entities yet
            data.writeVarInt(0)

            // trust edges: yeah?
            data.writeByte(1)

            // lighting is not implemented yet, just write out zeroes for the mask and ones for
            // the empty masks
            val bs = BitSet(packet.chunkCount + 2)
            data.writeBitSet(bs)  // sky light mask
            data.writeBitSet(bs)  // block light mask
            bs.set(0, packet.chunkCount + 2, true)
            data.writeBitSet(bs)  // empty sky light mask
            data.writeBitSet(bs)  // empty block light mask

            // sky light array count (0)
            data.writeVarInt(0)
            // block light array count (0)
            data.writeVarInt(0)
        }
    }

    override val id: Int get() = PACKET_ID
}