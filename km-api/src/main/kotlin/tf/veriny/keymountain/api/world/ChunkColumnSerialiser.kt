/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package tf.veriny.keymountain.api.world

import okio.Buffer
import tf.veriny.keymountain.api.client.ClientReference

/**
 * Has the ability to serialise a single chunk column both over the network and to disk.
 */
public interface ChunkColumnSerialiser {
    /**
     * Writes a chunk column out in network format.
     */
    public fun writeForNetwork(ref: ClientReference, sectionX: Long, sectionZ: Long, buffer: Buffer)
}