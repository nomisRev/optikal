package optikal.optics

import io.kotlintest.KTestJUnitRunner
import io.kotlintest.matchers.shouldBe
import io.kotlintest.specs.StringSpec
import kategory.IntMonoid
import kategory.ListKW
import kategory.ListKWKind
import kategory.k
import kategory.traverse
import org.junit.runner.RunWith

@RunWith(KTestJUnitRunner::class)
class TraversalSpec : StringSpec() {

    init {
        val eachL: Traversal<ListKWKind<Int>, Int> = Traversal.fromTraversable(ListKW.traverse())

        "modify" {
            eachL.modify { it + 1 }(listOf(1, 2, 3, 4).k()) shouldBe listOf(2, 3, 4, 5).k()
        }

        "fold" {
            eachL.fold(IntMonoid, listOf(1, 2, 3).k()) shouldBe 6
        }

        "length" {
            eachL.length(listOf(1,2,3,4).k()) shouldBe 4
        }

        "set" {
            eachL.set(5)(listOf(1,2,3,4).k()) shouldBe listOf(5,5,5,5).k()
        }

    }

}