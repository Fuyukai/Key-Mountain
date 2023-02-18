/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package tf.veriny.keymountain.api.mod

import tf.veriny.keymountain.api.data.KeyMountainData


public interface ModKickstarter<T : ModKlass> {
    /**
     * Creates your mod class instance. This is provided the [KeyMountainData] instance that the
     * server is currently using.
     */
    public fun createModKlass(data: KeyMountainData): T
}