/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package tf.veriny.keymountain.api.network

import tf.veriny.keymountain.api.network.plugin.PluginPacket
import tf.veriny.keymountain.api.network.plugin.PluginPacketAction
import tf.veriny.keymountain.api.network.plugin.PluginPacketSerialiser
import tf.veriny.keymountain.api.util.Identifier

/**
 * Contains references to plugin channel packet serialises and actions.
 */
public interface PluginPacketRegistry {
    /**
     * Registers a single outgoing packet type with the specified [packetSerialiser].
     */
    public fun <T : PluginPacket> addOutgoingPacket(
        channel: Identifier,
        packetSerialiser: PluginPacketSerialiser<T>,
    )

    /**
     * Registers a single incoming packet type with the specified [packetSerialiser] and [action].
     */
    public fun <T : PluginPacket> addIncomingPacket(
        channel: Identifier,
        packetSerialiser: PluginPacketSerialiser<T>,
        action: PluginPacketAction<T>,
    )
}