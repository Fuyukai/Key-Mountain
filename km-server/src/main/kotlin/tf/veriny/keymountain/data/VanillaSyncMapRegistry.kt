/*
 * This file is part of Key-Mountain Server.
 *
 * Key-Mountain Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, version 3.
 *
 * Key-Mountain Server is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Key-Mountain Server. If not, see <http://www.gnu.org/licenses/>.
 */

package tf.veriny.keymountain.data

import tf.veriny.keymountain.api.data.VanillaSynchronisableRegistry
import tf.veriny.keymountain.api.util.Identifiable
import tf.veriny.keymountain.api.util.Identifier

internal class VanillaSyncMapRegistry<E : Identifiable>(
    identifier: Identifier
) : MapRegistry<E>(identifier), VanillaSynchronisableRegistry<E> {
    override fun getAllEntries(): List<Identifiable> {
        return items.values.toList()
    }
}