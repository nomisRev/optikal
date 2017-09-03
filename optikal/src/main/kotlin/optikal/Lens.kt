package optikal

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
annotation class Lenses

data class Lens<A, B>(val get: (A) -> B, val set: (B) -> (A) -> A) {

    fun modify(f: (B) -> B, a: A) = set(f(get(a)))(a)

    infix fun <C> composeLens(l: Lens<B, C>): Lens<A, C> = Lens(
            get = { a -> l.get(get(a)) },
            set = { c -> { a -> set(l.set(c)(get(a)))(a) } }
    )

}