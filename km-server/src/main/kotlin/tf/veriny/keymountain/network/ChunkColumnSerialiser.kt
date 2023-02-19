package tf.veriny.keymountain.network

import com.dyescape.dataformat.nbt.databind.NBTMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import okio.Buffer
import tf.veriny.keymountain.api.client.ClientReference
import tf.veriny.keymountain.api.util.writeBitSet
import tf.veriny.keymountain.api.util.writeVarInt
import tf.veriny.keymountain.api.world.ChunkColumnSerialiser
import tf.veriny.keymountain.data.Data
import tf.veriny.keymountain.world.WorldImpl
import java.util.*
import kotlin.concurrent.read

/**
 * Responsible for serialising a chunk section.
 */
public class ChunkColumnSerialiserImpl(
    private val world: WorldImpl,
    private val data: Data
) : ChunkColumnSerialiser {
    private val nbt = NBTMapper().registerKotlinModule()
    private data class FakeHeightmapData(val prop: String = "value")

    /**
     * Writes a single chunk column to the network.
     */
    override fun writeForNetwork(
        ref: ClientReference,
        sectionX: Long,
        sectionZ: Long,
        buffer: Buffer
    ) {
        // we can afford to be inefficient here as we run in our own virtual thread on the
        // client listener, and this op is done relatively infrequently in normal play.

        val column = world.getChunkColumn(sectionX, sectionZ)
                     ?: error("No such column at ($sectionX, $sectionZ)")

        // lock immediately to prevent anyone else from meddling
        // we also add the "client has this chunk" flag *under the lock* so that block updates will
        // be dispatched.
        column.lock.read {
            val bitsNeededPer = data.blockStates.bitsPerEntry
            // TODO: actual heightmap data
            // the client otherwise accepts completely fake nbt data
            buffer.write(nbt.writeValueAsBytes(FakeHeightmapData()))

            val chunkBuffer = Buffer()

            for (chunk in column.chunks) {
                var bitsUsed = 0
                var current = 0L

                // block count, lie blatantly
                chunkBuffer.writeShort(4096)

                // block states (paletted container)
                // say we use 16 bits per entry. the client seems to ignore the actual value,
                // just check if its above 9?
                chunkBuffer.writeByte(bitsNeededPer)

                // no palette data!

                // number of longs: 4096 / floor(64 / bits)
                chunkBuffer.writeVarInt(4096 / (64).floorDiv(bitsNeededPer))

                for (y in 0 until 16) {
                    for (z in 0 until 16) {
                        for (x in 0 until 16) {
                            val fullId = chunk.get(x, y, z)
                            val bsId = data.blockStates.getBlockStateId(fullId)

                            // or on and shift along
                            val nextId = bsId.toLong().shl(bitsUsed)
                            current = current.or(nextId)
                            bitsUsed += bitsNeededPer

                            if (bitsUsed + bitsNeededPer >= 64) {
                                chunkBuffer.writeLong(current)
                                current = 0L
                                bitsUsed = 0
                            }
                        }
                    }
                }

                // biomes (paletted container), single valued as we dont currently implement biomes
                // bits per entry: 0
                chunkBuffer.writeByte(0)
                // "palette", 0 for the first biome entry in the registry
                chunkBuffer.writeVarInt(0)
                // array size (0 for direct palettes)
                chunkBuffer.writeVarInt(0)
            }

            buffer.writeVarInt(chunkBuffer.size.toInt())
            buffer.write(chunkBuffer, chunkBuffer.size)

            // no block entities (yet)
            buffer.writeVarInt(0)

            // trust edges: yeah?
            buffer.writeByte(1)

            // lighting is not implemented yet, just write out zeroes for the mask and ones for
            // the empty masks
            val chunkCount = world.dimensionInfo.totalHeight/16
            val bs = BitSet(chunkCount + 2)
            buffer.writeBitSet(bs)  // sky light mask
            buffer.writeBitSet(bs)  // block light mask
            bs.set(0, chunkCount + 2, true)
            buffer.writeBitSet(bs)  // empty sky light mask
            buffer.writeBitSet(bs)  // empty block light mask

            // sky light array count (0)
            buffer.writeVarInt(0)
            // block light array count (0)
            buffer.writeVarInt(0)
        }
    }
}