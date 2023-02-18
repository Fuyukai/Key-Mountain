/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package tf.veriny.keymountain.api

/**
 * The base class for all Key Mountain exceptions.
 */
public open class KeyMountainException(
    message: String, cause: Throwable? = null
) : Exception(message, cause)

/**
 * An exception caused by an issue in the network.
 */
public open class KeyMountainNetworkException(
    message: String, cause: Throwable? = null
) : KeyMountainException(message = message, cause = cause)

/** Thrown when an illegal packet is sent by the client. */
public class IllegalPacketException(
    message: String, cause: Throwable? = null,
) : KeyMountainNetworkException(message, cause)

/** Thrown if an invalid packet is sent. */
public class NoSuchPacketException(
    message: String, cause: Throwable? = null,
) : KeyMountainNetworkException(message, cause)

/** Thrown if a packet's payload is too long. */
public class PayloadTooLongException(
    size: Long, maxSize: Long = Int.MAX_VALUE.toLong(), cause: Throwable? = null,
) : KeyMountainNetworkException("Payload is too long: '$size' > '$maxSize'")
