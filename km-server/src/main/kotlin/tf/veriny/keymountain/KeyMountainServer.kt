/*
 * This file is part of Key-Mountain Server.
 *
 * Key-Mountain Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, version 3.
 *
 * Key-Mountain Server is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Key-Mountain Server. If not, see <http://www.gnu.org/licenses/>.
 */

package tf.veriny.keymountain

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import jdk.net.ExtendedSocketOptions
import org.apache.logging.log4j.LogManager
import tf.veriny.keymountain.api.entity.PlayerEntity
import tf.veriny.keymountain.api.util.Identifier
import tf.veriny.keymountain.api.world.World
import tf.veriny.keymountain.client.ClientConnection
import tf.veriny.keymountain.data.Data
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

    public val networker: ServerNetworker = ServerNetworker(this)
    public val jsonMapper: ObjectMapper = ObjectMapper().registerKotlinModule()

    // todo: better
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

    /**
     * Removes a player from the current server.
     */
    public fun removePlayer(player: PlayerEntity) {
        // TODO: broadcast to other players...
        for (world in worlds) {
            world.removeEntity<PlayerEntity>(player.uniqueId)
        }
    }

    public fun run(): Unit = Executors.newVirtualThreadPerTaskExecutor().use { mainExecutor ->
        val worlds = this.worlds as MutableList<World>
        worlds.add(WorldImpl.generatedWorld(this, data.dimensions.get(Identifier("minecraft:overworld"))!!))

        Thread.currentThread().name = "KeyMountain-Server-Acceptor"

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