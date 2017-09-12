package optikal.optics

import kategory.Either
import kategory.Foldable
import kategory.HK
import kategory.IntMonoid
import kategory.ListKW
import kategory.Monoid
import kategory.Option
import kategory.foldable
import kategory.identity
import kategory.left
import kategory.monoid
import kategory.none
import kategory.right
import kategory.some

/**
 * A [Fold] can be seen as a [Getter] with many targets or a weaker [PTraversal] which cannot modify its target.
 *
 * @param A the source of a [Fold]
 * @param B the target of a [Fold]
 */
abstract class Fold<A, B> {

    /**
     * Map each target to a Monoid and combine the results
     * underlying representation of [Fold], all [Fold] methods are defined in terms of foldMap
     */
    @PublishedApi internal abstract fun <R> foldMapI(M: Monoid<R>, a: A, f: (B) -> R): R

    inline fun <reified R> foldMap(M: Monoid<R> = monoid(), a: A, noinline f: (B) -> R): R = foldMapI(M, a, f)

    companion object {

        fun <A> id() = Iso.id<A>().asFold()

        inline fun <reified A> codiagonal() = object : Fold<Either<A, A>, A>() {
            override fun <R> foldMapI(M: Monoid<R>, a: Either<A, A>, f: (A) -> R): R = a.fold(f, f)
        }

        fun <A> select(p: (A) -> Boolean): Fold<A, A> = object : Fold<A, A>() {
            override fun <R> foldMapI(M: Monoid<R>, a: A, f: (A) -> R): R = if (p(a)) f(a) else M.empty()
        }

        /**
         * [Fold] that points to nothing
         */
        fun <A, B> void() = Optional.void<A, B>().asFold()

        /**
         * Create a [Fold] from a Foldable
         */
        inline fun <reified F, A> fromFoldable(Foldable: Foldable<F> = foldable()) = object : Fold<HK<F, A>, A>() {
            override fun <R> foldMapI(M: Monoid<R>, a: HK<F, A>, f: (A) -> R): R = Foldable.foldMap(M, a, f)
        }

    }

    /**
     * Calculate the number of targets
     */
    fun length(a: A) = foldMap(IntMonoid, a, { _ -> 1 })

    /**
     * Find the first target matching the predicate
     */
    fun find(p: (B) -> Boolean): (A) -> Option<B> = { a ->
        foldMap(M = firstOptionMonoid<B>(), a = a, f = { b -> (if (p(b)) b.some() else none()).tag() }).unwrap()
    }

    /**
     * Get the first target
     */
    fun headOption(a: A): Option<B> = foldMap(firstOptionMonoid<B>(), a, { b -> b.some().tag() }).unwrap()

    /**
     * Get the last target
     */
    fun lastOption(a: A): Option<B> = foldMap(lastOptionMonoid<B>(), a, { b -> b.some().tag() }).unwrap()

    /**
     * Check if all targets satisfy the predicate
     */
    fun all(p: (B) -> Boolean): (A) -> Boolean = { a -> foldMap(addMonoid, a, p) }

    /**
     * Check if there is no target
     */
    fun isEmpty(a: A): Boolean = foldMap(addMonoid, a, { _ -> false })

    /**
     * Check if there is at least one target
     */
    fun nonEmpty(a: A): Boolean = !isEmpty(a)

    /**
     * Join two [Fold] with the same target
     */
    fun <C> choice(other: Fold<C, B>): Fold<Either<A, C>, B> = object : Fold<Either<A, C>, B>() {
        override fun <R> foldMapI(M: Monoid<R>, a: Either<A, C>, f: (B) -> R): R =
                a.fold({ ac -> this@Fold.foldMapI(M, ac, f) }, { c -> other.foldMapI(M, c, f) })
    }

    fun <C> left(): Fold<Either<A, C>, Either<B, C>> = object : Fold<Either<A, C>, Either<B, C>>() {
        override fun <R> foldMapI(M: Monoid<R>, a: Either<A, C>, f: (Either<B, C>) -> R): R =
                a.fold({ a1: A -> this@Fold.foldMapI(M, a1, { b -> f(b.left()) }) }, { c -> f(c.right()) })
    }

    fun <C> right(): Fold<Either<C, A>, Either<C, B>> = object : Fold<Either<C, A>, Either<C, B>>() {
        override fun <R> foldMapI(M: Monoid<R>, a: Either<C, A>, f: (Either<C, B>) -> R): R =
                a.fold({ c ->  f(c.left()) }, { a1 -> this@Fold.foldMapI(M, a1, { b -> f(b.right()) }) })
    }

    /**
     * Compose a [Fold] with a [Fold]
     */
    infix fun <C> composeFold(other: Fold<B, C>): Fold<A, C> = object : Fold<A, C>() {
        override fun <R> foldMapI(M: Monoid<R>, a: A, f: (C) -> R): R = this@Fold.foldMapI(M, a, { b -> other.foldMapI(M, b, f) })
    }

    /**
     * Compose a [Fold] with a [Getter]
     */
    infix fun <C> composeGetter(other: Getter<B, C>): Fold<A, C> = composeFold(other.asFold())

    /**
     * Compose a [Fold] with a [Traversal]
     */
    //    infix fun <C> composeTraversal(other: Traversal<A,B>): Fold<A,B> = TODO figure out a way to transform Traversal to Fold.

    /**
     * Compose a [Fold] with a [Optional]
     */
    infix fun <C> composeOptional(other: Optional<B, C>): Fold<A, C> = composeFold(other.asFold())

    /**
     * Compose a [[Fold]] with a [Prism]
     */
    infix fun <C> composePrism(other: Prism<B, C>): Fold<A, C> = composeFold(other.asFold())

    /**
     * Compose a [Fold] with a [Lens]
     */
    infix fun <C> composeLens(other: Lens<B, C>): Fold<A, C> = composeFold(other.asFold())

    /**
     * Compose a [Fold] with a [Iso]
     */
    infix fun <C> composeIso(other: Iso<B, C>): Fold<A, C> = composeFold(other.asFold())

    /**
     * Plus operator overload to compose lenses
     */
    operator fun <C> plus(other: Fold<B, C>): Fold<A, C> = composeFold(other)

    operator fun <C> plus(other: Optional<B, C>): Fold<A, C> = composeOptional(other)

    operator fun <C> plus(other: Getter<B, C>): Fold<A, C> = composeGetter(other)

    operator fun <C> plus(other: Prism<B, C>): Fold<A, C> = composePrism(other)

    operator fun <C> plus(other: Lens<B, C>): Fold<A, C> = composeLens(other)

    operator fun <C> plus(other: Iso<B, C>): Fold<A, C> = composeIso(other)
}

inline fun <A, reified B> Fold<A, B>.fold(M: Monoid<B> = monoid(), a: A): B = foldMapI(M, a, ::identity)

inline fun <A, reified B> Fold<A, B>.getAll(M: Monoid<ListKW<B>> = monoid(), a: A): ListKW<B> = foldMapI(M, a, { ListKW.pure(it) })

//TODO move addMonoid to kategory
private val addMonoid = object : Monoid<Boolean> {
    override fun combine(a: Boolean, b: Boolean): Boolean = a && b

    override fun empty(): Boolean = true
}