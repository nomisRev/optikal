package optikal.optics

import kategory.Applicative
import kategory.Const
import kategory.ConstHK
import kategory.HK
import kategory.Id
import kategory.IntMonoid
import kategory.Monoid
import kategory.Option
import kategory.applicative
import kategory.identity
import kategory.map
import kategory.monoid
import kategory.value

abstract class Traversal<A, B> {

    /** Small work around for reified F. Cannot make internal because it would expose non-public API which is not allowed. */
    abstract fun <F> modifyFF(FA: Applicative<F>, f: (B) -> HK<F, B>, a: A): HK<F, A>

    /**
     * Modify polymorphically the target of a [Traversal] with an Applicative function all traversal methods are written in terms of modifyF
     */
    inline fun <reified F> modifyF(FA: Applicative<F> = applicative(), a: A, crossinline f: (B) -> HK<F, B>): HK<F, A> = modifyFF(FA, { f(it) }, a)

    companion object {
        operator fun <A, B> invoke(vararg lenses: Lens<A, B>) = object : Traversal<A, B>() {
            override fun <F> modifyFF(FA: Applicative<F>, f: (B) -> HK<F, B>, a: A): HK<F, A> = lenses.fold(FA.pure(a), { fs, lens ->
                FA.map(f(lens.get(a)), fs, { (b, a) ->
                    lens.set(b)(a)
                })
            })
        }

        inline fun <reified T, A> fromTraversable(TT: kategory.Traverse<T>) = object : Traversal<HK<T, A>, A>() {
            override fun <F> modifyFF(FA: Applicative<F>, f: (A) -> HK<F, A>, a: HK<T, A>): HK<F, HK<T, A>> = TT.traverse(a, f, FA)
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