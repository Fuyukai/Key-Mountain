import tf.veriny.keymountain.KeyMountainMakeUp
import tf.veriny.keymountain.vanilla.VanillaKickstarter

public object MakeUp {
    @JvmStatic public fun main(args: Array<String>) {
        val server = KeyMountainMakeUp()
        server.addMod(VanillaKickstarter)

        server.start()
    }
}