/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package tf.veriny.keymountain.api.world.block

import tf.veriny.keymountain.api.KeyMountainException
import tf.veriny.keymountain.api.util.Identifiable
import tf.veriny.keymountain.api.util.Identifier

// gross interface hack

/**
 * Helper interface for block metadata. Implement this by delegating to an instance.
 */
public interface WithBlockMetadata {
    /** Delegate for a type that has block properties. */
    public class Properties(override val properties: LinkedHashSet<BlockProperty<*>>) : WithBlockMetadata {
        public val metadata: BlockMetadata = BlockMetadata(properties)

        override fun <E : Any, T : BlockProperty<E>> getPropertyValue(
            meta: UInt, property: T
        ): E {
            return metadata.get(meta, property)
        }

        override fun <E : Any, T : BlockProperty<E>> setPropertyValue(
            meta: UInt, property: T, value: E
        ): UInt {
            return metadata.set(meta, property, value)
        }
    }

    /** Throws exceptions on accessing properties. */
    public object NoMetadata : WithBlockMetadata {
        override val properties: LinkedHashSet<BlockProperty<*>>
            get() = linkedSetOf()

        override fun <E : Any, T : BlockProperty<E>> getPropertyValue(
            meta: UInt, property: T
        ): E {
            throw KeyMountainException("this block has no metadata!")
        }

        override fun <E : Any, T : BlockProperty<E>> setPropertyValue(
            meta: UInt, property: T, value: E
        ): UInt {
            throw KeyMountainException("this block has no metadata!")
        }
    }

    /**
     * The set of properties that this [BlockType] has.
     */
    public val properties: LinkedHashSet<BlockProperty<*>>

    /**
     * Gets the value of [property] for the metadata [meta] inside this block.
     */
    public fun <E : Any, T : BlockProperty<E>> getPropertyValue(
        meta: UInt, property: T
    ): E

    /**
     * Sets [property] to [value] in the for the metadata [meta] inside this block.
     */
    public fun <E : Any, T : BlockProperty<E>> setPropertyValue(
        meta: UInt, property: T, value: E
    ): UInt
}

/**
 * Defines a type of block that can be placed in the world.
 */
public interface BlockType : Identifiable, WithBlockMetadata {

}

/**
 * A block type that has no additional properties.
 */
public open class EmptyBlockType(
    override val identifier: Identifier
) : BlockType, WithBlockMetadata by WithBlockMetadata.NoMetadata {
    public constructor(id: String) : this(Identifier(id))

    override fun toString(): String {
        return "EmptyBlockType[id=$identifier]"
    }
}
