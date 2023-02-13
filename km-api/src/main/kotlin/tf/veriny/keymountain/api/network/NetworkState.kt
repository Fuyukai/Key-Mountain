package tf.veriny.keymountain.api.network

/**
 * An enumeration of possible states for the network connection to be in.
 */
public enum class NetworkState {
    HANDSHAKE,
    STATUS,
    LOGIN,
    PLAY,
    ;
}