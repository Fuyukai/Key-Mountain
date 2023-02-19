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
import jdk.incubator.concurrent.StructuredTaskScope
import org.apache.logging.log4j.LogManager
import tf.veriny.keymountain.api.client.ClientReference
import tf.veriny.keymountain.api.entity.PlayerEntity
import tf.veriny.keymountain.api.util.Identifier
import tf.veriny.keymountain.api.world.World
import tf.veriny.keymountain.client.ClientConnection
import tf.veriny.keymountain.data.Data
import tf.veriny.keymountain.network.ServerNetworker
import tf.veriny.keymountain.world.WorldImpl
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.util.*
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

    /** The map of players currently connected to the server. */
    public val players: MutableMap<UUID, ClientReference> = mutableMapOf()

    // todo: better
    public val worlds: List<World> = mutableListOf()

    /**
     * Removes a player from the current server.
     */
    public fun removePlayer(player: ClientReference) {
        // TODO: broadcast to other players...
        val entity = player.entity
        if (entity != null) {
            worlds.forEach { it.removeEntity<PlayerEntity>(entity.uniqueId) }
        }

        players.remove(player.loginInfo.uuid)
    }

    // server tasks
    private fun acceptNewConnections(sock: ServerSocket) = Executors.newVirtualThreadPerTaskExecutor().use {
        Thread.currentThread().name = "KeyMountain-Server-Acceptance"
        LOGGER.info("Starting accept loop!")

        while (true) {
            val next = sock.accept()
            LOGGER.debug("Accepted new connection from {}!", next.remoteSocketAddress)
            it.submit(ClientConnection(this, next))
        }
    }

    public fun runServer(): Unit = StructuredTaskScope.ShutdownOnFailure().use {
        val worlds = this.worlds as MutableList<World>
        worlds.add(WorldImpl.generatedWorld(this, data.dimensions.get(Identifier("minecraft:overworld"))!!))

        val sock = ServerSocket()
        LOGGER.info("Binding to port 25565")
        sock.bind(InetSocketAddress(25565))
        sock.reuseAddress = true

        it.fork { acceptNewConnections(sock) }
        it.fork { networker.run() }

        it.join()
        it.throwIfFailed()
    }
}