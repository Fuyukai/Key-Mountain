/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package tf.veriny.keymountain.api.util

import tf.veriny.keymountain.api.world.block.WorldPosition

/**
 * A single point in 3D space.
 */
public class Vector3(public var x: Double, public var z: Double, public var y: Double) {
    public constructor(other: Vector3) : this(other.x, other.z, other.y)

    public fun set(from: Vector3) {
        x = from.x
        z = from.z
        y = from.y
    }

    public fun set(from: WorldPosition) {
        x = from.x.toDouble()
        z = from.z.toDouble()
        y = from.y.toDouble()
    }
}

public operator fun Vector3.component1(): Double = x
public operator fun Vector3.component2(): Double = z
public operator fun Vector3.component3(): Double = y