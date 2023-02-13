package tf.veriny.keymountain.api.network

/**
 * A packet that operates on the Minecraft protocol level. This is separate from mod packets, which
 * use mod channels.
 */
public interface ProtocolPacket {
    /** The ID for this packet. Usually statically defined. */
    public val id: Int
}