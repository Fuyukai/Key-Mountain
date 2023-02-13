package tf.veriny.keymountain.network

import tf.veriny.keymountain.api.NoSuchPacketException
import tf.veriny.keymountain.api.network.*

/**
 * Server-side implementation of the packet registry.
 */
public class ProtocolPacketRegistryImpl : ProtocolPacketRegistry {
    private class Subregistry {
        val c2sPacketMakers = Array<ProtocolPacketSerialiser<*>?>(256) { null }
        val s2cPacketMakers = Array<ProtocolPacketSerialiser<*>?>(256) { null }
        val packetActions = Array<ProtocolPacketAction<*>?>(256) { null }

        fun forIncoming(id: Int): ProtocolPacketSerialiser<*>? {
            return c2sPacketMakers[id]
        }

        fun forOutgoing(id: Int): ProtocolPacketSerialiser<*>? {
            return s2cPacketMakers[id]
        }
    }

    private val subRegisteries = mutableMapOf<NetworkState, Subregistry>()

    private fun packetError(state: NetworkState, id: Int): Nothing {
        throw NoSuchPacketException("No such packet '$id' in state '$state'")
    }

    override fun <T : ProtocolPacket> addIncomingPacket(state: NetworkState, id: Int, maker: ProtocolPacketSerialiser<T>) {
        val subregistry = subRegisteries.getOrPut(state) { Subregistry() }
        subregistry.c2sPacketMakers[id] = maker
    }

    override fun <T : ProtocolPacket> addOutgoingPacket(state: NetworkState, id: Int, maker: ProtocolPacketSerialiser<T>) {
        val subregistry = subRegisteries.getOrPut(state) { Subregistry() }
        subregistry.s2cPacketMakers[id] = maker
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : ProtocolPacket> getIncomingMaker(state: NetworkState, id: Int): ProtocolPacketSerialiser<T> {
        val subregistry = subRegisteries[state] ?: packetError(state, id)
        return subregistry.forIncoming(id) as? ProtocolPacketSerialiser<T> ?: packetError(state, id)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : ProtocolPacket> getOutgoingMaker(state: NetworkState, id: Int): ProtocolPacketSerialiser<T> {
        val subregistry = subRegisteries[state] ?: packetError(state, id)
        return subregistry.forOutgoing(id) as? ProtocolPacketSerialiser<T> ?: packetError(state, id)
    }


    override fun <T : ProtocolPacket> setPacketAction(state: NetworkState, id: Int, action: ProtocolPacketAction<T>) {
        val subregistry = subRegisteries[state] ?: packetError(state, id)
        subregistry.packetActions[id] = action
    }

    /**
     * Applies the action from an incoming packet.
     */
    @Suppress("UNCHECKED_CAST")
    public fun <T : ProtocolPacket> applyPacketAction(
        packet: ClientPacket,
    ) {
        val state = packet.lastState
        val id = packet.packet.id
        val subregistry = subRegisteries[state] ?: packetError(state, id)
        val action = subregistry.packetActions[id] as? ProtocolPacketAction<T>
                     ?: packetError(state, id)
        action(packet.ref, packet.packet as T)
    }
}