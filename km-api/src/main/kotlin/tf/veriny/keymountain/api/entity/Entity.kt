package tf.veriny.keymountain.api.entity

import tf.veriny.keymountain.api.util.Vector3
import tf.veriny.keymountain.api.world.block.WorldPosition
import java.util.concurrent.atomic.AtomicInteger

/**
 * A single entity inside a single world.
 */
public interface Entity<Data : EntityData, Self : Entity<Data, Self>> {
    public companion object {
        private val entityCounter = AtomicInteger()

        /**
         * Gets the next global unique entity ID.
         */
        public fun nextEntityId(): Int {
            return entityCounter.getAndIncrement()
        }
    }

    /** The type of this entity. */
    public val type: EntityType<Data, Self>

    /** The unique ID across the server. Always increments upwards. */
    public val uniqueId: Int

    // todo: movement stuff
    // TODO: can we pack this into two longs? WorldPosition + offset, stored in three 16-bit floats
    //  would save 4 bytes per entity over 3 doubles
    public val position: Vector3
}