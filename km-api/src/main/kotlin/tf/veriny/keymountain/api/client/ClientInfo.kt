/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package tf.veriny.keymountain.api.client

import java.util.UUID

/** The login info for a client. */
public data class ClientInfo(
    public val uuid: UUID = UUID(0L, 0L),
    public val username: String = "Player",
)