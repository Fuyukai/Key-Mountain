package tf.veriny.keymountain.api.network.packets

import okio.Buffer
import tf.veriny.keymountain.api.network.ProtocolPacket
import tf.veriny.keymountain.api.network.ProtocolPacketSerialiser

/**
 * Sent by the client to request server status information.
 */
public object C2SStatusRequest : ProtocolPacket, ProtocolPacketSerialiser<C2SStatusRequest> {
    override val id: Int = 0x00

    override fun readIn(data: Buffer): C2SStatusRequest {
        return C2SStatusRequest
    }

    override fun writeOut(packet: C2SStatusRequest, data: Buffer) {
        // Nothing to write
    }
}