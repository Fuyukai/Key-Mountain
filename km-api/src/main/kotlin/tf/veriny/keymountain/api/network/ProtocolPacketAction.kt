/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package tf.veriny.keymountain.api.network

import tf.veriny.keymountain.api.client.ClientReference

/**
 * An action that is performed when a protocol packet is received.
 */
public fun interface ProtocolPacketAction<in T : ProtocolPacket> {
    public operator fun invoke(client: ClientReference, packet: T)
}