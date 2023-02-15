package tf.veriny.keymountain

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import jdk.net.ExtendedSocketOptions
import org.apache.logging.log4j.LogManager
import tf.veriny.keymountain.client.ClientConnection
import tf.veriny.keymountain.data.KeyMountainData
import tf.veriny.keymountain.network.ProtocolPacketRegistryImpl
import tf.veriny.keymountain.network.ServerNetworker
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * The primary server class. This contains references to all the helper classes and state objects,
 * and is passed around to various places.
 */
public class KeyMountainServer {
    public companion object {
        /** The protocol level that we speak. */
        public const val PROTOCOL_LEVEL: Int = 761
        /** The game version that we speak. */
        public const val GAME_VERSION: String = "1.19"

        private val LOGGER = LogManager.getLogger(KeyMountainServer::class.java)
    }

    // order of initialisation matters, registries go at the top
    public val packets: ProtocolPacketRegistryImpl = ProtocolPacketRegistryImpl()
    public val data: KeyMountainData = KeyMountainData()

    // then utilities that would otherwise need the registries
    public val networker: ServerNetworker = ServerNetworker(this)

    public val jsonMapper: ObjectMapper = ObjectMapper().registerKotlinModule()

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