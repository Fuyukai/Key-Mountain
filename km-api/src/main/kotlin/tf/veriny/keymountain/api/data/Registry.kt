package tf.veriny.keymountain.api.data

import tf.veriny.keymountain.api.util.Identifiable
import tf.veriny.keymountain.api.util.Identifier

/**
 * A holder of things that can be uniquely identified. This acts as a collection over the
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
}