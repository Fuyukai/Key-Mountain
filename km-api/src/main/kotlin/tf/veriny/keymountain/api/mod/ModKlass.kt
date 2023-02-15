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