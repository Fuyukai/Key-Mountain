package tf.veriny.keymountain.data

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap
import tf.veriny.keymountain.api.KeyMountainException
import tf.veriny.keymountain.api.data.RegistryWithIds
import tf.veriny.keymountain.api.util.Identifiable
import tf.veriny.keymountain.api.util.Identifier

internal open class MapRegistry<E : Identifiable>(override val identifier: Identifier) : RegistryWithIds<E> {
    protected var idCounter = 0
    protected val items = mutableMapOf<Identifier, E>()
    protected val idMapping = Object2IntArrayMap<Identifier>().also { it.defaultReturnValue(-1) }
    protected val reverseIdMapping = Int2ObjectArrayMap<Identifier>()

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
            reverseIdMapping.put(idCounter, item.identifier)
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

    override fun getThingFromId(id: Int): E {
        val identifier = reverseIdMapping.get(id)
                         ?: throw KeyMountainException("no such thing for id '$id'")

        return get(identifier)!!
    }

}