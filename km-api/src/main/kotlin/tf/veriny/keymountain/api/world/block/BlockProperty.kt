/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package tf.veriny.keymountain.api.world.block

import kotlin.math.ceil
import kotlin.math.log2
import kotlin.reflect.KClass

/** Base interface for block properties. */
public interface BlockProperty<E : Any> {
    /** The name of this property. Needs to be unique across the block type. */
    public val name: String

    /** The default value for this property. */
    public val default: E

    /** The number of bits required to store this property in the metadata. */
    public fun bitSize(): Int

    /** Converts the specified metadata bits into an instance of this container. */
    public fun fromBits(data: UInt): E

    /** Converts the provided data into metadata bits. */
    public fun toBits(data: E): UInt

    /**
     * Generates the permutations for this property. The order for this matters!
     */
    public fun permutations(): LinkedHashSet<E>

    /**
     * Checks if the specified element would be valid for this property.
     */
    public fun isValid(item: E): Boolean
}

/** A property containing a single boolean value. */
public class BoolProperty(
    override val name: String, override val default: Boolean
) : BlockProperty<Boolean> {
    override fun toString(): String {
        return name
    }

    override fun bitSize(): Int = 1
    override fun fromBits(data: UInt): Boolean = data == 1U
    override fun toBits(data: Boolean): UInt = if (data) 1U else 0U

    override fun permutations(): LinkedHashSet<Boolean> {
        // vanilla blockstates seem to put true first in the json
        return linkedSetOf(true, false)
    }

    override fun isValid(item: Boolean): Boolean = true
}

/**
 * A property containing an integer value, equal to or between [minimum] and [maximum].
 */
public class IntProperty(
    override val name: String,
    public val minimum: Int, public val maximum: Int,
    override val default: Int
) : BlockProperty<Int> {
    init {
        require(default in minimum .. maximum) {
            "'$default' must be in range '$minimum' .. '$maximum'"
        }
    }

    override fun bitSize(): Int {
        val size = maximum - minimum
        return ceil(log2(size.toDouble())).toInt()
    }

    override fun toString(): String {
        return name
    }

    // the number is just stored directly in the metadata
    override fun fromBits(data: UInt): Int = data.toInt() + minimum
    override fun toBits(data: Int): UInt = data.toUInt() - minimum.toUInt()
    override fun permutations(): LinkedHashSet<Int> = LinkedHashSet((minimum .. maximum).toList())
    override fun isValid(item: Int): Boolean = item in (minimum .. maximum)
}

/**
 * A property containing an enumeration instance.
 */
public open class EnumProperty<T : Enum<T>>(
    override val name: String,
    override val default: T
) : BlockProperty<T> {
    private val enumKlass = default.declaringJavaClass

    override fun toString(): String {
        return name
    }


    override fun bitSize(): Int {
        val size = enumKlass.enumConstants.size
        return ceil(log2(size.toDouble())).toInt()
    }

    override fun fromBits(data: UInt): T = enumKlass.enumConstants[data.toInt()]
    override fun toBits(data: T): UInt = data.ordinal.toUInt()
    override fun permutations(): LinkedHashSet<T> = LinkedHashSet(enumKlass.enumConstants.toList())
    override fun isValid(item: T): Boolean = true
}

/**
 * Like [EnumProperty], but for block directions. If [full], then [up] and [down] will be included;
 * otherwise, only
 */
public class DirectionProperty(
    override val name: String, override val default: Direction,
    public val full: Boolean
) : BlockProperty<Direction> {
    override fun toString(): String {
        return name
    }

    override fun bitSize(): Int {
        // full has 6 states, which requires 3 bits
        // non-full has 4, which is 2 bits
        return if (full) 3
        else 2
    }

    override fun fromBits(data: UInt): Direction = Direction.values()[data.toInt()]
    override fun toBits(data: Direction): UInt = data.ordinal.toUInt()

    override fun permutations(): LinkedHashSet<Direction> {
        // we do a little trolling
        // for the 4-variant it's north-south-west-east (what?)
        // and for the 6-variant its north-east-south-west-up-down.
        // (what the fuck, game!)
        return if (full) {
            LinkedHashSet(Direction.values().toList())
        } else {
            linkedSetOf(Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        }
    }

    override fun isValid(item: Direction): Boolean {
        return if (full) true
        else item != Direction.UP && item != Direction.DOWN
    }
}