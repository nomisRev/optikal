package optikal.optics

import kategory.Applicative
import kategory.Either
import kategory.HK
import kategory.Monoid
import kategory.Option
import kategory.getOrElse
import kategory.identity
import kategory.none
import kategory.right

/**
 * A [Optional] can be seen as a pair of functions `getOption: (A) -> Option<B>` and `set: (B) -> (A) -> A`
 *
 * An [Optional] can also be defined as a weaker [Lens] and [Prism]
 *
 * @param A the source of a [Optional]
 * @param B the target of a [Optional]
 * @property getOption from an `A` we can extract a `Option<B?`
 * @property set replace the target value by `B` in an `A` so we obtain another modified `A`
 * @constructor Creates a Lens of type `A` with target `B`.
 */
abstract class Optional<A, B> {

    /** get the target of a [Optional] or nothing if there is no target */
    abstract fun getOption(a: A): Option<B>

    /** get the modified source of a [Optional] */
    abstract fun set(b: B): (A) -> (A)

    companion object {

        fun <A> id() = Iso.id<A>().asOptional()

        fun <A> codiagonal() = Optional<Either<A, A>, A>(
                getOption = { aa -> aa.fold({ a -> a.right() }, { a -> a.right() }).toOption() },
                set = { a -> { aa -> aa.bimap({ a }, { a }) } }
        )

        operator fun <A, B> invoke(getOption: (A) -> Option<B>, set: (B) -> (A) -> (A)) = object : Optional<A, B>() {
            override fun getOption(a: A): Option<B> = getOption(a)

            override fun set(b: B): (A) -> A = set(b)
        }

        fun <A, B> void() = Optional<A, B>(
                { _ -> none() },
                { _ -> ::identity }
        )

    }

    /** get the target of a [Optional] or return the original value while allowing the type to change if it does not match */
    fun getOrModify(a: A): Either<A, B> = getOption(a).fold({ Either.Left(a) }, { b -> Either.Right(b) })

    /** check if there is a target */
    fun nonEmpty(a: A): Boolean = getOption(a).isDefined

    /** modify polymorphically the target of a [Optional] with a function */
    inline fun modify(crossinline f: (B) -> B): (A) -> A = { a -> getOption(a).fold({ a }, { set(f(it))(a) }) }

    /** modify polymorphically the target of a [Optional] with an Applicative function */
    inline fun <reified F> modifyF(FA: Applicative<F> = kategory.applicative(), crossinline f: (B) -> HK<F, B>, a: A): HK<F, A> =
            getOrModify(a).fold(
                    FA::pure,
                    { FA.map(f(it), { b -> set(b)(a) }) }
            )

    /**
     * modify polymorphically the target of a [Optional] with a function.
     * return empty if the [Optional] is not matching
     */
    fun modifiyOption(f: (B) -> B): (A) -> Option<A> = { a -> getOption(a).map({ b -> set(f(b))(a) }) }

    /** compose a [Optional] with a [Optional] */
    infix fun <C> composeOptional(other: Optional<B, C>): Optional<A, C> = Optional(
            { a -> getOption(a).flatMap(other::getOption) },
            { c -> modify(other.set(c)) }
    )

    /** compose a [Optional] with a [Prism] */
    infix fun <C> composePrism(other: Prism<B, C>): Optional<A, C> = composeOptional(other.asOptional())

    /** compose a [Optional] with a [Lens] */
    infix fun <C> composeLens(other: Lens<B, C>): Optional<A, C> = composeOptional(other.asOptional())

    /**
     * Plus operator overload to compose optionals
     */
    operator fun <C> plus(o: Optional<B, C>): Optional<A, C> = composeOptional(o)

    operator fun <C> plus(o: Prism<B, C>): Optional<A, C> = composePrism(o)

    operator fun <C> plus(o: Lens<B, C>): Optional<A, C> = composeLens(o)

    fun asFold() = object : Fold<A, B>() {
        override fun <R> foldMapI(M: Monoid<R>, a: A, f: (B) -> R): R =
                getOption(a).map(f).getOrElse(M::empty)
    }

    /**
     * View a [Optional] as a [Traversal]
     */
    fun asTraversal() = object : Traversal<A, B>() {
        override fun <F> modifyFI(FA: Applicative<F>, f: (B) -> HK<F, B>, a: A): HK<F, A> = getOrModify(a).fold(
                FA::pure,
                { FA.map(f(it), { set(it)(a) }) }
        )
    }
}