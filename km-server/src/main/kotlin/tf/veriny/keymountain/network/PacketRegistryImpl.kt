package tf.veriny.keymountain.network

import tf.veriny.keymountain.api.KeyMountainException
import tf.veriny.keymountain.api.NoSuchPacketException
import tf.veriny.keymountain.api.network.*
import tf.veriny.keymountain.api.network.plugin.PluginPacketAction
import tf.veriny.keymountain.api.network.plugin.PluginPacket
import tf.veriny.keymountain.api.network.plugin.PluginPacketSerialiser
import tf.veriny.keymountain.api.util.Identifier

private typealias PPair<T> = Pair<PluginPacketSerialiser<T>, PluginPacketAction<T>?>

/**
 * Server-side implementation of the packet registry.
 */
public class PacketRegistryImpl : PacketRegistry {
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

    private val incomingPluginPackets = mutableMapOf<Identifier, PPair<*>>()
    private val outgoingPluginPackets = mutableMapOf<Identifier, PluginPacketSerialiser<*>>()

    private fun packetError(state: NetworkState, id: Int): Nothing {
        throw NoSuchPacketException("No such packet '0x${id.toString(16)}' in state '$state'")
    }

    override fun <T : ProtocolPacket> addIncomingPacket(
        state: NetworkState, id: Int,
        maker: ProtocolPacketSerialiser<T>,
        action: ProtocolPacketAction<T>,
    ) {
        val subregistry = subRegisteries.getOrPut(state) { Subregistry() }
        subregistry.c2sPacketMakers[id] = maker
        subregistry.packetActions[id] = action
    }

    override fun <T : ProtocolPacket> addOutgoingPacket(state: NetworkState, id: Int, maker: ProtocolPacketSerialiser<T>) {
        val subregistry = subRegisteries.getOrPut(state) { Subregistry() }
        subregistry.s2cPacketMakers[id] = maker
    }

    override fun <T : PluginPacket> addIncomingPacket(
        channel: Identifier,
        packetSerialiser: PluginPacketSerialiser<T>,
        action: PluginPacketAction<T>
    ) {
        incomingPluginPackets[channel] = PPair(packetSerialiser, action)
    }

    override fun <T : PluginPacket> addOutgoingPacket(
        channel: Identifier,
        packetSerialiser: PluginPacketSerialiser<T>,
    ) {
        outgoingPluginPackets[channel] = packetSerialiser
    }

    @Suppress("UNCHECKED_CAST")
    public fun <T : ProtocolPacket> getIncomingMaker(state: NetworkState, id: Int): ProtocolPacketSerialiser<T> {
        val subregistry = subRegisteries[state] ?: packetError(state, id)
        return subregistry.forIncoming(id) as? ProtocolPacketSerialiser<T> ?: packetError(state, id)
    }

    @Suppress("UNCHECKED_CAST")
    public fun <T : ProtocolPacket> getOutgoingMaker(state: NetworkState, id: Int): ProtocolPacketSerialiser<T> {
        val subregistry = subRegisteries[state] ?: packetError(state, id)
        return subregistry.forOutgoing(id) as? ProtocolPacketSerialiser<T> ?: packetError(state, id)
    }

    @Suppress("UNCHECKED_CAST")
    public fun <T : PluginPacket> getIncomingPluginMaker(
        id: Identifier
    ): PluginPacketSerialiser<T>? {
        return incomingPluginPackets[id]?.first as PluginPacketSerialiser<T>?
    }

    @Suppress("UNCHECKED_CAST")
    public fun <T : PluginPacket> getOutgoingPluginMaker(
        id: Identifier
    ): PluginPacketSerialiser<T>? {
        return outgoingPluginPackets[id] as PluginPacketSerialiser<T>?
    }

    /**
     * Applies the action from an incoming packet.
     */
    @Suppress("UNCHECKED_CAST")
    public fun <T : ProtocolPacket> applyPacketAction(
        packet: IncomingPacket,
    ) {
        val state = packet.lastState
        val id = packet.packet.id
        val subregistry = subRegisteries[state] ?: packetError(state, id)
        val action = subregistry.packetActions[id] as? ProtocolPacketAction<T>
                     ?: packetError(state, id)
        action(packet.ref, packet.packet as T)
    }

    @Suppress("UNCHECKED_CAST")
    public fun <T : PluginPacket> getPluginAction(id: Identifier): PluginPacketAction<T>? {
        val (_, action) = incomingPluginPackets[id] ?: return null
        return action as PluginPacketAction<T>
    }
}