/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package tf.veriny.keymountain.api.network

/**
 * API for registering protocol-level packet creators as well as protocol-level packet handlers.
 */
public interface PacketRegistry : PluginPacketRegistry {
    /** Adds a handler for the specified C2S packet ID and network state. */
    public fun <T : ProtocolPacket> addIncomingPacket(
        state: NetworkState,
        id: Int,
        maker: ProtocolPacketSerialiser<T>,
        action: ProtocolPacketAction<T>,
    )

    /** Adds a handler for the specified S2C packet ID and network state. */
    public fun <T : ProtocolPacket> addOutgoingPacket(
        state: NetworkState,
        id: Int,
        maker: ProtocolPacketSerialiser<T>
    )
}