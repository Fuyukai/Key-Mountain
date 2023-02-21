/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

@file:OptIn(ExperimentalStdlibApi::class)

package tf.veriny.keymountain.api.network.packets

import okio.Buffer
import tf.veriny.keymountain.api.IllegalPacketException
import tf.veriny.keymountain.api.network.NetworkState
import tf.veriny.keymountain.api.network.ProtocolPacket
import tf.veriny.keymountain.api.network.ProtocolPacketSerialiser
import tf.veriny.keymountain.api.util.readMcString
import tf.veriny.keymountain.api.util.readVarInt
import tf.veriny.keymountain.api.util.writeMcString
import tf.veriny.keymountain.api.util.writeVarInt

/**
 * Send by the client to the server upon initial connection.
 */
public class C2SHandshake(
    public val version: Int,
    public val serverAddress: String,
    public val port: Int,
    public val nextState: NetworkState
) : ProtocolPacket {
    public companion object : ProtocolPacketSerialiser<C2SHandshake> {
        public const val PACKET_ID: Int = 0x00

        override fun readIn(data: Buffer): C2SHandshake {
            val protocolVersion = data.readVarInt()
            val address = data.readMcString()
            val port = data.readShort().toUShort().toInt()
            val nextStateId = data.readVarInt()

            // TODO: Replace with entries
            val nextState = try {
                NetworkState.values()[nextStateId]
            } catch (e: IndexOutOfBoundsException) {
                throw IllegalPacketException("invalid network state ID '$nextStateId'")
            }

            return C2SHandshake(protocolVersion, address, port, nextState)
        }

        override fun writeOut(packet: C2SHandshake, data: Buffer) {
            data.writeVarInt(packet.version)
            data.writeMcString(packet.serverAddress)
            data.writeShort(packet.port)
            data.writeVarInt(packet.nextState.ordinal)
        }
    }

    override val id: Int get() = PACKET_ID
}