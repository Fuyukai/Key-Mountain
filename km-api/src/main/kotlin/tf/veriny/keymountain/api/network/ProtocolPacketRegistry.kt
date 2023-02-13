package tf.veriny.keymountain.api.network

/**
 * API for registering protocol-level packet creators as well as protocol-level packet handlers.
 */
public interface ProtocolPacketRegistry {
    /** Adds a handler for the specified C2S packet ID and network state. */
    public fun <T : ProtocolPacket> addIncomingPacket(
        state: NetworkState,
        id: Int,
        maker: ProtocolPacketSerialiser<T>
    )

    /** Adds a handler for the specified S2C packet ID and network state. */
    public fun <T : ProtocolPacket> addOutgoingPacket(
        state: NetworkState,
        id: Int,
        maker: ProtocolPacketSerialiser<T>
    )

    /** Gets a packet maker for the C2S packet [id] and the specified network [state]. */
    public fun <T : ProtocolPacket> getIncomingMaker(state: NetworkState, id: Int): ProtocolPacketSerialiser<T>

    /** Gets a packet maker for the S2C packet [id] and the specified network [state]. */
    public fun <T : ProtocolPacket> getOutgoingMaker(state: NetworkState, id: Int): ProtocolPacketSerialiser<T>

    /**
     * Sets the action to be performed on receiving the packet with [id] in the network [state].
     */
    public fun <T : ProtocolPacket> setPacketAction(
        state: NetworkState,
        id: Int,
        action: ProtocolPacketAction<T>
    )
}