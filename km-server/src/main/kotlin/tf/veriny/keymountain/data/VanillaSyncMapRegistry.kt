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