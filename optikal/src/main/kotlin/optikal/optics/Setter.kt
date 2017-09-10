package optikal.optics

import kategory.Either
import kategory.Functor
import kategory.HK
import kategory.functor

/**
 * A [[Setter]] is a generalisation of Functor map:
 *  - `map:    (A => B) => F[A] => F[B]`
 *  - `modify: (A => B) => S    => T`
 *
 * @param A the source of a [Setter]
 * @param B the target of a [Setter]
 */
abstract class Setter<A, B> {

    /**
     * modify polymorphically the target of a [Setter] with a function
     */
    abstract fun modify(f: (B) -> B): (A) -> A

    /**
     * Set polymorphically the target of a [Setter] with a value
     */
    abstract fun set(b: B): (A) -> A

    companion object {

        fun <A> id() = Iso.id<A>().asSetter()

        fun <A> codiagonal(): Setter<Either<A, A>, A> = Setter { f -> { it.bimap(f, f) } }

        /**
         * create a [Setter] using modify function
         */
        operator fun <A, B> invoke(modify: ((B) -> B) -> (A) -> A): Setter<A, B> = object : Setter<A, B>() {
            override fun modify(f: (B) -> B): (A) -> A = modify(f)

            override fun set(b: B): (A) -> A = modify { b }
        }

        /**
         * create a [Setter] from a Functor
         */
        inline fun <A, reified F> fromFunctor(FF: Functor<F> = functor()): Setter<HK<F, A>, A> = Setter { f ->
            { fa -> FF.map(fa, f) }
        }
    }

    /**
     * join two [Setter] with the same target
     */
    fun <C> choice(other: Setter<C, B>): Setter<Either<A, C>, B> = Setter { f ->
        { it.bimap(modify(f), other.modify(f)) }
    }

    /**
     * compose a [Setter] with a [Setter]
     */
    infix fun <C> composeSetter(other: Setter<B, C>): Setter<A, C> = Setter { fb ->
        modify(other.modify(fb))
    }

}