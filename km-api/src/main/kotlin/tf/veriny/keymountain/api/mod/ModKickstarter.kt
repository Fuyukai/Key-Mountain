package tf.veriny.keymountain.api.mod

import tf.veriny.keymountain.api.data.KeyMountainData


public interface ModKickstarter<T : ModKlass> {
    /**
     * Creates your mod class instance. This is provided the [KeyMountainData] instance that the
     * server is currently using.
     */
    public fun createModKlass(data: KeyMountainData): T
}