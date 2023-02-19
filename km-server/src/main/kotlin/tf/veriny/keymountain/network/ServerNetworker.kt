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

package tf.veriny.keymountain.network

import lbmq.LinkedBlockingMultiQueue
import lbmq.Offerable
import org.apache.logging.log4j.LogManager
import tf.veriny.keymountain.KeyMountainServer
import tf.veriny.keymountain.api.client.ClientReference
import tf.veriny.keymountain.api.data.VanillaSynchronisableRegistry
import tf.veriny.keymountain.api.entity.PlayerEntity
import tf.veriny.keymountain.api.network.NetworkState.*
import tf.veriny.keymountain.api.network.ProtocolPacket
import tf.veriny.keymountain.api.network.packets.*
import tf.veriny.keymountain.api.network.plugin.BidiBrand
import tf.veriny.keymountain.api.network.plugin.PluginPacket
import tf.veriny.keymountain.api.util.Identifiable
import tf.veriny.keymountain.api.world.GameMode
import tf.veriny.keymountain.api.world.block.WorldPosition
import tf.veriny.keymountain.client.ClientConnection
import tf.veriny.keymountain.world.WorldImpl

/**
 * Routes incoming packets from clients and updates the server state appropriately.
 */
public class ServerNetworker(private val server: KeyMountainServer) : Runnable {
    private companion object {
        private val LOGGER = LogManager.getLogger(ServerNetworker::class.java)
    }

    private val incoming = LinkedBlockingMultiQueue<ClientReference, IncomingPacket>()
    private val syncher = QuiltRegistrySyncher(server.data, this)

    /** Adds a new client sub-queue. */
    internal fun getSubQueue(ref: ClientReference): Offerable<IncomingPacket> {
        incoming.addSubQueue(ref, 0)
        return incoming.getSubQueue(ref)
    }

    /** Removes a client sub-queue. Used when a client is disconnecting. */
    internal fun removeSubQueue(ref: ClientReference) {
        // thread-safe as LBMQ releases the lock via conditions
        LOGGER.trace("removing subqueue for {}", ref)
        incoming.removeSubQueue(ref)
    }

    // == Misc == //
    private fun handleBrandPacket(ref: ClientReference, packet: BidiBrand) {
        LOGGER.debug("Client ${ref.loginInfo.username} is using client '${packet.brand}'")
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
        ref.enqueueProtocolPacket(S2CStatusResponse(s))
    }

    private fun handleStatusPingPacket(ref: ClientReference, packet: C2SStatusPing) {
        ref.enqueueProtocolPacket(S2CStatusPong(packet.value))
    }

    // == Login == //
    private fun handleLoginStartPacket(ref: ClientReference, packet: C2SLoginStart) {
        LOGGER.info("Incoming connection from {} (uuid: {})", packet.username, packet.uuid)
        ref.changeLoginInfo(uuid = packet.uuid, username = packet.username)

        ref.enqueueProtocolPacket(S2CLoginSuccess(packet.uuid, packet.username))
        ref.transitionToState(PLAY)

        // send registry sync data
        syncher.sendHandshake(ref)
    }

    // == Play == //
    private fun handlePluginMessagePacket(ref: ClientReference, packet: C2SPluginMessage) {
        LOGGER.trace("received plugin message {}", packet.channel)

        val serialiser = server.data.packets.getIncomingPluginMaker<PluginPacket>(packet.channel)
        if (serialiser == null) {
            val body = packet.data.readByteString()
            LOGGER.warn("Received unrecognised plugin message {}! (body: {})", packet.channel, body.base64())
            return
        }

        val action = server.data.packets.getPluginAction<PluginPacket>(packet.channel)
        if (action == null) {
            // ??
            LOGGER.warn("Received plugin message {}, but we don't have a registered handler!", packet.channel)
        } else {
            val pluginPacket = serialiser.readIn(packet.data)
            action(ref, pluginPacket)
        }
    }

