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

import jdk.incubator.concurrent.StructuredTaskScope
import lbmq.Offerable
import okio.Buffer
import okio.buffer
import okio.sink
import okio.source
import org.apache.logging.log4j.LogManager
import tf.veriny.keymountain.api.PayloadTooLongException
import tf.veriny.keymountain.api.network.NetworkState
import tf.veriny.keymountain.api.network.ProtocolPacket
import tf.veriny.keymountain.api.network.packets.*
import tf.veriny.keymountain.api.util.readVarInt
import tf.veriny.keymountain.api.util.writeVarInt
import tf.veriny.keymountain.api.world.ChunkColumnSerialiser
import tf.veriny.keymountain.client.ClientConnection
import java.io.EOFException
import java.net.Socket
import java.security.SecureRandom
import java.time.Duration
import java.util.concurrent.CountDownLatch
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import kotlin.random.asKotlinRandom

/**
 * Handles incoming connections from a client.
 */
internal class ClientNetworker(
    private val clientReference: ClientConnection,
    private val networker: ServerNetworker,
    private val packetRegistry: PacketRegistryImpl,
    private val clientSocket: Socket,
) : Runnable {
    public companion object {
        internal val LOGGER = LogManager.getLogger(ClientNetworker::class.java)

        private val secure = SecureRandom.getInstanceStrong().asKotlinRandom()
    }

    private val incomingPackets: Offerable<IncomingPacket> = networker.getSubQueue(clientReference)
    private val outgoingPackets = LinkedBlockingQueue<OutgoingPacket>()
    internal val outgoingChunks = LinkedBlockingQueue<Triple<ChunkColumnSerialiser, Long, Long>>()

    internal var state: NetworkState = NetworkState.HANDSHAKE
    internal var isClosing = false

    private val sink = clientSocket.sink().buffer()
    private val source = clientSocket.source().buffer()

    private val keepAliveLatch = CountDownLatch(1)

    private val keepAliveQueue = ArrayDeque<Long>()

    internal fun startSendingKeepAlives() {
        keepAliveLatch.countDown()
    }

    private fun runKeepAlive() {
        Thread.currentThread().name = "ClientListener-${clientSocket.remoteSocketAddress}-Pinger"
        keepAliveLatch.await()

        while (!(clientSocket.isClosed || isClosing)) {
            if (keepAliveQueue.size > 8) {
                LOGGER.error("Client has failed to respond to over eight keep-alive packets, cutting it off")
                throw EOFException()
            }

            val id = secure.nextLong()
            keepAliveQueue.addLast(id)
            enqueueBasePacket(S2CKeepAlive(id))

            Thread.sleep(Duration.ofSeconds(5L))
        }
    }

    private fun runChunkDataSerialiser() {
        while (!(clientSocket.isClosed || isClosing)) {
            val (serialiser, chunkX, chunkZ) = outgoingChunks.take()

            val buffer = Buffer()
            serialiser.writeForNetwork(clientReference, chunkX, chunkZ, buffer)
            val packet = S2CChunkData(chunkX, chunkZ, buffer)

            enqueueBasePacket(packet)
        }
    }

    /**
     * Constantly loops over incoming packets and pushes them to the ``incomingPackets`` queue.
     */
    private fun runSocketReader() {
        val addr = clientSocket.remoteSocketAddress
        Thread.currentThread().name = "ClientListener-${addr}-Reader"
        sink.timeout().timeout(15, TimeUnit.SECONDS)

        while (!(clientSocket.isClosed || isClosing)) {
            val fullLength = source.readVarInt()
            if (state == NetworkState.HANDSHAKE && fullLength == 0xFE) {
                // legacy ping, ignore it
                return
            }

            LOGGER.trace("reading packet of length $fullLength")

            val into = Buffer()
            var read = 0L
            while (read < fullLength) {
                val count = source.read(into, fullLength.toLong() - read)
                LOGGER.trace("read $count bytes")
                if (count == -1L) throw EOFException()
                read += count
            }

            val packetId = into.readVarInt()

            val packetMaker = packetRegistry.getIncomingMaker<ProtocolPacket>(state, packetId)
            val packet = packetMaker.readIn(into)

            // intercept C2SHandshake and change the state, or else we read in the next packet
            // is processed on the queue.
            val previousState = state

            when (packet) {
                is C2SHandshake -> {
                    LOGGER.trace("potentially changing state to {}", packet.nextState)
                    clientReference.transitionToState(packet.nextState)
                }
                is C2SKeepAlive -> {
                    val keepAlive = keepAliveQueue.removeFirst()
                    if (keepAlive != packet.value) {
                        LOGGER.error("Client sent us the wrong keep-alive value!")
                        throw EOFException()
                    } else {
                        LOGGER.trace("received keep-alive: ${packet.value}")
                    }
                }
                else -> {
                    LOGGER.trace("read packet {}", packet)

                    incomingPackets.put(IncomingPacket(previousState, clientReference, packet))
                }
            }
        }
    }

    private fun runSocketWriter() {
        val addr = clientSocket.remoteSocketAddress
        Thread.currentThread().name = "ClientListener-${addr}-Writer"

        while (!(clientSocket.isClosed || isClosing)) {
            LOGGER.trace("waiting for outgoing packet...")
            val (packetState, next) = outgoingPackets.take()
            LOGGER.trace("writing packet {}", next.id.toString(16))

            val packetMaker = packetRegistry.getOutgoingMaker<ProtocolPacket>(packetState, next.id)
            val buffer = Buffer()
            buffer.writeVarInt(next.id)
            packetMaker.writeOut(next, buffer)

            LOGGER.trace("full packet is {} bytes long", buffer.size)

            if (buffer.size > Int.MAX_VALUE.toLong()) {
                throw PayloadTooLongException(buffer.size, Int.MAX_VALUE.toLong())
            }

            sink.writeVarInt(buffer.size.toInt())
            // okio internally buffers this it seems
            sink.write(buffer, buffer.size)
            sink.flush()

            if (next is S2CDisconnectPlay) {
                LOGGER.info("Disconnecting client!")
                isClosing = true
                throw EOFException()
            }

            LOGGER.trace("successfully written and flushed packet!")
        }
    }

    // == api == //
    internal fun enqueueBasePacket(packet: ProtocolPacket) {
        LOGGER.trace("adding new packet {}", packet)
        val outgoing = OutgoingPacket(state, packet)
        outgoingPackets.put(outgoing)
    }

    override fun run() {
        LOGGER.debug("Starting client listener for {}", clientSocket.remoteSocketAddress)
        StructuredTaskScope.ShutdownOnFailure().use {
            it.fork(::runSocketReader)
            it.fork(::runSocketWriter)
            it.fork(::runKeepAlive)
            it.fork(::runChunkDataSerialiser)

            it.join()
            it.throwIfFailed()
        }
    }
}