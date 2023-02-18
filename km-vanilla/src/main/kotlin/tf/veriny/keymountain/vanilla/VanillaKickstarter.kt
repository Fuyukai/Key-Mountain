/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package tf.veriny.keymountain.vanilla

import tf.veriny.keymountain.api.data.KeyMountainData
import tf.veriny.keymountain.api.mod.ModKickstarter

public object VanillaKickstarter : ModKickstarter<VanillaMod> {
    override fun createModKlass(data: KeyMountainData): VanillaMod {
        return VanillaMod(data)
    }
}