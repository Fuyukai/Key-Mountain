package tf.veriny.keymountain.data

import tf.veriny.keymountain.api.KeyMountainException
import tf.veriny.keymountain.api.data.KeyMountainData
import tf.veriny.keymountain.api.data.RegistryWithIds
import tf.veriny.keymountain.api.mod.ModKlass
import tf.veriny.keymountain.api.util.Identifier
import tf.veriny.keymountain.api.world.block.*
import kotlin.reflect.KClass

private class FurnaceBlock : BlockType, WithBlockMetadata by WithBlockMetadata.Properties(
    linkedSetOf(facing, lit)
) {
    companion object {
        private val facing = DirectionProperty("facing", default = Direction.NORTH, full = false)
        private val lit = BoolProperty("lit", default = false)
    }

    override val identifier: Identifier = Identifier("minecraft:furnace")
}

/**
 * Contains references to the various data registries used by the server.
 */
public class Data : KeyMountainData {
    private val modInstances = mutableMapOf<KClass<out ModKlass>, ModKlass>()

    /** Contains networking data about block states. */
    public val blockStates: BlockStateData = BlockStateData()

    /** Registry containing all known blocks. */
    public override val blocks: RegistryWithIds<BlockType> = MapRegistry()

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