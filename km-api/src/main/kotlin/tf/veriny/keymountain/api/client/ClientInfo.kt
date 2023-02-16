package tf.veriny.keymountain.api.client

import java.util.UUID

/** The login info for a client. */
public data class ClientInfo(
    public val uuid: UUID = UUID(0L, 0L),
    public val username: String = "Player",
)