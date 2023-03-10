/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package tf.veriny.keymountain.api.data

import tf.veriny.keymountain.api.util.Identifiable
import tf.veriny.keymountain.api.util.Identifier

/**
 * A holder of things that can be uniquely identified. This acts as a collection over the items
 * within, but not a mutable collection (use [register]) for that.
 */
public interface Registry<E : Identifiable> : Collection<E> {
    /**
     * Registers the specified item into this registry, using the identifier it holds. Existing
     * items will be overwritten; returns the previous item, if any.
     */
    public fun register(item: E): E?

    /**
     * Gets a single item in this registry by its identifier, or null if it does not exist.
     */
    public fun get(id: Identifier): E?
}

/**
 * A registry with IDs. This can be synchronised with the client so requires its own identifier.
 */
public interface RegistryWithIds<E : Identifiable> : Registry<E>, Identifiable {
    /**
     * Gets the internal numeric ID for this thing. This is only used for networking; you have
     * no need for this method.
     */
    public fun getNumericId(thing: E): Int

    public fun getThingFromId(id: Int): E
}

/**
 * A registry that can be synchronised with the vanilla synchronisation mechanism (the sucky one).
 * All other [RegistryWithIds] instances can be synchronised with the Quilt protocol.
 */
public interface VanillaSynchronisableRegistry<E : Identifiable> : RegistryWithIds<E> {
    public fun getAllEntries(): List<Identifiable>
}