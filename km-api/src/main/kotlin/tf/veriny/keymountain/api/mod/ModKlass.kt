/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package tf.veriny.keymountain.api.mod

/**
 * Implemented by your mod's main class. This
 */
public interface ModKlass {
    /**
     * Called to set up this mod.
     */
    public fun setup(): Unit = Unit

    /**
     * Called after all mods have loaded to meddle with other mod data.
     */
    public fun postSetup(): Unit = Unit
}