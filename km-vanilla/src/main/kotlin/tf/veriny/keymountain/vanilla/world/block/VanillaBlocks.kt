/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package tf.veriny.keymountain.vanilla.world.block

import tf.veriny.keymountain.api.data.KeyMountainData
import tf.veriny.keymountain.api.util.Identifier
import tf.veriny.keymountain.api.world.block.BlockType
import tf.veriny.keymountain.api.world.block.EmptyBlockType

/** Holds references to all the vanilla block types. */
public class VanillaBlocks(private val data: KeyMountainData) {
    public val stone: BlockType = EmptyBlockType(Identifier("minecraft:stone"))
    public val furnace: BlockType = FurnaceBlock()

    public fun register() {
        data.blocks.register(stone)
        data.blocks.register(furnace)
    }
}