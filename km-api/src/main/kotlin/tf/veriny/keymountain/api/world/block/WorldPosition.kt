/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package tf.veriny.keymountain.api.world.block

/**
 * An absolute block position in a world.
 */
@JvmInline
public value class WorldPosition(public val position: Long) {
    public companion object {
        public operator fun invoke(x: Int, z: Int, y: Int): WorldPosition {
            var pos = 0L
            pos = (x.toLong().and(0x3FFFFFFL).shl(38))
            pos = pos.or((z.toLong().and(0x3FFFFFFL)).shl(12))
            pos = pos.or(y.toLong().and(0xFFFL))
            return WorldPosition(position = pos)
        }
    }

    public val x: Int
        get() = (position.shr(38)).toInt()

    public val z: Int
        get() = (position.shl(26).shr(38)).toInt()

    public val y: Int
        get() = (position.and((1L).shl(12) - 1L)).toInt()

    /**
     * Replaces the position of this entity with another one,
     */
    public fun replace(x: Int = this.x, y: Int = this.y, z: Int = this.z): WorldPosition {
        return Companion(x, y, z)
    }

    override fun toString(): String {
        return "WorldPosition[x=$x, y=$y, z=$z]"
    }
}