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