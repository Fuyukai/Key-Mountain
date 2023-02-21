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

package tf.veriny.keymountain.client

import okio.Buffer
import org.apache.logging.log4j.LogManager
import tf.veriny.keymountain.KeyMountainServer
import tf.veriny.keymountain.api.IllegalPacketException
import tf.veriny.keymountain.api.client.ClientInfo
import tf.veriny.keymountain.api.client.ClientReference
import tf.veriny.keymountain.api.entity.PlayerEntity
import tf.veriny.keymountain.api.network.NetworkState
import tf.veriny.keymountain.api.network.ProtocolPacket
import tf.veriny.keymountain.api.network.packets.S2CDisconnectPlay
import tf.veriny.keymountain.api.network.packets.S2CPluginMessage
import tf.veriny.keymountain.api.network.plugin.PluginPacket
import tf.veriny.keymountain.network.ClientNetworker
import java.io.EOFException
import java.net.Socket
import java.util.*

// notes on closure
// 1) die() sends a disconnection message to the client which will (eventually) be processed by
//    the socket writer
// 2a) when the socket writer sees the disconnection message, it'll kill both the reader/writer tasks
// 2b) the socket writer has a timeout of 30s, after which it will fail without a timeout exception
// 3) when the tasks eventually finish, then the socket is closed and the server cleans up the player.
//
// cleanup is done in the finally block and never the die() method for consistency in the case of
// protocol errors.

/**
 * A single, connected client.
 */
public class KeyMountainClient(
    private val server: KeyMountainServer,
    private val socket: Socket
) : ClientReference, Runnable {
    private companion object {
        private val LOGGER = LogManager.getLogger(KeyMountainClient::class.java)
    }

    private val network = ClientNetworker(
        this,
        server.networker,
        server.data.packets,
        socket
    )

    // == ClientRef == //
    // delegates to the network handler
    override val state: NetworkState by network::state
    override val stillReceivingPackets: Boolean get() = !network.isClosing

    override var loginInfo: ClientInfo = ClientInfo()
        private set

    override var entity: PlayerEntity? = null

    override fun changeLoginInfo(uuid: UUID, username: String): ClientInfo {
        loginInfo = ClientInfo(uuid, username)
        return loginInfo
    }

    override fun transitionToState(state: NetworkState) {
        if (state == NetworkState.HANDSHAKE) {
            throw IllegalPacketException("Cannot transition to HANDSHAKE state")
        }

        if (network.state != NetworkState.HANDSHAKE && network.state != NetworkState.LOGIN) {
            throw IllegalPacketException("Existing state must be either HANDSHAKE or LOGIN")
        }

        if (state == NetworkState.PLAY && network.state != NetworkState.LOGIN) {
            throw IllegalPacketException("Cannot transition to PLAY state from non-LOGIN state")
        }

        if (state == NetworkState.PLAY) {
            network.startSendingKeepAlives()
        }

        network.state = state
    }

    override fun enqueueChunkData(chunkX: Long, chunkZ: Long) {
        if (stillReceivingPackets) {
            val entity = this.entity ?: error("This client is not in a world!")
            val world = entity.world

            network.outgoingChunks.put(Triple(world.columnSerialiser, chunkX, chunkZ))
        }
    }

    override fun enqueueProtocolPacket(packet: ProtocolPacket): Unit {
        if (stillReceivingPackets) {
            network.enqueueBasePacket(packet)
        }
    }

    override fun enqueuePluginPacket(packet: PluginPacket) {
        // auto-wrap it so that the networker writer doesn't have to care
        val buffer = Buffer()

        val serialiser = server.data.packets.getOutgoingPluginMaker<PluginPacket>(packet.identifier)
        if (serialiser == null) {
            LOGGER.error("Can't enqueue packet '{}' with no serialiser registered", packet)
            return
        }

        serialiser.writeOut(packet, buffer)
        val basePacket = S2CPluginMessage(packet.identifier, buffer)
        network.enqueueBasePacket(basePacket)
    }

    override fun die(message: String) {
        network.enqueueBasePacket(S2CDisconnectPlay("\"$message\""))
        network.isClosing = true
    }

    // == runnable == //
    override fun run(): Unit = socket.use {
        try {
            LOGGER.trace("starting fresh client listener")
            network.run()
        } catch (e: EOFException) {
            // client disconnected
            ClientNetworker.LOGGER.debug("Client disconnected")
        } catch (e: Exception) {
            if (e.cause !is EOFException) {
                LOGGER.warn("Error in client connection", e)
            } else {
                LOGGER.debug("Client disconnected")
            }
        } finally {
            server.networker.removeSubQueue(this)
            server.removePlayer(this)
            network.isClosing = true
        }
    }
}