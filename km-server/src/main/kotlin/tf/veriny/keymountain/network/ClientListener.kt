package tf.veriny.keymountain.network

import jdk.incubator.concurrent.StructuredTaskScope
import lbmq.Offerable
import okio.*
import org.apache.logging.log4j.LogManager
import tf.veriny.keymountain.api.util.readVarInt
import tf.veriny.keymountain.api.util.writeVarInt
import tf.veriny.keymountain.api.PayloadTooLongException
import tf.veriny.keymountain.api.client.ClientReference
import tf.veriny.keymountain.api.network.NetworkState
import tf.veriny.keymountain.api.network.ProtocolPacket
import tf.veriny.keymountain.api.network.ProtocolPacketRegistry
import tf.veriny.keymountain.api.network.packets.C2SHandshake
import java.io.EOFException
import java.net.Socket
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

/**
 * Handles incoming connections from a client.
 */
internal class ClientListener(
    private val clientReference: ClientReference,
    private val networker: ServerNetworker,
    private val packetRegistry: ProtocolPacketRegistry,
    private val clientSocket: Socket,
) : Runnable {
    public companion object {
        internal val LOGGER = LogManager.getLogger(ClientListener::class.java)
    }

    private val incomingPackets: Offerable<ClientPacket> = networker.getSubQueue(clientReference)
    private val outgoingPackets = LinkedBlockingQueue<ProtocolPacket>()

    internal var state: NetworkState = NetworkState.HANDSHAKE
    internal var isClosing = false

    private val sink = clientSocket.sink().buffer()
    private val source = clientSocket.source().buffer()

    private fun close() {

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
            if (packet is C2SHandshake) {
                LOGGER.trace("potentially changing state to {}", packet.nextState)
                clientReference.transistionToState(packet.nextState)
            }

            LOGGER.trace("read packet {}", packet)

            incomingPackets.put(ClientPacket(previousState, clientReference, packet))
        }
    }

    private fun runSocketWriter() {
        val addr = clientSocket.remoteSocketAddress
        Thread.currentThread().name = "ClientListener-${addr}-Writer"

        while (!(clientSocket.isClosed || isClosing)) {
            val next = outgoingPackets.take()
            LOGGER.trace("writing packet ${next.id}")

            val packetMaker = packetRegistry.getOutgoingMaker<ProtocolPacket>(state, next.id)
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
        }
    }

    // == api == //
    internal fun enqueueBasePacket(packet: ProtocolPacket) {
        outgoingPackets.put(packet)
    }

    override fun run() {
        LOGGER.debug("Starting client listener for {}", clientSocket.remoteSocketAddress)
        clientSocket.use {
            StructuredTaskScope.ShutdownOnFailure().use {
                it.fork(::runSocketReader)
                it.fork(::runSocketWriter)

                it.join()
                it.throwIfFailed()
            }
        }
    }
}