    private fun handlePongPacket(ref: ClientReference, packet: C2SPong) {
        if (packet.data == S2CPing.SYNC_COMPLETED_PING) {
            LOGGER.debug("registry sync completed successfully!")
        } else {
            // ???
            LOGGER.error("Client sent an unsolicited pong packet...?")
            return ref.die("Don't care + didn't ask")
        }

        // spawn the player
        val world = server.worlds.first() as WorldImpl
        val playerEntity = world.spawnEntity(PlayerEntity, WorldPosition(0, 0, 128), null)
        ref.entity = playerEntity

        // send all the various play packets
        val startPlaying = S2CStartPlaying(
            entityId = playerEntity.uniqueId,
            isHardcore = false,
            gameMode = GameMode.CREATIVE,
            dimensionIds = server.data.dimensions.getAllEntries().map { it.identifier },
            synchronisedRegisteries = listOf(server.data.dimensions, server.data.biomeNetworkData) as List<VanillaSynchronisableRegistry<Identifiable>>,
            dimensionType = "minecraft:overworld",
            viewDistance = 12, clientRenderDistance = 7,
            enableRespawn = false, isFlat = true
        )
        ref.enqueueProtocolPacket(startPlaying)

        ref.enqueuePluginPacket(BidiBrand("key-mountain"))

        // ack!
        for (chunkX in -7..7) {
            for (chunkZ in -7..7) {
                ref.enqueueChunkData(chunkX.toLong(), chunkZ.toLong())
            }
        }

        val setSpawnPosition = S2CSetSpawnPosition(WorldPosition(0, 0, 128), 0f)
        ref.enqueueProtocolPacket(setSpawnPosition)

        val setPositionPacket = S2CForcePlayerPosition(
            0.0, 0.0, 420.0,
            0f, 0f, 0, 1234567, true
        )
        ref.enqueueProtocolPacket(setPositionPacket)

    }

    private fun handleClientInformationPacket(ref: ClientReference, packet: C2SClientInformation) {
        LOGGER.debug("client {}'s settings: {}", ref.loginInfo.username, packet)
    }

    private fun commonHandleSetPlayerPosition(
        ref: ClientReference,
        packet: C2SSetPlayerPosition,
        rotation: C2SSetPlayerRotation?,
        onGround: Boolean
    ) {
        val entity = ref.entity
        if (entity == null) {
            LOGGER.error("client sent a set player position before they exist??")
            return ref.die("Illegal move packet")
        } else {
            LOGGER.trace("player sent position: ({}, {}, {})", packet.x, packet.z, packet.feetY)
        }

        // awkwardly hover the player at Y=132 for now

        entity.position.x = packet.x
        entity.position.z = packet.z
        entity.position.y = packet.feetY

        /*val position = S2CForcePlayerPosition(
            0.0, 0.0, 132.0,
            rotation?.yaw ?: 0f, rotation?.pitch ?: 0f, 0, 1234567, true
        )
        ref.enqueueProtocolPacket(position)*/
    }

    private fun handleSetPlayerPositionPacket(ref: ClientReference, packet: C2SSetPlayerPosition) {
        commonHandleSetPlayerPosition(ref, packet, null, packet.onGround)
    }

    private fun handleSetPlayerCombinedPacket(ref: ClientReference, packet: C2SSetPlayerCombined) {
        commonHandleSetPlayerPosition(ref, packet.position, packet.rotation, packet.onGround)
    }

    private fun handleSetPlayerRotationPacket(ref: ClientReference, packet: C2SSetPlayerRotation) {
        // pass
    }

    private fun handleConfirmTeleportationPacket(ref: ClientReference, packet: C2SConfirmTeleportation) {
        // pass
    }

    private fun handleSwingArmPacket(ref: ClientReference, packet: C2SSwingArm) {
        // pass
    }

