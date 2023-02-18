/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package tf.veriny.keymountain.api.client

import tf.veriny.keymountain.api.entity.PlayerEntity
import tf.veriny.keymountain.api.network.NetworkState
import tf.veriny.keymountain.api.network.ProtocolPacket
import tf.veriny.keymountain.api.network.plugin.PluginPacket
import java.util.UUID

// ClientReference holds a PlayerClient, which is the in-world player client.
// whereas this is used for bookkeeping for a single connected player.

/**
 * A reference to a connected client to the Key Mountain server.
 */
public interface ClientReference {
    /** The network state this client is in. */
    public val state: NetworkState

    /** If incoming packets should still be processed. Otherwise, they will just be drained. */
    public val stillReceivingPackets: Boolean

    /** The login info that this client connected with. */
    public val loginInfo: ClientInfo

    /** The entity that represents this player. */
    public val entity: PlayerEntity?

    /**
     * Changes the login info for this client. Normally only called after launch.
     */
    public fun changeLoginInfo(uuid: UUID, username: String): ClientInfo

    /** Changes the state of this client reference to the specified network state. */
    public fun transitionToState(state: NetworkState)

    /**
     * Enqueues a single base packet for later sending to the client. This will be sent out using
     * the serialiser for the network state at the time of queueing, rather than at the time of
     * writing.
     */
    public fun enqueueProtocolPacket(packet: ProtocolPacket)

    /**
     * Enqueues a single plugin packet for later sending to the client.
     */
    public fun enqueuePluginPacket(packet: PluginPacket)

    /**
     * Closes this client's connection and removes them from the server.
     */
    public fun die(message: String = "Disconnected")
}