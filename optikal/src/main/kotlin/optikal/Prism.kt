package optikal

import kategory.Applicative
import kategory.Option
import kategory.Either
import kategory.HK
import kategory.Monoid
import kategory.applicative
import kategory.flatMap
import kategory.getOrElse
import kategory.identity
import kategory.left
import kategory.right

/**
 * A [Prism] can be seen as a pair of functions: `reverseGet : B -> A` and `getOrModify: A -> Either<A,B>`
 *
 * - `reverseGet : B -> A` get the source type of a [Prism]
 * - `getOrModify: A -> Either<A,B>` get the target of a [Prism] or return the original value
 *
 * It encodes the relation between a Sum or CoProduct type (sealed class) and one of its element.
 *
 * @param A the source of a [Prism]
 * @param B the target of a [Prism]
 * @property getOrModify from an `B` we can produce an `A`
 * @property reverseGet get the target of a [Prism] or return the original value
 * @constructor Creates a Lens of type `A` with target `B`
 */
abstract class Prism<A, B> {

    abstract fun getOrModify(a: A): Either<A, B>
    abstract fun reverseGet(b: B): A

    companion object {
        operator fun <A, B> invoke(getOrModify: (A) -> Either<A, B>, reverseGet: (B) -> A) = object : Prism<A, B>() {
            override fun getOrModify(a: A): Either<A, B> = getOrModify(a)

            override fun reverseGet(b: B): A = reverseGet(b)
        }
    }

    /**
     * Get the target or nothing if `A` does not match the target
     */
    fun getOption(a: A): Option<B> = getOrModify(a).toOption()

    /**
     * Modify the target of a [Prism] with an Applicative function
     */
    inline fun <reified F> modifyF(FA: Applicative<F> = applicative(), crossinline f: (B) -> HK<F, B>, a: A): HK<F, A> = getOrModify(a).fold(
            { FA.pure(it) },
            { FA.map(f(it), this::reverseGet) }
    )

    /**
     * Modify the target of a [Prism] with a function
     */
    inline fun modify(crossinline f: (B) -> B): (A) -> A = {
        getOrModify(it).fold(::identity, { reverseGet(f(it)) })
    }

    /**
     * Modify the target of a [Prism] with a function
     */
    inline fun modifyOption(crossinline f: (B) -> B): (A) -> Option<A> = { getOption(it).map { b -> reverseGet(f(b)) } }

    /**
     * Set the target of a [Prism] with a value
     */
    fun set(b: B): (A) -> A = modify { b }

    infix fun <C> composePrism(other: Prism<B, C>): Prism<A, C> = Prism(
            { a -> getOrModify(a).flatMap { b: B -> other.getOrModify(b).bimap({ set(it)(a) }, ::identity) } },
            { reverseGet(other.reverseGet(it)) }
    )

    /** compose a [Prism] with a [Optional] */
    infix fun <C> composeOptional(other: Optional<B, C>): Optional<A, C> =
            asOptional() composeOptional other

    /**
     * Set the target of a [Prism] with a value
     */
    fun setOption(b: B): (A) -> Option<A> = modifyOption { b }

    /**
     * Check if there is no target
     */
    fun isEmpty(a: A): Boolean = getOption(a).isDefined

    /**
     * Check if there is a target
     */
    fun nonEmpty(a: A): Boolean = getOption(a).nonEmpty

    /**
     * Find if the target satisfies the predicate
     */
    inline fun find(crossinline p: (B) -> Boolean): (A) -> Option<B> = { getOption(it).flatMap { if (p(it)) Option.Some(it) else Option.None } }

    /**
     * Check if there is a target and it satisfies the predicate
     */
    inline fun exist(crossinline p: (B) -> Boolean): (A) -> Boolean = { getOption(it).fold({ false }, p) }

    /**
     * Check if there is no target or the target satisfies the predicate
     */
    inline fun all(crossinline p: (B) -> Boolean): (A) -> Boolean = { getOption(it).fold({ true }, p) }

    /**
     * Convenience method to create a sum of the target and a type C
     */
    fun <C> first(): Prism<Pair<A, C>, Pair<B, C>> = Prism(
            { (a, c) -> getOrModify(a).bimap({ it to c }, { it to c }) },
            { (b, c) -> reverseGet(b) to c }
    )

    /**
     * Convenience method to create a sum of a type C and the target
     */
    fun <C> second(): Prism<Pair<C, A>, Pair<C, B>> = Prism(
            { (c, a) -> getOrModify(a).bimap({ c to it }, { c to it }) },
            { (c, b) -> c to reverseGet(b) }
    )

    /**
     * View a [Prism] as an [Optional]
     */
    fun asOptional(): Optional<A, B> = Optional(
            { a -> getOption(a) },
            { b -> set(b) }
    )

    fun asFold(): Fold<A, B> = object : Fold<A, B>() {
        override fun <R> foldMap(M: Monoid<R>, a: A, f: (B) -> R): R =
                getOption(a).map(f).getOrElse { M.empty() }
    }

}

/**
 * Convenience method to create a product of the target and a type C
 */
fun <A, B, C> Prism<A, B>.left(): Prism<Either<A, C>, Either<B, C>> = Prism(
        { it.fold({ a -> getOrModify(a).bimap({ it.left() }, { it.left() }) }, { c -> Either.Right(c.right()) }) },
        {
            when (it) {
                is Either.Left<B, C> -> Either.Left(reverseGet(it.a))
                is Either.Right<B, C> -> Either.Right(it.b)
            }
        }
)

/**
 * Convenience method to create a product of a type C and the target
 */
fun <A, B, C> Prism<A, B>.right(): Prism<Either<C, A>, Either<C, B>> = Prism(
        { it.fold({ c -> Either.Right(c.left()) }, { a -> getOrModify(a).bimap({ it.right() }, { it.right() }) }) },
        { it.map(this::reverseGet) }
)
