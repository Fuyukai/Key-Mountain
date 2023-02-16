package tf.veriny.keymountain.client

import okio.Buffer
import org.apache.logging.log4j.LogManager
import tf.veriny.keymountain.KeyMountainServer
import tf.veriny.keymountain.api.IllegalPacketException
import tf.veriny.keymountain.api.client.ClientInfo
import tf.veriny.keymountain.api.client.ClientReference
import tf.veriny.keymountain.api.network.NetworkState
import tf.veriny.keymountain.api.network.ProtocolPacket
import tf.veriny.keymountain.api.network.packets.S2CPluginMessage
import tf.veriny.keymountain.api.network.plugin.PluginPacket
import tf.veriny.keymountain.network.ClientListener
import java.io.EOFException
import java.net.Socket
import java.util.*

/**
 * A single, connected client.
 */
public class ClientConnection(
    private val server: KeyMountainServer,
    private val socket: Socket
) : ClientReference, Runnable {
    private companion object {
        private val LOGGER = LogManager.getLogger(ClientConnection::class.java)
    }

    private val network = ClientListener(
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

        network.state = state
    }

    override fun enqueueProtocolPacket(packet: ProtocolPacket): Unit =
        network.enqueueBasePacket(packet)

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

    override fun close() {
        socket.close()
    }

    // == runnable == //
    override fun run() {
        try {
            LOGGER.trace("starting fresh client listener")
            network.run()
        } catch (e: EOFException) {
            // client disconnected
            ClientListener.LOGGER.debug("Client disconnected")
        } catch (e: Exception) {
            if (e.cause !is EOFException) {
                LOGGER.warn("Error in client connection", e)
            } else {
                LOGGER.debug("Client disconnected")
            }
        } finally {
            server.networker.removeSubQueue(this)
        }
    }
}