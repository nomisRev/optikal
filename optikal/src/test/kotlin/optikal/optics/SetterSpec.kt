package optikal.optics

import io.kotlintest.KTestJUnitRunner
import io.kotlintest.matchers.shouldBe
import kategory.ListKW
import kategory.ListKWKind
import kategory.UnitSpec
import kategory.k
import org.junit.runner.RunWith

@RunWith(KTestJUnitRunner::class)
class SetterSpec : UnitSpec() {

    init {
        val eachL: Setter<ListKWKind<Int>, Int> = Setter.fromFunctor(ListKW.functor())

        "set" {
            eachL.set(0)(listOf(1,2,3,4).k()) shouldBe listOf(0,0,0,0).k()
        }

        "modify" {
            eachL.modify { it + 1 }(listOf(1,2,3,4).k()) shouldBe listOf(2,3,4,5).k()
         }

    }

}