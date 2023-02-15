package tf.veriny.keymountain.data

import tf.veriny.keymountain.api.util.cartesianProduct
import tf.veriny.keymountain.api.world.block.BlockMetadata
import tf.veriny.keymountain.api.world.block.BlockProperty
import tf.veriny.keymountain.api.world.block.BlockType

// The client-side blockstate generation can't be synched as there's no real way to serialise
// a BlockState. Also, I don't have the generation code. That being said, the mechanism can be
// inferred by looking at the generated reports JSON.
//
// The properties are permutated IN DEFINITION ORDER, with IDs being applied sequentially. IDs are
// generated separately to block IDs, but are in the same order as the block registry.
// The default property MAY NOT be the first ID.

private typealias BPair = Pair<BlockProperty<*>, Any>

/**
 * Contains mappings of metadata:blockstate protocol numbers.
 */
public class BlockStateData {
    // indexed by data[block_id][metadata_value]
    private var counter = 0
    private val data = mutableListOf<IntArray>()

    public fun reset() {
        counter = 0
        data.clear()
    }

    internal fun printStates(blockType: BlockType, id: Int) {
        val md = BlockMetadata(blockType.properties)

        val states = data[id]
        val indexed = states.withIndex().toList()
        val sorted = indexed.sortedBy { it.value }

        for ((metadata, bsId) in sorted) {
            val s = md.getMetadataString(metadata.toUInt())
            println("Prop: $s / ID: $bsId")
        }
    }

    /**
     * Generates the BlockState mappings for a single [BlockType].
     */
    @Suppress("UNCHECKED_CAST")
    internal fun generate(blockType: BlockType) {
        if (blockType.properties.isEmpty()) {
            data.add(intArrayOf(counter++))
            return
        }

        // ewwww
        // this is all just a gross amount of casts.
        val sets = mutableListOf<LinkedHashSet<BPair>>()

        for (property in blockType.properties) {
            val permutations = property.permutations()
            val set = linkedSetOf<BPair>()
            permutations.mapTo(set) { property to it }
            sets.add(set)
        }

        // actual type is like.. List<List<BPair>>... or something like that.
        val product = cartesianProduct(*sets.toTypedArray())

        val blockStateIds = IntArray(product.size) { 0 }

        for (combination in product) {
            // this is a list of (Property, Value)
            var metadata = 0U
            for (combo in combination) {
                val (prop, value) = (combo as BPair)
                metadata = blockType.setPropertyValue(metadata, prop as BlockProperty<Any>, value)
            }

            blockStateIds[metadata.toInt()] = counter++
        }

        data.add(blockStateIds)
    }
}