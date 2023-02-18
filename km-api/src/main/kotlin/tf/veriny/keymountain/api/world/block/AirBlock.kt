/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package tf.veriny.keymountain.api.world.block

import tf.veriny.keymountain.api.util.Identifier
import tf.veriny.keymountain.api.world.block.EmptyBlockType

/**
 * The singleton instance of the Air block.
 */
public object AirBlock : EmptyBlockType(Identifier("minecraft:air"))