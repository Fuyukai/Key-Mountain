/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package tf.veriny.keymountain.api.network

/**
 * A packet that operates on the Minecraft protocol level. This is separate from mod packets, which
 * use mod channels.
 */
public interface ProtocolPacket {
    /** The ID for this packet. Usually statically defined. */
    public val id: Int
}