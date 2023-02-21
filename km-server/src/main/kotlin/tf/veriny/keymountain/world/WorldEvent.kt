package tf.veriny.keymountain.world

import tf.veriny.keymountain.api.client.ClientReference
import tf.veriny.keymountain.client.KeyMountainClient

/**
 * Hierachy of events that are reflected to other clients.
 */
public sealed interface WorldEvent

/** Sent when a player spawns. */
public class PlayerSpawnEvent(public val ref: ClientReference) : WorldEvent