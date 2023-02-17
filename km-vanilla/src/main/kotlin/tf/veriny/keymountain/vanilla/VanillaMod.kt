package tf.veriny.keymountain.vanilla

import org.apache.logging.log4j.LogManager
import tf.veriny.keymountain.api.data.KeyMountainData
import tf.veriny.keymountain.api.mod.ModKlass
import tf.veriny.keymountain.api.util.Identifier
import tf.veriny.keymountain.api.world.DimensionInfo
import tf.veriny.keymountain.api.world.block.EmptyBlockType
import tf.veriny.keymountain.vanilla.world.block.VanillaBlocks

public class VanillaMod(private val data: KeyMountainData) : ModKlass {
    private companion object {
        val LOGGER = LogManager.getLogger(VanillaMod::class.java)!!
    }

    /** Holds references to all of the vanilla blocks. */
    public val blockHolder: VanillaBlocks = VanillaBlocks(data)

    override fun setup() {
        LOGGER.info("Loading the vanilla mod...")
        blockHolder.register()

        val overworld = DimensionInfo(
            Identifier("minecraft:overworld"),
            0, 512
        )
        data.dimensions.register(overworld)
    }
}