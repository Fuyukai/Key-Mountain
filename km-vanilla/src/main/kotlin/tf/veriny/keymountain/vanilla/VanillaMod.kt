package tf.veriny.keymountain.vanilla

import org.apache.logging.log4j.LogManager
import tf.veriny.keymountain.api.data.KeyMountainData
import tf.veriny.keymountain.api.mod.ModKlass

public class VanillaMod(private val data: KeyMountainData) : ModKlass {
    private companion object {
        val LOGGER = LogManager.getLogger(VanillaMod::class.java)
    }

    override fun setup() {
        LOGGER.info("Loading the vanilla mod...")

        // TODO: uh, anything?
    }
}