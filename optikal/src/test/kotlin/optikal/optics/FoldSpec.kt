package optikal.optics

import io.kotlintest.matchers.shouldBe
import io.kotlintest.specs.StringSpec
import kategory.IntMonoid
import kategory.ListKW
import kategory.ListKWKind
import kategory.ListKWMonoid
import kategory.k
import kategory.none
import kategory.some

class FoldSpec : StringSpec() {

    val intFold: Fold<ListKWKind<Int>, Int> = Fold.fromFoldable(ListKW.foldable())
    val stringFold: Fold<ListKWKind<String>, String> = Fold.fromFoldable(ListKW.foldable())

    init {

        "folding a list of ints" {
            intFold.fold(IntMonoid, listOf(1, 1, 1, 1, 1).k()) shouldBe 5
        }

        "folding and mapping a list of strings" {
            stringFold.foldMapI(IntMonoid, listOf("1", "1", "1", "1", "1").k(), String::toInt)
        }

        "getting all values" {
            intFold.getAll(object : ListKWMonoid<Int> {}, listOf(1, 2, 3, 4, 5).k()) shouldBe listOf(1, 2, 3, 4, 5).k()
        }

        "Getting the length" {
            intFold.length(listOf(1, 2, 3, 4, 5).k()) shouldBe 5
            intFold.length(emptyList<Int>().k()) shouldBe 0
        }

        "find" {
            intFold.find { it > 2 }(listOf(1, 2, 3, 4).k()) shouldBe 3.some()
            intFold.find{ it > 9}(listOf(1, 2, 3, 4).k()) shouldBe none<Int>()
        }

        "all" {
            intFold.all { it == 1 }(listOf(1,1,1,1).k()) shouldBe true
            intFold.all { it == 1 }(listOf(1,1,2,1).k()) shouldBe false
            intFold.all { it == 1 }(emptyList<Int>().k()) shouldBe true
        }

        "empty" {
            intFold.isEmpty(listOf(1).k()) shouldBe false
            intFold.isEmpty(emptyList<Int>().k()) shouldBe true
        }

        "nonEmpty" {
            intFold.nonEmpty(listOf(1).k()) shouldBe true
            intFold.nonEmpty(emptyList<Int>().k()) shouldBe false
        }

    }

}