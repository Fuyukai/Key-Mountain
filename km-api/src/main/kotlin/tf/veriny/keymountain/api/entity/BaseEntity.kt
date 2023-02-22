/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package tf.veriny.keymountain.api.entity

import tf.veriny.keymountain.api.util.Vector3
import tf.veriny.keymountain.api.world.World
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Helper entity class that can be directly inherited from
 */
public abstract class BaseEntity<Data : EntityData, Self : Entity<Data, Self>>(
    override val type: EntityType<Data, Self>,
    override val uniqueId: Int,
    override val world: World,
) : Entity<Data, Self> {
    // default fields
    override val needsPositionSync: AtomicBoolean = AtomicBoolean(false)
    override val position: Vector3 = Vector3(0.0, 0.0, 0.0)
    override var pitch: Float = 0.0f
    override var yaw: Float = 0.0f
    override var isOnGround: Boolean = false

}