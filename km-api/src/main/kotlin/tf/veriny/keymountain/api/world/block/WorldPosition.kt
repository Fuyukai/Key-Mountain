package tf.veriny.keymountain.api.world.block

import javax.management.Query.or

/**
 * An absolute block position in a world.
 */
@JvmInline
public value class WorldPosition(public val position: ULong) {
    public companion object {
        public operator fun invoke(x: Int, z: Int, y: Int): WorldPosition {
            var pos = 0UL
            pos = (x.toULong().and(0x3FFFFFFUL).shl(38))
            pos = pos.or((z.toULong().and (0x3FFFFFFUL)).shl(12))
            pos = pos.or(y.toULong().and(0xFFFUL))
            return WorldPosition(position = pos)
        }
    }

    public val x: Int
        get() = (position.shr(38)).toInt()

    public val z: Int
        get() = (position.shr(12).and((1UL).shl(26) - 1UL)).toInt()

    public val y: Int
        get() = (position.and((1UL).shl(12) - 1UL)).toInt()

    override fun toString(): String {
        return "WorldPosition[x=$x, y=$y, z=$z]"
    }
}