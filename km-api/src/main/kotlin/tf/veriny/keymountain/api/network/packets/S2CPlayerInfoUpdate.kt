/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package tf.veriny.keymountain.api.network.packets

import okio.Buffer
import tf.veriny.keymountain.api.network.ProtocolPacket
import tf.veriny.keymountain.api.network.ProtocolPacketSerialiser
import tf.veriny.keymountain.api.util.writeMcString
import tf.veriny.keymountain.api.util.writeUuid
import tf.veriny.keymountain.api.util.writeVarInt
import java.util.*

// TODO: rn we have to send 2*N packets (where N = players) but we can pack this into just two
//  packets with a bit of API design work.

/**
 * Sent to inform clients about new players connecting to the server.
 */
public class S2CPlayerInfoUpdate(
    public val uuid: UUID,
    public val addPlayer: AddPlayer? = null,
    public val updateListed: UpdateListed? = null,
) : ProtocolPacket {
    public sealed class Action

    /** Action that adds a new tracked player to the client. */
    public class AddPlayer(public val name: String, public val properties: Map<String, String>) : Action()

    /** Action that marks a player as listed. */
    public class UpdateListed(public val listed: Boolean) : Action()

    public companion object : ProtocolPacketSerialiser<S2CPlayerInfoUpdate> {
        public const val PACKET_ID: Int = 0x36

        override fun readIn(data: Buffer): S2CPlayerInfoUpdate {
            TODO("Not yet implemented")
        }

        override fun writeOut(packet: S2CPlayerInfoUpdate, data: Buffer) {
            // this is a REALLY stupid packet and it's not structured the way you'd expect
            // I first thought it was a list of (UUID, Action), e.g.
            // [(UUID, AddPlayer), (UUID, UpdateListed)]
            // but it's actually a list of (UUID, All Actions), e.g.
            // [(UUID, AddPlayer + UpdateListed), (UUID, AddPlayer + UpdateListed)]
            // but ofc in the infinite wisdom of mojang the bit flag is per packet, not per player
            // so we just only support one packet per player :)

            var actionBits = 0

            val subBuf = Buffer()
            val adp = packet.addPlayer
            if (adp != null) {
                subBuf.writeMcString(adp.name)
                subBuf.writeVarInt(adp.properties.size)
                for ((k, v) in adp.properties) {
                    subBuf.writeMcString(k)
                    subBuf.writeMcString(v)
                    // boolean: is signed (no, fuck off)
                    subBuf.writeByte(0)
                }
                actionBits = actionBits.or(1)
            }

            val ul = packet.updateListed
            if (ul != null) {
                subBuf.writeByte(if (ul.listed) 1 else 0)

                actionBits = actionBits.or(1 shl 3)
            }

            data.writeByte(actionBits)
            // one entry, aka one player
            data.writeVarInt(1)
            data.writeUuid(packet.uuid)
            data.write(subBuf, subBuf.size)
        }
    }

    override val id: Int get() = PACKET_ID
}