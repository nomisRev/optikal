package optikal.optics.laws

import io.kotlintest.properties.Gen
import io.kotlintest.properties.forAll
import kategory.Eq
import kategory.Law
import optikal.optics.Traversal

object TraversalLaws {

    inline fun <A, reified B> laws(traversal: Traversal<A, B>, aGen: Gen<A>, bGen: Gen<B>, funcGen: Gen<(B) -> B>, EQA: Eq<A>, EQB: Eq<B>): List<Law> = listOf(
            Law("Traversal Law: headOption", {}),
            Law("Traversal Law: get what you get", {}),
            Law("Traversal Law: set is idempotent", {}),
            Law("Traversal Law: modify identity is identity", {}),
            Law("Traversal Law: compose modify", {})
    )

//    fun <A, B> headOption(traversal: Traversal<A, B>, aGen: Gen<A>, EQA: Eq<A>): Unit =
//            forAll(aGen, { a ->
//                EQA.eqv(
//                        traversal.headOption(a),
//                        traversal.getAll(a).headOption
//                )
//            })

    fun <A, B> modifyGetAll(traversal: Traversal<A, B>): Unit = TODO()

    fun <A, B> setIdempotent(traversal: Traversal<A, B>): Unit = TODO()

    fun <A, B> modifyIdentity(traversal: Traversal<A, B>): Unit = TODO()

    fun <A, B> composeModify(traversal: Traversal<A, B>): Unit = TODO()

}