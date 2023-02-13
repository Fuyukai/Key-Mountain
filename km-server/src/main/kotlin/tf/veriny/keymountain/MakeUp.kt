package tf.veriny.keymountain

public object MakeUp {
    @JvmStatic
    public fun main(args: Array<String>) {
        val server = KeyMountainServer()
        server.run()
    }
}