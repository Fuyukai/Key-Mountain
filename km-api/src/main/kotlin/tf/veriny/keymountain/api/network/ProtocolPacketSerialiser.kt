/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package tf.veriny.keymountain.api.network

import okio.Buffer

/**
 * Used internally during networking to produce packets over the wire.
 */
public interface ProtocolPacketSerialiser<T : ProtocolPacket> {
    /**
     * Produces a new packet from the wire protocol.
     */
    public fun readIn(data: Buffer): T

    /**
     * Writes a packet out to the write protocol.
     */
    public fun writeOut(packet: T, data: Buffer)
}