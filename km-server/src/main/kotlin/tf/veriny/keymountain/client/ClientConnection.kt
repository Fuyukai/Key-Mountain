package tf.veriny.keymountain.client

import org.apache.logging.log4j.LogManager
import tf.veriny.keymountain.KeyMountainServer
import tf.veriny.keymountain.api.IllegalPacketException
import tf.veriny.keymountain.api.client.ClientReference
import tf.veriny.keymountain.api.network.NetworkState
import tf.veriny.keymountain.api.network.ProtocolPacket
import tf.veriny.keymountain.network.ClientListener
import java.io.EOFException
import java.net.Socket

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
        server.packets,
        socket
    )

    // == ClientRef == //
    // delegates to the network handler
    override val state: NetworkState by network::state
    override val stillReceivingPackets: Boolean get() = !network.isClosing

    override fun transistionToState(state: NetworkState) {
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

    override fun enqueueBasePacket(packet: ProtocolPacket): Unit =
        network.enqueueBasePacket(packet)

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