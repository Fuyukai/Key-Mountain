package tf.veriny.keymountain.api.network.packets

import okio.Buffer
import okio.ByteString
import okio.Sink
import okio.Source
import tf.veriny.keymountain.api.network.ProtocolPacket
import tf.veriny.keymountain.api.network.ProtocolPacketSerialiser

/**
 * Contains data about a single chunk column.
 */
public class S2CChunkData(
    public val chunkX: Long,
    public val chunkZ: Long,
    public val actualChunkData: Buffer,
) : ProtocolPacket {
    public companion object : ProtocolPacketSerialiser<S2CChunkData> {
        public const val PACKET_ID: Int = 0x20

        override fun readIn(data: Buffer): S2CChunkData {
            val chunkX = data.readInt().toLong()
            val chunkY = data.readInt().toLong()
            val rest = data.readByteString()
            return S2CChunkData(chunkX, chunkY, Buffer().also { it.write(rest) })
        }

        override fun writeOut(packet: S2CChunkData, data: Buffer) {
            data.writeInt(packet.chunkX.toInt())
            data.writeInt(packet.chunkZ.toInt())
            data.write(packet.actualChunkData, packet.actualChunkData.size)
        }
    }

    override val id: Int get() = PACKET_ID
}