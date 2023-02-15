package tf.veriny.keymountain.vanilla

import tf.veriny.keymountain.api.data.KeyMountainData
import tf.veriny.keymountain.api.mod.ModKickstarter

public object VanillaKickstarter : ModKickstarter<VanillaMod> {
    override fun createModKlass(data: KeyMountainData): VanillaMod {
        return VanillaMod(data)
    }
}