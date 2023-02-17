package tf.veriny.keymountain.api.data

import tf.veriny.keymountain.api.mod.ModKlass
import tf.veriny.keymountain.api.network.PluginPacketRegistry
import tf.veriny.keymountain.api.world.DimensionInfo
import tf.veriny.keymountain.api.world.block.BlockType
import kotlin.reflect.KClass

/**
 * Contains references to most of the data for Key Mountain. This is passed to your Kickstarter
 * method
 */
public interface KeyMountainData {
    /** The registry for blocks. */
    public val blocks: RegistryWithIds<BlockType>

    /** The registry for dimensions. */
    public val dimensions: RegistryWithIds<DimensionInfo>

    /** The registry for plugin channel packets. */
    public val packets: PluginPacketRegistry

    /** Gets the instance of another mod's mod class. */
    public fun <T : ModKlass> getModKlass(klass: KClass<T>): T
}

public inline fun <reified T : ModKlass> KeyMountainData.getModKlass(): T {
    return getModKlass(T::class)
}