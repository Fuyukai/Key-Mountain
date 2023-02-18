/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package tf.veriny.keymountain.api.network.plugin

import tf.veriny.keymountain.api.client.ClientReference

/**
 * Functional interface for handling plugin channel
 */
public fun interface PluginPacketAction<T : PluginPacket> {
    public operator fun invoke(ref: ClientReference, packet: T)
}