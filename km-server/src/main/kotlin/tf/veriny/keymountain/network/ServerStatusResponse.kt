package tf.veriny.keymountain.network

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.annotation.JsonSerialize

/**
 * Serialised and sent in StatusResponse packets.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public data class ServerStatusResponse(
    public val version: StatusVersion,
    public val players: StatusPlayers,
    public val description: StatusDescription,
    public val favicon: String? = null,
    public val previewsChat: Boolean = false,
    public val enforcesSecureChat: Boolean = false,
) {
    public data class StatusVersion(public val name: String, public val protocol: Int)

    public data class StatusPlayers(
        public val max: Int,
        public val online: Int,
        @JsonSerialize(contentAs = StatusPlayer::class)
        public val sample: List<StatusPlayer>?,
    ) {
        public data class StatusPlayer(public val name: String, public val id: String)
    }

    public data class StatusDescription(public val text: String)
}