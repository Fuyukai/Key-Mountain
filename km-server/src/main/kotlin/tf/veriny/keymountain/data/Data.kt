package tf.veriny.keymountain.data

import tf.veriny.keymountain.api.KeyMountainException
import tf.veriny.keymountain.api.data.KeyMountainData
import tf.veriny.keymountain.api.data.RegistryWithIds
import tf.veriny.keymountain.api.mod.ModKlass
import tf.veriny.keymountain.api.network.PacketRegistry
import tf.veriny.keymountain.api.network.PluginPacketRegistry
import tf.veriny.keymountain.api.util.Identifiable
import tf.veriny.keymountain.api.util.Identifier
import tf.veriny.keymountain.api.world.DimensionInfo
import tf.veriny.keymountain.api.world.biome.BiomeNetworkInfo
import tf.veriny.keymountain.api.world.block.*
import tf.veriny.keymountain.network.PacketRegistryImpl
import kotlin.reflect.KClass

/**
 * Contains references to the various data registries used by the server.
 */
public class Data : KeyMountainData {
    private val modInstances = mutableMapOf<KClass<out ModKlass>, ModKlass>()

    /** Contains networking data about block states. */
    public val blockStates: BlockStateData = BlockStateData()

    /** Registry containing all known blocks. */
    override val blocks: RegistryWithIds<BlockType> = MapRegistry(Identifier("minecraft:block"))
    override val dimensions: RegistryWithIds<DimensionInfo> = MapRegistry(Identifier("minecraft:dimension_type"))

    internal val biomeNetworkData = MapRegistry<BiomeNetworkInfo>(Identifier("minecraft:worldgen/biome"))

    override val packets: PacketRegistryImpl = PacketRegistryImpl()

    internal fun getSynchronisedRegistries(): Sequence<RegistryWithIds<*>> = sequence {
        yield(blocks)
    }

    internal fun addMod(klass: KClass<out ModKlass>, modKlass: ModKlass) {
        modInstances[klass] = modKlass
    }

    internal fun getAllMods(): Iterator<ModKlass> {
        return modInstances.values.iterator()
    }

    override fun <T : ModKlass> getModKlass(klass: KClass<T>): T {
        return modInstances[klass] as? T ?: throw KeyMountainException("no such mod: $klass")
    }

    internal fun generateBlockStates() {
        for (block in blocks) {
            blockStates.generate(block)
        }
    }

    init {
        blocks.register(AirBlock)

        // to fucking do: actual biome data.
        biomeNetworkData.register(BiomeNetworkInfo(
            Identifier("minecraft:plains"),
            precipitation = "rain",
            downfall = 0.4f,
            temperature = 0.8f,
            effects = BiomeNetworkInfo.BNIEffects(
                fogColour = 12638463,
                skyColour = 7907327,
                waterColour = 4159204,
                waterFogColour = 329011,
            )
        ))
    }
}