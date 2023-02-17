package tf.veriny.keymountain.data

import tf.veriny.keymountain.api.KeyMountainException
import tf.veriny.keymountain.api.data.KeyMountainData
import tf.veriny.keymountain.api.data.RegistryWithIds
import tf.veriny.keymountain.api.data.VanillaSynchronisableRegistry
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

    override val blocks: RegistryWithIds<BlockType> = MapRegistry(Identifier("minecraft:block"))
    override val dimensions: VanillaSynchronisableRegistry<DimensionInfo> = VanillaSyncMapRegistry(Identifier("minecraft:dimension_type"))
    override val biomeNetworkData: VanillaSynchronisableRegistry<BiomeNetworkInfo> =
        VanillaSyncMapRegistry(Identifier("minecraft:worldgen/biome"))

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
    }
}