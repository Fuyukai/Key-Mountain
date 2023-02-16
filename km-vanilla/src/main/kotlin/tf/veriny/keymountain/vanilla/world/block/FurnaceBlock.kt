package tf.veriny.keymountain.vanilla.world.block

import tf.veriny.keymountain.api.util.Identifier
import tf.veriny.keymountain.api.world.block.*

public class FurnaceBlock(

) : BlockType, WithBlockMetadata by WithBlockMetadata.Properties(linkedSetOf(FACING, LIT))  {
    public companion object {
        public val FACING: BlockProperty<Direction> = DirectionProperty("FACING", default = Direction.NORTH, full = false)
        public val LIT: BoolProperty = BoolProperty("LIT", default = false)
    }

    override val identifier: Identifier = Identifier("minecraft:furnace")
}