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
@JvmInline
public value class Identifier(public val full: String) {
    public constructor(namespace: String, thing: String) : this("$namespace:$thing")

    init {
        val (sp1, sp2) = full.split(':', limit = 2)

        if (!namespaceRegexp.matches(sp1)) throw IllegalArgumentException(sp1)
        if (!valueRegexp.matches(sp2)) throw IllegalArgumentException(sp2)
    }

    override fun toString(): String {
        return full
    }
}

public val Identifier.namespace: String
    get() = (full.split(':', limit = 2))[0]

public val Identifier.thing: String
    get() = (full.split(':', limit = 2))[1]