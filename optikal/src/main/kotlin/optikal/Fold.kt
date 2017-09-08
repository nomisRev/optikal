package optikal

import kategory.Monoid
import kategory.getOrElse
import kategory.identity
import kategory.monoid

abstract class Fold<A, B> {

    /**
     * map each target to a Monoid and combine the results
     * underlying representation of [Fold], all [Fold] methods are defined in terms of foldMap
     */
    abstract fun <R> foldMap(M: Monoid<R>, f: (B) -> R, a: A): R

}

inline fun <A, reified B> Fold<A, B>.fold(M: Monoid<B> = monoid(), a: A): B = foldMap(M, ::identity, a)

fun <A, B> Prism<A, B>.asFold(): Fold<A, B> = object : Fold<A, B>() {
    override fun <R> foldMap(M: Monoid<R>, f: (B) -> R, a: A): R =
            getOption(a).map(f).getOrElse { M.empty() }
}