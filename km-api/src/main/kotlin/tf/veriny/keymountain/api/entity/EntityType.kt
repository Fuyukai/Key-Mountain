/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package tf.veriny.keymountain.api.entity

import tf.veriny.keymountain.api.util.Identifiable
import tf.veriny.keymountain.api.world.World
import tf.veriny.keymountain.api.world.block.WorldPosition

/**
 * Represents the type of an entity, as well as a way to construct a new entity of a type.
 */
public interface EntityType<D : EntityData, T : Entity<D, T>> : Identifiable {
    /**
     * If true, then this will send a S2CEntitySpawn packet when creating this entity. Otherwise,
     * it is expected that implementors will send their own packet in [create].
     */
    public val shouldSendSpawnEntityPacket: Boolean get() = true

    /**
     * Creates a new entity with id [entityId] into the world, at the position [pos] and with the
     * state [data]. If this method is called, it is guaranteed that the entity will be added
     * to the world.
     */
    public fun create(entityId: Int, into: World, pos: WorldPosition, data: D?): T
}