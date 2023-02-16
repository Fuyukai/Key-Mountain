package tf.veriny.keymountain.data

import it.unimi.dsi.fastutil.objects.Object2IntArrayMap
import tf.veriny.keymountain.api.KeyMountainException
import tf.veriny.keymountain.api.data.RegistryWithIds
import tf.veriny.keymountain.api.util.Identifiable
import tf.veriny.keymountain.api.util.Identifier

internal open class MapRegistry<E : Identifiable>(override val identifier: Identifier) : RegistryWithIds<E> {
    private var idCounter = 0
    private val items = mutableMapOf<Identifier, E>()
    private val idMapping = Object2IntArrayMap<Identifier>().also { it.defaultReturnValue(-1) }

    // == contract methods == //
    override val size: Int get() = items.size
    override fun iterator(): Iterator<E> = items.values.iterator()
    override fun isEmpty(): Boolean = items.isEmpty()

    override fun contains(element: E): Boolean {
        return items[element.identifier] != null
    }

    override fun containsAll(elements: Collection<E>): Boolean {
        return elements.all { contains(it) }
    }

    override fun register(item: E): E? {
        val previous = items.put(item.identifier, item)

        // only update ID if nothing was registered before to avoid gaps
        if (previous == null) {
            idMapping.put(item.identifier, idCounter++)
        }

        return previous
    }

    override fun get(id: Identifier): E? = items[id]

    override fun getNumericId(thing: E): Int {
        // stupid java duck apis
        val id = idMapping.getInt(thing.identifier)
        if (id == -1) {
            throw KeyMountainException("no ID for '${thing.identifier}'")
        }
        return id
    }

}