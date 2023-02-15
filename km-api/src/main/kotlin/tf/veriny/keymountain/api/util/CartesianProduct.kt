package tf.veriny.keymountain.api.util


// credit: https://stackoverflow.com/a/714256
// written by Michael Myers, licenced under the CC 3.0 BY-SA
// then J2K'd.
public fun cartesianProduct(vararg sets: Set<*>): Set<MutableSet<Any?>> {
    require(sets.size >= 2) {
        "Can't have a product of fewer than two sets (got ${sets.size})"
    }
    return cartesianImpl(0, *sets)
}

private fun cartesianImpl(index: Int, vararg sets: Set<*>): Set<MutableSet<Any?>> {
    val ret: MutableSet<MutableSet<Any?>> = HashSet()
    if (index == sets.size) {
        ret.add(HashSet())
    } else {
        for (obj in sets[index]) {
            for (set in cartesianImpl(index + 1, *sets)) {
                set.add(obj)
                ret.add(set)
            }
        }
    }
    return ret
}