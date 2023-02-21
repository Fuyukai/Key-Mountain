/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package tf.veriny.keymountain.api.entity

import tf.veriny.keymountain.api.util.Vector3
import tf.veriny.keymountain.api.world.World
import java.util.concurrent.atomic.AtomicBoolean
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

    /** The world this entity is currently inside. */
    public val world: World

    /** The type of this entity. */
    public val type: EntityType<Data, Self>

    /** The unique ID across the server. Always increments upwards. */
    public val uniqueId: Int

    /** If True, then this player needs a position sync. Used in networking. */
    public val needsPositionSync: AtomicBoolean

    /** The current position of this entity. */
    public val position: Vector3

    // rotation properties, only really used by other clients...
    public var yaw: Float
    public var pitch: Float

    /**
     * Sets the position of this entity.
     */
    public fun setPosition(x: Double, y: Double, z: Double) {
        position.x = x
        position.y = y
        position.z = z
        needsPositionSync.set(true)
    }

}