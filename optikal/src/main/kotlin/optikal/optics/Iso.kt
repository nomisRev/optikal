package optikal.optics

import kategory.*

/**
 * An [Iso] defines an isomorphism between a type S and A:
 * <pre>
 *             get
 *     -------------------->
 *   S                       A
 *     <--------------------
 *          reverseGet
 * </pre>
 * A [Iso] is also a valid [Getter], [Fold], [Lens], [Prism], [Optional], [Traversal] and [Setter]
 *
 * @param A the source of a [Iso]
 * @param B the target of a [Iso]
 */
abstract class Iso<A, B> {

    /** get the target of a [ISO] */
    abstract fun get(a: A): B

    /** get the modified source of a [ISO] */
    abstract fun reverseGet(b: B): A

    companion object {

        /**
         * create an [Iso] between any type and itself. id is the zero element of optics composition, for all optics o of type O (e.g. Lens, Iso, Prism, ...):
         * o      composeIso Iso.id == o
         * Iso.id composeO   o        == o (replace composeO by composeLens, composeIso, composePrism, ...)
         */
        fun <A> id() = Iso<A, A>(::identity, ::identity)

        operator fun <A, B> invoke(get: (A) -> (B), reverseGet: (B) -> A) = object : Iso<A, B>() {

            override fun get(a: A): B = get(a)

            override fun reverseGet(b: B): A = reverseGet(b)
        }

    }

    /** reverse a [Iso]: the source becomes the target and the target becomes the source */
    fun reverse(): Iso<B, A> = object : Iso<B, A>() {
        /** get the target of a [Iso] */
        override fun get(a: B): A = reverseGet(a)

        /** get the modified source of a [Iso] */
        override fun reverseGet(b: A): B = get(b)
    }

    /** modify polymorphically the target of a [Iso] with a function */
    inline fun modify(crossinline f: (B) -> B): (A) -> A = { a -> reverseGet(f(get(a))) }

    /** modify polymorphically the target of a [Iso] with a Functor function */
    inline fun <reified F> modifyF(FF: Functor<F> = functor(), f: (B) -> HK<F, B>, a: A): HK<F, A> =
            FF.map(f(get(a)), this::reverseGet)

    /** set polymorphically the target of a [Iso] with a value */
    fun set(b: B): (A) -> (A) = { reverseGet(b) }

    /** compose a [Iso] with a [Iso] */
    infix fun <C> composeIso(other: Iso<B, C>): Iso<A, C> = Iso(
            { a -> other.get(get(a)) },
            this::reverseGet compose other::reverseGet
    )

    /** view a [Iso] as a [Optional] */
    fun asOptional(): Optional<A, B> = Optional(
            { a -> Option.Some(get(a)) },
            this::set
    )

    /** view a [Iso] as a [Prism] */
    fun asPrism(): Prism<A, B> = Prism(
            { a -> Either.Right(get(a)) },
            this::reverseGet
    )

    /** view a [Iso] as a [Lens] */
    fun asLens(): Lens<A, B> = Lens(this::get, this::set)

    /**
     * view a [Iso] as a [Getter]
     */
    fun asGetter(): Getter<A, B> = Getter(this::get)

    /**
     * View an [Iso] as a [Setter]
     */
    fun asSetter(): Setter<A, B> = Setter(this::modify)

    /**
     * View a [Iso] as a [Traversal]
     */
    fun asTraversal(): Traversal<A, B> = object : Traversal<A, B>() {
        override fun <F> modifyFI(FA: Applicative<F>, f: (B) -> HK<F, B>, a: A): HK<F, A> =
                FA.map(f(get(a)), this@Iso::reverseGet)
    }

    /**
     * View a [Iso] as a [Fold]
     */
    fun asFold(): Fold<A, B> = object : Fold<A, B>() {
        override fun <R> foldMapI(M: Monoid<R>, a: A, f: (B) -> R): R = f(get(a))
    }
}