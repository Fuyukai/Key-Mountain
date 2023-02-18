package tf.veriny.keymountain.api.world

import tf.veriny.keymountain.api.entity.Entity
import tf.veriny.keymountain.api.entity.EntityData
import tf.veriny.keymountain.api.entity.EntityType
import tf.veriny.keymountain.api.world.block.BlockType
import tf.veriny.keymountain.api.world.block.WorldPosition

/**
 * A single world instance simulating a single dimension.
 */
public interface World {
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
    public fun setBlock(at: WorldPosition, blockType: BlockType, metadata: Int = 0)

    /**
     * Spawns an entity into this world from the provided entity type.
     */
    public fun <D : EntityData, E : Entity<D, E>> spawnEntity(
        entityType: EntityType<D, E>, pos: WorldPosition, data: D?
    ): E
}