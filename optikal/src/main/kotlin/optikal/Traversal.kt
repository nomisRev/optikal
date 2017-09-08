package optikal

import kategory.Applicative
import kategory.Const
import kategory.ConstHK
import kategory.HK
import kategory.applicative
import kategory.map
import kategory.value

abstract class Traversal<A, B> {

    /** Small work around for reified F. Cannot make internal because it would expose non-public API which is not allowed. */
    abstract fun <F> modifyFF(FA: Applicative<F>, f: (B) -> HK<F, B>, a: A): HK<F, A>

    /**
     * Modify polymorphically the target of a [Traversal] with an Applicative function all traversal methods are written in terms of modifyF
     */
    inline fun <reified F> modifyF(FA: Applicative<F> = applicative(), noinline f: (B) -> HK<F, B>, a: A): HK<F, A> = modifyFF(FA, f, a)

    companion object {
        operator fun <A, B> invoke(vararg lenses: Lens<A, B>) = object : Traversal<A, B>() {
            override fun <F> modifyFF(FA: Applicative<F>, f: (B) -> HK<F, B>, a: A): HK<F, A> = lenses.fold(FA.pure(a), { fs, lens ->
                FA.map(f(lens.get(a)), fs, { (b, a) ->
                    lens.set(b)(a)
                })
            })
        }
    }

    /**
     * Map each target to a Monoid and combine the results
     */
    inline fun <reified M> foldMap(FA: Applicative<HK<ConstHK, M>> = applicative(), crossinline f: (B) -> M, a: A): M = modifyF(FA, { b ->
        Const(f(b))
    }, a).value()

}