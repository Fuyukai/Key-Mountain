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


    override fun toString(): String {
        return full
    }
}