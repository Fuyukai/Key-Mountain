/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package tf.veriny.keymountain.api.util

private val namespaceRegexp = "[a-z0-9.-_]*".toRegex()
private val valueRegexp = "[a-z0-9.-_/]*".toRegex()

/**
 * Uniquely identifies a single resource in the game.
 */
public class Identifier(public val full: String) {
    public constructor(namespace: String, thing: String) : this("$namespace:$thing")

    public val namespace: String
    public val thing: String

    init {
        val (sp1, sp2) = full.split(':', limit = 2)
        namespace = sp1
        thing = sp2

        if (!namespaceRegexp.matches(namespace)) throw IllegalArgumentException(namespace)
        if (!valueRegexp.matches(thing)) throw IllegalArgumentException(thing)
    }

    override fun hashCode(): Int {
        return full.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other !is Identifier) return false

        return other.full == full
    }


    override fun toString(): String {
        return full
    }
}