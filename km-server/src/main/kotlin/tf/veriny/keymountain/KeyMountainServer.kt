package tf.veriny.keymountain

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import jdk.net.ExtendedSocketOptions
import org.apache.logging.log4j.LogManager
import tf.veriny.keymountain.api.util.Identifier
import tf.veriny.keymountain.api.world.World
import tf.veriny.keymountain.client.ClientConnection
import tf.veriny.keymountain.data.Data
import tf.veriny.keymountain.network.PacketRegistryImpl
import tf.veriny.keymountain.network.ServerNetworker
import tf.veriny.keymountain.world.WorldImpl
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * The primary server class. This contains references to all the helper classes and state objects,
 * and is passed around to various places.
 */
public class KeyMountainServer(public val data: Data) {
    public companion object {
        /** The protocol level that we speak. */
        public const val PROTOCOL_LEVEL: Int = 761
        /** The game version that we speak. */
        public const val GAME_VERSION: String = "1.19"

        private val LOGGER = LogManager.getLogger(KeyMountainServer::class.java)
    }

    // then utilities that would otherwise need the registries
    public val networker: ServerNetworker = ServerNetworker(this)

    public val jsonMapper: ObjectMapper = ObjectMapper().registerKotlinModule()

    public val worlds: List<World> = mutableListOf()

    private fun acceptAndDispatch(sock: ServerSocket, executor: ExecutorService) {
        val next = sock.accept()
        LOGGER.trace(
            "Accepted connection from {} on port {}!",
            next.inetAddress, next.port
        )
        val client = ClientConnection(this, next)
        executor.execute(client)
    }

    public fun run(): Unit = Executors.newVirtualThreadPerTaskExecutor().use { mainExecutor ->
        Thread.currentThread().name = "KeyMountain-Server-Acceptor"
        val worlds = this.worlds as MutableList<World>
        worlds.add(WorldImpl.generatedWorld(this, data.dimensions.get(Identifier("minecraft:overworld"))!!))

        LOGGER.info("Starting KeyMountain server!")

        // start the various simulators
        mainExecutor.submit(networker)

        val sock = ServerSocket()
        LOGGER.info("Binding to port 25565")
        sock.bind(InetSocketAddress(25565))
        sock.reuseAddress = true
        sock.setOption(ExtendedSocketOptions.TCP_QUICKACK, true)

        Executors.newThreadPerTaskExecutor(
            Thread.ofVirtual().name("KeyMountain-Client-", 0L).factory()
        ).use { clientExecutor ->
            while (true) {
                try {
                    acceptAndDispatch(sock, clientExecutor)
                } catch (e: Exception) {
                    LOGGER.error("Error while accepting client connection!", e)
                }
            }
        }
    }
}