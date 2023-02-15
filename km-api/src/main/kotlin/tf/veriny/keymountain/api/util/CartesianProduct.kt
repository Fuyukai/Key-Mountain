package tf.veriny.keymountain.api.util

import kotlin.reflect.KFunction

public typealias CartesianProduct = Set<List<*>>

/**
 * Create the cartesian product of any number of sets of any size. Useful for parameterized tests
 * to generate a large parameter space with little code. Note that any type information is lost, as
 * the returned set contains list of any combination of types in the input set.
 *
 * @param sets Any additional sets.
 */
public fun cartesianProduct(vararg sets: Set<*>): CartesianProduct {
    val list = (sets)
        .fold(listOf(listOf<Any?>())) { acc, set ->
            acc.flatMap { list -> set.map { element -> list + element } }
        }

    return LinkedHashSet(list)
}


/**
 * Transform elements of a cartesian product.
 */
public fun <T> CartesianProduct.map(transform: KFunction<T>): List<T> = map { transform.call(*it.toTypedArray()) }