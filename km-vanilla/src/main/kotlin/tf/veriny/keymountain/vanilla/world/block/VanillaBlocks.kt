package tf.veriny.keymountain.vanilla.world.block

import tf.veriny.keymountain.api.data.KeyMountainData
import tf.veriny.keymountain.api.world.block.BlockType
import tf.veriny.keymountain.api.world.block.EmptyBlockType

/** Holds references to all the vanilla block types. */
public class VanillaBlocks(private val data: KeyMountainData) {
    public val stone: BlockType = EmptyBlockType("minecraft:stone")
    public val furnace: BlockType = FurnaceBlock()

    public fun register() {
        data.blocks.register(stone)
        data.blocks.register(furnace)
    }
}