/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package tf.veriny.keymountain.api.world

import okio.Buffer
import tf.veriny.keymountain.api.client.ClientReference
import tf.veriny.keymountain.api.entity.Entity
import tf.veriny.keymountain.api.entity.EntityData
import tf.veriny.keymountain.api.entity.EntityType
import tf.veriny.keymountain.api.world.block.BlockType
import tf.veriny.keymountain.api.world.block.WorldPosition

/**
 * A single world instance simulating a single dimension.
 */
public interface World {
    /** The column serialiser for this world. */
    public val columnSerialiser: ChunkColumnSerialiser

    // == blocks == //
    /**
     * Gets the block type at the specified position.
     */
    public fun getBlockType(at: WorldPosition): BlockType

    /**
     * Gets the raw metadata value at the specified position.
     */
    public fun getBlockMetadata(at: WorldPosition): UInt

    /**
     * Sets the block at the specified position to be the [blockType], with the specified
     * [metadata].
     */
    public fun setBlock(at: WorldPosition, blockType: BlockType, metadata: UInt = 0U)

    // == entities == //
    /**
     * Spawns an entity into this world from the provided entity type.
     */
    public fun <D : EntityData, E : Entity<D, E>> spawnEntity(
        entityType: EntityType<D, E>, pos: WorldPosition, data: D?
    ): E

    /**
     * Gets a single entity in this world by entity ID.
     */
    public fun <E : Entity<*, E>> getEntity(id: Int): E?

    /**
     * Removes a single entity by entity ID. Returns the entity removed, or null if it did not
     * exist.
     */
    public fun <E : Entity<*, E>> removeEntity(id: Int): E?
}