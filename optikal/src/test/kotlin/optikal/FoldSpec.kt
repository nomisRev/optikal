package optikal

import io.kotlintest.KTestJUnitRunner
import io.kotlintest.matchers.shouldBe
import io.kotlintest.specs.StringSpec
import kategory.IntMonoid
import kategory.ListKW
import kategory.ListKWKind
import kategory.ListKWMonoid
import kategory.Monoid
import kategory.Option
import kategory.k
import org.junit.runner.RunWith

@RunWith(KTestJUnitRunner::class)
class FoldSpec : StringSpec() {

    val intFold: Fold<ListKWKind<Int>, Int> = Fold.fromFoldable(ListKW.foldable())
    val stringFold: Fold<ListKWKind<String>, String> = Fold.fromFoldable(ListKW.foldable())

    val addMonoid = object : Monoid<Int> {
        override fun empty() = 0
        override fun combine(a: Int, b: Int) = a + b
    }

    val addOptionMonoid = object : Monoid<Option<Int>> {
        override fun empty() = Option.None
        override fun combine(a: Option<Int>, b: Option<Int>) = a.flatMap { aInt ->
            b.map { aInt + it }
        }
    }

    init {

        "folding a list of ints" {
            intFold.fold(IntMonoid, listOf(1, 1, 1, 1, 1).k()) shouldBe 5
        }

        "folding and mapping a list of strings" {
            stringFold.foldMap(IntMonoid, listOf("1", "1", "1", "1", "1").k(), String::toInt)
        }

        "getting all values" {
            intFold.getAll(object : ListKWMonoid<Int> {}, listOf(1, 2, 3, 4, 5).k()) shouldBe listOf(1, 2, 3, 4, 5).k()
        }

        "Getting the length" {
            intFold.length(listOf(1, 2, 3, 4, 5).k()) shouldBe 5
            intFold.length(emptyList<Int>().k()) shouldBe 0
        }

    }

}