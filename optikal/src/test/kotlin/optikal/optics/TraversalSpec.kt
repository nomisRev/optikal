package optikal.optics

import io.kotlintest.KTestJUnitRunner
import io.kotlintest.matchers.shouldBe
import kategory.IntMonoid
import kategory.ListKWKind
import kategory.UnitSpec
import kategory.k
import org.junit.runner.RunWith

@RunWith(KTestJUnitRunner::class)
class TraversalSpec : UnitSpec() {

    init {
        val eachL: Traversal<ListKWKind<Int>, Int> = Traversal.fromTraversable()

        "modify" {
            eachL.modify { it + 1 }(listOf(1, 2, 3, 4).k()) shouldBe listOf(2, 3, 4, 5).k()
        }

        "fold" {
            eachL.fold(IntMonoid, listOf(1, 2, 3).k()) shouldBe 6
        }

        //WTF!? java.lang.ClassNotFoundException: optikal.optics.TaggedMonoidInstanceImplicits
//        "find" {
//            eachL.find { it > 2 }(listOf(1,2,3,4).k()) shouldBe 3.some()
//            eachL.find { it > 9 }(listOf(1,2,3,4).k()) shouldBe none<Int>()
//        }

        "length" {
            eachL.length(listOf(1, 2, 3, 4).k()) shouldBe 4
        }

        "set" {
            eachL.set(5)(listOf(1, 2, 3, 4).k()) shouldBe listOf(5, 5, 5, 5).k()
        }

    }

}