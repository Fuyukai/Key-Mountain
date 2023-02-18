package tf.veriny.keymountain.api.data

import tf.veriny.keymountain.api.entity.EntityType
import tf.veriny.keymountain.api.mod.ModKlass
import tf.veriny.keymountain.api.network.PluginPacketRegistry
import tf.veriny.keymountain.api.world.DimensionInfo
import tf.veriny.keymountain.api.world.biome.BiomeNetworkInfo
import tf.veriny.keymountain.api.world.block.BlockType
import kotlin.reflect.KClass

/**
 * Contains references to most of the data for Key Mountain. This is passed to your Kickstarter
 * method
 */
public interface KeyMountainData {
    public val blocks: RegistryWithIds<BlockType>
    public val entityTypes: RegistryWithIds<EntityType<*, *>>
    public val dimensions: VanillaSynchronisableRegistry<DimensionInfo>
    public val biomeNetworkData: VanillaSynchronisableRegistry<BiomeNetworkInfo>

    /** The registry for plugin channel packets. */
    public val packets: PluginPacketRegistry

    /** Gets the instance of another mod's mod class. */
    public fun <T : ModKlass> getModKlass(klass: KClass<T>): T
}

public inline fun <reified T : ModKlass> KeyMountainData.getModKlass(): T {
    return getModKlass(T::class)
}