package optikal.optics

import kategory.Either
import kategory.Monoid
import kategory.Option
import kategory.Tuple2
import kategory.compose
import kategory.identity
import kategory.none
import kategory.some
import kategory.toT

/**
 * A [Getter] can be seen as a glorified get method between a type A and a type B.
 *
 * @param A the source of a [Getter]
 * @param B the target of a [Getter]
 */
abstract class Getter<A, B> {
    /**
     * Get the target of a [Getter]
     */
    abstract fun get(a: A): B

    companion object {

        fun <A> id() = Iso.id<A>().asGetter()

        fun <A> codiagonal(): Getter<Either<A, A>, A> = Getter { it.fold(::identity, ::identity) }

        operator fun <A, B> invoke(_get: (A) -> B) = object : Getter<A, B>() {
            override fun get(a: A): B = _get(a)
        }
    }

    /**
     * Find if the target satisfies the predicate.
     */
    inline fun find(crossinline p: (B) -> Boolean): (A) -> Option<B> = { a ->
        get(a).let {
            if (p(it)) it.some() else none()
        }
    }

    /**
     * Check if the target satisfies the predicate
     */
    fun exist(p: (B) -> Boolean): (A) -> Boolean = p compose this::get

    /**
     * join two [Getter] with the same target
     */
    fun <C> choice(other: Getter<C, B>): Getter<Either<A, C>, B> = Getter { a ->
        a.fold(this::get, other::get)
    }

    /**
     * Pair two disjoint [Getter]
     */
    fun <C, D> split(other: Getter<C, D>): Getter<Tuple2<A, C>, Tuple2<B, D>> = Getter { (a, c) ->
        get(a) toT other.get(c)
    }

    fun <C> zip(other: Getter<A, C>): Getter<A, Tuple2<B, C>> = Getter { a ->
        get(a) toT other.get(a)
    }

    fun <C> first(): Getter<Tuple2<A, C>, Tuple2<B, C>> = Getter { (a, c) ->
        get(a) toT c
    }

    fun <C> second(): Getter<Tuple2<C, A>, Tuple2<C, B>> = Getter { (c, a) ->
        c toT get(a)
    }

    fun <C> left(): Getter<Either<A, C>, Either<B, C>> = Getter {
        it.bimap(this::get, ::identity)
    }

    fun <C> right(): Getter<Either<C, A>, Either<C, B>> = Getter {
        it.map(this::get)
    }

    /**
     * Compose a [Getter] with a [Getter]
     */
    infix fun <C> composeGetter(other: Getter<B, C>): Getter<A, C> = Getter { a ->
        other.get(get(a))
    }

    operator fun <C> plus(other: Getter<B, C>): Getter<A, C> = composeGetter(other)

    fun asFold() = object : Fold<A, B>() {
        override fun <R> foldMapI(M: Monoid<R>, a: A, f: (B) -> R): R = f(get(a))
    }

}
