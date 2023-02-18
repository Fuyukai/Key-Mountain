/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

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