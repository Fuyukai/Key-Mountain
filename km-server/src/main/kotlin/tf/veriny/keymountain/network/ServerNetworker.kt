package tf.veriny.keymountain.network

import lbmq.LinkedBlockingMultiQueue
import lbmq.Offerable
import org.apache.logging.log4j.LogManager
import tf.veriny.keymountain.KeyMountainServer
import tf.veriny.keymountain.api.client.ClientReference
import tf.veriny.keymountain.api.network.NetworkState.*
import tf.veriny.keymountain.api.network.ProtocolPacket
import tf.veriny.keymountain.api.network.packets.*

/**
 * Routes incoming packets from clients and updates the server state appropriately.
 */
public class ServerNetworker(private val server: KeyMountainServer) : Runnable {
    private companion object {
        private val LOGGER = LogManager.getLogger(ServerNetworker::class.java)
    }

    private val incoming = LinkedBlockingMultiQueue<ClientReference, ClientPacket>()

    /** Adds a new client sub-queue. */
    internal fun getSubQueue(ref: ClientReference): Offerable<ClientPacket> {
        incoming.addSubQueue(ref, 0)
        return incoming.getSubQueue(ref)
    }

    /** Removes a client sub-queue. Used when a client is disconnecting. */
    internal fun removeSubQueue(ref: ClientReference) {
        // thread-safe as LBMQ releases the lock via conditions
        LOGGER.trace("removing subqueue for $ref")
        incoming.removeSubQueue(ref)
    }

    // == Handshake == //
    private fun handleHandshakePacket(ref: ClientReference, packet: C2SHandshake) {
        LOGGER.trace(
            "client wants to connect to ${packet.serverAddress}:${packet.port}"
        )

        if (packet.version != KeyMountainServer.PROTOCOL_LEVEL) {
            LOGGER.warn(
                "Client is trying to connect with a different protocol version. " +
                "This may cause crashes! (client: ${packet.version}, us: ${KeyMountainServer.PROTOCOL_LEVEL})"
            )
        }
    }

    // == Status == //
    private fun handleStatusRequestPacket(ref: ClientReference, packet: C2SStatusRequest) {
        val statusResponse = ServerStatusResponse(
            version = ServerStatusResponse.StatusVersion(
                name = KeyMountainServer.GAME_VERSION,
                protocol = KeyMountainServer.PROTOCOL_LEVEL
            ),
            players = ServerStatusResponse.StatusPlayers(
                max = 0,
                online = 0,
                sample = emptyList()
            ),
            description = ServerStatusResponse.StatusDescription(
                text = "A Key Mountain server.",
            ),
        )
        val s = server.jsonMapper.writeValueAsString(statusResponse)
        ref.enqueueBasePacket(S2CStatusResponse(s))
    }

    private fun handleStatusPingPacket(ref: ClientReference, packet: C2SStatusPing) {
        ref.enqueueBasePacket(S2CStatusPong(packet.value))
    }

    init {
        server.packets.addIncomingPacket(HANDSHAKE, C2SHandshake.PACKET_ID, C2SHandshake)
        server.packets.setPacketAction(HANDSHAKE, C2SHandshake.PACKET_ID, ::handleHandshakePacket)

        server.packets.addIncomingPacket(STATUS, C2SStatusRequest.id, C2SStatusRequest)
        server.packets.setPacketAction(STATUS, C2SStatusRequest.id, ::handleStatusRequestPacket)
        server.packets.addIncomingPacket(STATUS, C2SStatusPing.PACKET_ID, C2SStatusPing)
        server.packets.setPacketAction(STATUS, C2SStatusPing.PACKET_ID, ::handleStatusPingPacket)
        server.packets.addOutgoingPacket(STATUS, S2CStatusResponse.PACKET_ID, S2CStatusResponse)
        server.packets.addOutgoingPacket(STATUS, S2CStatusPong.PACKET_ID, S2CStatusPong)
    }

    override fun run() {
        Thread.currentThread().name = "KeyMountain-Server-Networker"

        while (true) {
            val next = incoming.take()
            // drain any extra packets for clients that aren't being processed
            if (!next.ref.stillReceivingPackets) continue

            // special handling for handshake packets as the client listener switches into the
            // desired state
            LOGGER.trace("dispatching packet of type {}", next)
            // dummy <T>, the type checking is only used for the ProtocolPacketAction interface

            try {
                server.packets.applyPacketAction<ProtocolPacket>(next)
            } catch (e: Exception) {
                // TODO: better kill logic
                LOGGER.error("Error in protocol packet processing!", e)
                next.ref.close()
            }
        }
    }
}