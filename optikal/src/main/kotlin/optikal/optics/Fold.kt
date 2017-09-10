package optikal.optics

import kategory.Either
import kategory.Foldable
import kategory.HK
import kategory.IntMonoid
import kategory.ListKW
import kategory.Monoid
import kategory.foldable
import kategory.identity
import kategory.left
import kategory.monoid
import kategory.right

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
    abstract fun <R> foldMap(M: Monoid<R>, a: A, f: (B) -> R): R

    companion object {

        fun <A> id() = Iso.id<A>().asFold()

        inline fun <reified A> codiagonal() = object : Fold<Either<A, A>, A>() {
            override fun <R> foldMap(M: Monoid<R>, a: Either<A, A>, f: (A) -> R): R = a.fold(f, f)
        }

        fun <A> select(p: (A) -> Boolean): Fold<A, A> = object : Fold<A, A>() {
            override fun <R> foldMap(M: Monoid<R>, a: A, f: (A) -> R): R =
                    if (p(a)) f(a) else M.empty()
        }

        /**
         * [Fold] that points to nothing
         */
        fun <A, B> void() = Optional.void<A, B>().asFold()

        /**
         * Create a [Fold] from a Foldable
         */
        inline fun <reified F, A> fromFoldable(Foldable: Foldable<F> = foldable()) = object : Fold<HK<F, A>, A>() {
            override fun <R> foldMap(M: Monoid<R>, a: HK<F, A>, f: (A) -> R): R = Foldable.foldMap(M, a, f)
        }


    }

    /**
     * Compose a [Fold] with a [Fold]
     */
    infix inline fun <reified C> composeFold(other: Fold<B, C>): Fold<A, C> = object : Fold<A, C>() {
        override fun <R> foldMap(M: Monoid<R>, a: A, f: (C) -> R): R =
                this@Fold.foldMap(M, a, { b -> other.foldMap(M, b, { f(it) }) })
    }

    /**
     * Calculate the number of targets
     */
    fun length(a: A) = foldMap(IntMonoid, a, { 1 })

    /**
     * Join two [Fold] with the same target
     */
    fun <C> choice(other: Fold<C, B>): Fold<Either<A, C>, B> = object : Fold<Either<A, C>, B>() {
        override fun <R> foldMap(M: Monoid<R>, a: Either<A, C>, f: (B) -> R): R =
                a.fold({ this@Fold.foldMap(M, it, f) }, { other.foldMap(M, it, f) })
    }

    fun <C> left(): Fold<Either<A, C>, Either<B, C>> = object : Fold<Either<A, C>, Either<B, C>>() {
        override fun <R> foldMap(M: Monoid<R>, a: Either<A, C>, f: (Either<B, C>) -> R): R =
                a.fold({ a1: A -> this@Fold.foldMap(M, a1, { b -> f(b.left()) }) }, { c -> f(c.right()) })
    }

    fun <C> right(): Fold<Either<C, A>, Either<C, B>> = object : Fold<Either<C, A>, Either<C, B>>() {
        override fun <R> foldMap(M: Monoid<R>, a: Either<C, A>, f: (Either<C, B>) -> R): R =
                a.fold({ c -> f(c.left()) }, { a1 -> this@Fold.foldMap(M, a1, { b -> f(b.right()) }) })
    }

}

inline fun <A, reified B> Fold<A, B>.fold(M: Monoid<B> = monoid(), a: A): B = foldMap(M, a, ::identity)

inline fun <A, reified B> Fold<A, B>.getAll(M: Monoid<ListKW<B>> = monoid(), a: A): ListKW<B> = foldMap(M, a, { ListKW.pure(it) })
