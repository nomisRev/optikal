package optikal.optics

import kategory.Applicative
import kategory.Const
import kategory.ConstHK
import kategory.Either
import kategory.HK
import kategory.Id
import kategory.IntMonoid
import kategory.Monoid
import kategory.Option
import kategory.applicative
import kategory.identity
import kategory.left
import kategory.map
import kategory.monoid
import kategory.right
import kategory.traverse
import kategory.value

abstract class Traversal<A, B> {

    /** Small work around for reified F. Cannot make internal because it would expose non-public API which is not allowed. */
    @PublishedApi internal abstract fun <F> modifyFI(FA: Applicative<F>, f: (B) -> HK<F, B>, a: A): HK<F, A>

    /**
     * Modify polymorphically the target of a [Traversal] with an Applicative function all traversal methods are written in terms of modifyF
     */
    inline fun <reified F> modifyF(FA: Applicative<F> = applicative(), a: A, crossinline f: (B) -> HK<F, B>): HK<F, A> = modifyFI(FA, { f(it) }, a)

    companion object {
        fun <A> id() = Iso.id<A>().asTraversal()

        fun <A> codiagonal() = object : Traversal<Either<A, A>, A>() {
            override fun <F> modifyFI(FA: Applicative<F>, f: (A) -> HK<F, A>, a: Either<A, A>): HK<F, Either<A, A>> =
                    a.bimap(f, f).fold({ FA.map(it, { it.left() }) }, { FA.map(it, { it.right() }) })
        }

        inline fun <reified T, A> fromTraversable(TT: kategory.Traverse<T> = traverse()) = object : Traversal<HK<T, A>, A>() {
            override fun <F> modifyFI(FA: Applicative<F>, f: (A) -> HK<F, A>, a: HK<T, A>): HK<F, HK<T, A>> = TT.traverse(a, f, FA)
        }

        /**
         * [Traversal] that points to nothing
         */
        fun <A, B> void() = Optional.void<A, B>().asTraversal()

        operator fun <A, B> invoke(vararg lenses: Lens<A, B>) = object : Traversal<A, B>() {
            override fun <F> modifyFI(FA: Applicative<F>, f: (B) -> HK<F, B>, a: A): HK<F, A> = lenses.fold(FA.pure(a), { fs, lens ->
                FA.map(f(lens.get(a)), fs, { (b, a) ->
                    lens.set(b)(a)
                })
            })
        }

        operator fun <A, B> invoke(get1: (A) -> B, get2: (A) -> B, set: (B, B, A) -> A): Traversal<A, B> = object : Traversal<A, B>() {
            override fun <F> modifyFI(FA: Applicative<F>, f: (B) -> HK<F, B>, a: A): HK<F, A> =
                    FA.map2(f(get1(a)), f(get2(a)), { (b1, b2) -> set(b1, b2, a) })
        }

    }

    /**
     * Map each target to a Monoid and combine the results
     */
    @Suppress("UNUSED_PARAMETER")
    inline fun <reified R> foldMap(FA: Applicative<HK<ConstHK, R>> = applicative(), M: Monoid<R> = monoid(), crossinline f: (B) -> R, a: A): R = modifyF(FA, a, { b ->
        Const(f(b))
    }).value()

    /**
     * Modify polymorphically the target of a [Traversal] with a function
     */
    inline fun modify(crossinline f: (B) -> B): (A) -> A = { a ->
        modifyF(Id.applicative(), a, { Id(f(it)) }).value()
    }

    /**
     * Set polymorphically the target of a [Traversal] with a value
     */
    fun set(b: B): (A) -> A = modify { b }

    /**
     * Calculate the number of targets
     */
    fun length(a: A): Int = foldMap(Const.applicative(), IntMonoid, { 1 }, a)

    /**
     * Compose a [Traversal] with a [Traversal]
     */
    infix fun <C> composeTraversal(other: Traversal<B, C>): Traversal<A, C> = object : Traversal<A, C>() {
        override fun <F> modifyFI(FA: Applicative<F>, f: (C) -> HK<F, C>, a: A): HK<F, A> =
                this@Traversal.modifyFI(FA, { b -> other.modifyFI(FA, f, b) }, a)
    }
}

/**
 * Find the first target matching the predicate
 */
inline fun <reified A, reified B> Traversal<A, B>.find(crossinline p: (B) -> Boolean): (A) -> Option<B> = { a: A ->
    foldMap(Const.applicative(), firstOptionMonoid<B>(), { b ->
        (if (p(b)) Option.Some(b) else Option.None).tag()
    }, a).unwrap()
}

inline fun <reified A, reified B> Traversal<A, B>.fold(M: Monoid<B> = monoid(), a: A): B =
        foldMap(Const.applicative(), M, ::identity, a)
