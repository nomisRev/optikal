package optikal

import kategory.Option

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
annotation class Lenses

/**
 * A [Lens] can be seen as a pair of functions `get: (A) -> B` and `set: (B) -> (A) -> A`
 * - `get: (A) -> B` i.e. from an `A`, we can extract an `P`
 * - `set: (B) -> (A) -> A` i.e. if we replace target value by `B` in an `A`, we obtain another modified `A`
 *
 * @param A the source of a [Lens]
 * @param B the target of a [Lens]
 * @param get from an `A` we can extract a `B`
 * @param set replace the target value by `B` in an `A` so we obtain another modified `A`
 */
data class Lens<A, B>(val get: (A) -> B, val set: (B) -> (A) -> A) {

    /**
     * Modify the target of a [Lens] using a function `(B) -> B`
     */
    fun modify(f: (B) -> B, a: A) = set(f(get(a)))(a)

    /**
     * Compose a [Lens] with another [Lens]
     */
    infix fun <C> composeLens(l: Lens<B, C>): Lens<A, C> = Lens(
            get = { a -> l.get(get(a)) },
            set = { c -> { a -> set(l.set(c)(get(a)))(a) } }
    )

    /**
     * plus operator overload to compose lenses
     */
    operator fun <C> plus(other: Lens<B, C>): Lens<A, C> = composeLens(other)

    /**
     * Checks if the target of a [Lens] satisfies the predicate
     */
    inline fun exist(crossinline p: (B) -> Boolean): (A) -> Boolean = { p(get(it)) }

    /**
     * Find if the target satisfies the predicate
     */
    inline fun find(crossinline p: (B) -> Boolean): (A) -> Option<B> = {
        val a = get(it)
        if (p(a)) Option.Some(a) else Option.None
    }
}
