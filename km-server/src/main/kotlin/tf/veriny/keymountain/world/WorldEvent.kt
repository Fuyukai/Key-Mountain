/*
 * This file is part of Key-Mountain Server.
 *
 * Key-Mountain Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, version 3.
 *
 * Key-Mountain Server is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Key-Mountain Server. If not, see <http://www.gnu.org/licenses/>.
 */

package tf.veriny.keymountain.world

import tf.veriny.keymountain.api.client.ClientReference
import tf.veriny.keymountain.api.entity.Entity

/**
 * Hierarchy of events that are reflected to other clients.
 */
public sealed interface WorldEvent

/** Sent when a player spawns. */
public class PlayerSpawnEvent(public val ref: ClientReference) : WorldEvent

/**
 * Sent when a player moves.
 */
public class EntityMoveEvent(
    public val entity: Entity<*, *>,
    public val x: Double, public val y: Double, public val z: Double,
    public val yaw: Float, public val pitch: Float,
) : WorldEvent {
    public constructor(entity: Entity<*, *>) : this(
        entity, entity.position.x, entity.position.y, entity.position.z,
        entity.yaw, entity.pitch
    )
}