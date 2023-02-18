/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package tf.veriny.keymountain.api.util

import okio.BufferedSink
import okio.BufferedSource
import java.util.*


// borrowed from wiki.vg
private const val SEGMENT_BITS = 0x7F
private const val CONTINUE_BIT = 0x80

/**
 * Reads a variably-encoded integer from a Source.
 */
public fun BufferedSource.readVarInt(): Int {
    var value = 0
    var position = 0
    var currentByte = 0
    while (true) {
        currentByte = readByte().toInt()
        value = value or (currentByte and SEGMENT_BITS shl position)

        if (currentByte and CONTINUE_BIT == 0) break
        position += 7

        if (position >= 32) throw RuntimeException("VarInt is too big")
    }
    return value
}

/**
 * Reads a variably-encoded long from a Source.
 */
public fun BufferedSource.readVarLong(): Long {
    var value: Long = 0
    var position = 0
    var currentByte: Byte

    while (true) {
        currentByte = readByte()
        value = value or ((currentByte.toInt() and SEGMENT_BITS).toLong() shl position)

        if (currentByte.toInt() and CONTINUE_BIT == 0) break
        position += 7

        if (position >= 64) throw RuntimeException("VarLong is too big")
    }

    return value
}

/**
 * Reads a single boolean from a Source.
 */
public fun BufferedSource.readBoolean(): Boolean {
    return readByte().toInt() == 0x1
}

/**
 * Reads a pascal-like Minecraft string (varint + utf-8 string data).
 */
public fun BufferedSource.readMcString(): String {
    val length = readVarInt()
    // TODO: is this right?
    return readUtf8(length.toLong())
}

/** Reads a single long-encoded UUID. */
public fun BufferedSource.readUUID(): UUID {
    val upper = readLong()
    val lower = readLong()
    return UUID(upper, lower)
}

/**
 * Writes a variably-encoded integer to a Sink.
 */
public fun BufferedSink.writeVarInt(toWrite: Int) {
    var value = toWrite

    while (true) {
        if (value and SEGMENT_BITS.inv() == 0) {
            writeByte(value)
            return
        }

        writeByte(value and SEGMENT_BITS or CONTINUE_BIT)
        value = value ushr 7
    }
}

/**
 * Writes a variably-encoded long to a Sink.
 */
public fun BufferedSink.writeVarLong(toWrite: Long) {
    var value = toWrite

    while (true) {
        if (value and SEGMENT_BITS.toLong().inv() == 0L) {
            writeByte(value.toInt())
            return
        }
        writeByte((value and SEGMENT_BITS.toLong() or CONTINUE_BIT.toLong()).toInt())

        // Note: >>> means that the sign bit is shifted with the rest of the number rather than being left alone
        value = value ushr 7
    }
}

/**
 * Writes a pascal-like Minecraft string (varint + utf-8 string data).
 */
public fun BufferedSink.writeMcString(s: String) {
    writeVarInt(s.length)
    writeUtf8(s)
}

/** Writes a UUID to the stream. */
public fun BufferedSink.writeUuid(u: UUID) {
    writeLong(u.mostSignificantBits)
    writeLong(u.leastSignificantBits)
}