    init {
        val packets = server.data.packets
        packets.addIncomingPacket(HANDSHAKE, C2SHandshake.PACKET_ID, C2SHandshake, ::handleHandshakePacket)

        packets.addIncomingPacket(STATUS, C2SStatusRequest.id, C2SStatusRequest, ::handleStatusRequestPacket)
        packets.addIncomingPacket(STATUS, C2SStatusPing.PACKET_ID, C2SStatusPing, ::handleStatusPingPacket)
        packets.addOutgoingPacket(STATUS, S2CStatusResponse.PACKET_ID, S2CStatusResponse)
        packets.addOutgoingPacket(STATUS, S2CStatusPong.PACKET_ID, S2CStatusPong)

        packets.addIncomingPacket(LOGIN, C2SLoginStart.PACKET_ID, C2SLoginStart, ::handleLoginStartPacket)
        packets.addOutgoingPacket(LOGIN, S2CLoginSuccess.PACKET_ID, S2CLoginSuccess)

        packets.addIncomingPacket(PLAY, C2SPluginMessage.PACKET_ID, C2SPluginMessage, ::handlePluginMessagePacket)
        packets.addIncomingPacket(PLAY, C2SPong.PACKET_ID, C2SPong, ::handlePongPacket)
        packets.addIncomingPacket(PLAY, C2SClientInformation.PACKET_ID, C2SClientInformation, ::handleClientInformationPacket)
        packets.addIncomingPacket(PLAY, C2SSetPlayerPosition.PACKET_ID, C2SSetPlayerPosition, ::handleSetPlayerPositionPacket)
        packets.addIncomingPacket(PLAY, C2SSetPlayerCombined.PACKET_ID, C2SSetPlayerCombined, ::handleSetPlayerCombinedPacket)
        packets.addIncomingPacket(PLAY, C2SConfirmTeleportation.PACKET_ID, C2SConfirmTeleportation, ::handleConfirmTeleportationPacket)
        packets.addIncomingPacket(PLAY, C2SSetPlayerRotation.PACKET_ID, C2SSetPlayerRotation, ::handleSetPlayerRotationPacket)
        packets.addIncomingPacket(PLAY, C2SSwingArm.PACKET_ID, C2SSwingArm, ::handleSwingArmPacket)

        packets.addOutgoingPacket(PLAY, S2CPing.PACKET_ID, S2CPing)
        packets.addOutgoingPacket(PLAY, S2CPluginMessage.PACKET_ID, S2CPluginMessage)
        packets.addOutgoingPacket(PLAY, S2CDisconnectPlay.PACKET_ID, S2CDisconnectPlay)
        packets.addOutgoingPacket(PLAY, S2CStartPlaying.PACKET_ID, S2CStartPlaying)
        packets.addOutgoingPacket(PLAY, S2CSetSpawnPosition.PACKET_ID, S2CSetSpawnPosition)
        packets.addOutgoingPacket(PLAY, S2CForcePlayerPosition.PACKET_ID, S2CForcePlayerPosition)
        packets.addOutgoingPacket(PLAY, S2CChunkData.PACKET_ID, S2CChunkData)

        packets.addIncomingPacket(BidiBrand.ID, BidiBrand, ::handleBrandPacket)
        packets.addOutgoingPacket(BidiBrand.ID, BidiBrand)

        syncher.setupPacketHandlers(packets)
    }

    override fun run() {
        Thread.currentThread().name = "KeyMountain-Server-Networker"
        val packets = server.data.packets

        while (true) {
            val next = incoming.take()
            // drain any extra packets for clients that aren't being processed
            if (!next.ref.stillReceivingPackets) continue

            // special handling for handshake packets as the client listener switches into the
            // desired state
            LOGGER.trace("dispatching packet of type {}", next)
            // dummy <T>, the type checking is only used for the ProtocolPacketAction interface

            try {
                packets.applyPacketAction<ProtocolPacket>(next)
            } catch (e: Exception) {
                // TODO: better kill logic
                LOGGER.error("Error in protocol packet processing!", e)
                next.ref.die("Internal server error")
            }
        }
    }
}