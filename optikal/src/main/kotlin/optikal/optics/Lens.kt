package optikal.optics

import kategory.Applicative
import kategory.Either
import kategory.Functor
import kategory.HK
import kategory.Monoid
import kategory.Option
import kategory.functor
import kategory.identity
import kategory.none
import kategory.some

/**
 * A [Lens] can be seen as a pair of functions `get: (A) -> B` and `set: (B) -> (A) -> A`
 * - `get: (A) -> B` i.e. from an `A`, we can extract an `B`
 * - `set: (B) -> (A) -> A` i.e. if we replace target value by `B` in an `A`, we obtain another modified `A`
 *
 * @param A the source of a [Lens]
 * @param B the target of a [Lens]
 * @property get from an `A` we can extract a `B`
 * @property set replace the target value by `B` in an `A` so we obtain another modified `A`
 * @constructor Creates a Lens of type `A` with target `B`.
 */
abstract class Lens<A, B> {

    abstract fun get(a: A): B
    abstract fun set(b: B): (A) -> A

    companion object {

        fun <A> id() = Iso.id<A>().asLens()

        fun <A> codiagonal() = Lens<Either<A, A>, A>(
                get = { it.fold(::identity, ::identity) },
                set = { a -> { it.bimap({ a }, { a }) } }
        )

        operator fun <A, B> invoke(get: (A) -> B, set: (B) -> (A) -> A) = object : Lens<A, B>() {
            override fun get(a: A): B = get(a)

            override fun set(b: B): (A) -> A = set(b)
        }
    }

    /**
     * Modify the target of a [Lens] using a function `(B) -> B`
     */
    inline fun modify(f: (B) -> B, a: A): A = set(f(get(a)))(a)

    /**
     * Modify the target of a [Lens] using Functor function
     */
    inline fun <reified F> modifyF(FF: Functor<F> = functor(), f: (B) -> HK<F, B>, a: A): HK<F, A> =
            FF.map(f(get(a)), { b -> set(b)(a) })

    /**
     * Find if the target satisfies the predicate
     */
    inline fun find(crossinline p: (B) -> Boolean): (A) -> Option<B> = {
        get(it).let { a ->
            if (p(a)) a.some() else none()
        }
    }

    /**
     * Checks if the target of a [Lens] satisfies the predicate
     */
    inline fun exist(crossinline p: (B) -> Boolean): (A) -> Boolean = { p(get(it)) }

    /**
     * Join two [Lens] with the same target
     */
    fun <C> choice(other: Lens<C, B>): Lens<Either<A, C>, B> = Lens(
            { it.fold(this::get, other::get) },
            { b -> { it.bimap(set(b), other.set(b)) } }
    )

    /**
     * Pair two disjoint [Lens]
     */
    fun <C, D> split(other: Lens<C, D>): Lens<Pair<A, C>, Pair<B, D>> = Lens(
            { (a, c) -> get(a) to other.get(c) },
            { (b, d) -> { (a, c) -> set(b)(a) to other.set(d)(c) } }
    )

    /**
     * Convenience method to create a pair of the target and a type C
     */
    fun <C> first(): Lens<Pair<A, C>, Pair<B, C>> = Lens(
            { (a, c) -> get(a) to c },
            { (b, c) -> { (a, _) -> set(b)(a) to c } }
    )

    /**
     * Convenience method to create a pair of a type C and the target
     */
    fun <C> second(): Lens<Pair<C, A>, Pair<C, B>> = Lens(
            { (c, a) -> c to get(a) },
            { (c, b) -> { (_, a) -> c to set(b)(a) } }
    )

    /**
     * Compose a [Lens] with another [Lens]
     */
    infix fun <C> composeLens(l: Lens<B, C>): Lens<A, C> = Lens(
            { a -> l.get(get(a)) },
            { c -> { a -> set(l.set(c)(get(a)))(a) } }
    )

    /** compose a [Lens] with a [Optional] */
    infix fun <C> composeOptional(other: Optional<B, C>): Optional<A, C> =
            asOptional() composeOptional other

    /**
     * Compose a [Lens] with a [Getter]
     */
    infix fun <C> composeGetter(other: Getter<B, C>): Getter<A, C> =
            asGetter() composeGetter other

    /**
     * Plus operator overload to compose lenses
     */
    operator fun <C> plus(other: Lens<B, C>): Lens<A, C> = composeLens(other)

    operator fun <C> plus(other: Optional<B, C>): Optional<A, C> = composeOptional(other)

    operator fun <C> plus(other: Getter<B, C>): Getter<A, C> = composeGetter(other)

    /**
     * View a [Lens] as an [Optional]
     */
    fun asOptional(): Optional<A, B> = Optional(
            { a -> Option.Some(get(a)) },
            this::set
    )

    /**
     * View a [Lens] as a [Getter]
     */
    fun asGetter(): Getter<A, B> = Getter(this::get)

    /**
     * View a [Lens] as a [Fold]
     */
    fun asFold() = object : Fold<A, B>() {
        override fun <R> foldMapI(M: Monoid<R>, a: A, f: (B) -> R): R = f(get(a))
    }

    /**
     * View a [Lens] as a [Traversal]
     */
    fun asTraversal(): Traversal<A, B> = object : Traversal<A, B>() {
        override fun <F> modifyFI(FA: Applicative<F>, f: (B) -> HK<F, B>, a: A): HK<F, A> =
                FA.map(f(get(a)), { set(it)(a) })
    }

}
