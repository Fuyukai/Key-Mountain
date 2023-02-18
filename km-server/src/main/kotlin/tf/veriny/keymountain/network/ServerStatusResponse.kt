/*
 * This file is part of Key-Mountain Server.
 *
 * Key-Mountain Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, version 3.
 *
 * Key-Mountain Server is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Key-Mountain Server. If not, see <http://www.gnu.org/licenses/>.
 */

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