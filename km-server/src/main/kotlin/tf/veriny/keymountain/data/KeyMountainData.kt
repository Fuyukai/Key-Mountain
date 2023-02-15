package tf.veriny.keymountain.data

import tf.veriny.keymountain.api.data.RegistryWithIds
import tf.veriny.keymountain.api.util.Identifier
import tf.veriny.keymountain.api.world.block.*

private class FurnaceBlock : BlockType, WithBlockMetadata by WithBlockMetadata.Properties(
    linkedSetOf(facing, lit)
) {
    companion object {
        private val facing = DirectionProperty("facing", default = Direction.NORTH, full = false)
        private val lit = BoolProperty("lit", default = false)
    }

    override val identifier: Identifier = Identifier("minecraft:furnace")
}

/**
 * Contains references to the various data registries used by the server.
 */
public class KeyMountainData {
    /** Contains networking data about block states. */
    public val blockStates: BlockStateData = BlockStateData()

    /** Registry containing all known blocks. */
    public val blocks: RegistryWithIds<BlockType> = MapRegistry()

    internal fun generateBlockStates() {
        for (block in blocks) {
            blockStates.generate(block)
        }
    }

    init {
        blocks.register(AirBlock)
    }
}