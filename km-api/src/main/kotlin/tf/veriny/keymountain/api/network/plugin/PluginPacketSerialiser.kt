/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package tf.veriny.keymountain.api.network.plugin

import okio.Buffer

/**
 * Responsible for turning a custom plugin channel packet into a series of bytes over the wire.
 */
public interface PluginPacketSerialiser<T : PluginPacket> {
    public fun readIn(data: Buffer): T
    public fun writeOut(packet: T, data: Buffer)
}