/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

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