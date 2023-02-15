package tf.veriny.keymountain.api.util

private val namespaceRegexp = "[a-z0-9.-_]*".toRegex()
private val valueRegexp = "[a-z0-9.-_/]*".toRegex()

/** Uniquely identifies a single resource in the game. */
public data class Identifier(public val namespace: String, public val thing: String) {
    public companion object {
        public operator fun invoke(s: String): Identifier {
            val (first, second) = s.split(':', limit = 2)
            return Identifier(first, second)
        }
    }

    init {
        if (!namespaceRegexp.matches(namespace)) throw IllegalArgumentException(namespace)
        if (!valueRegexp.matches(thing)) throw IllegalArgumentException(thing)
    }

    /** The full value of this identifier. */
    public val full: String get() = "$namespace:$thing"

    override fun toString(): String {
        return full
    }
}