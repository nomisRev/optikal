package optikal.optics

import io.kotlintest.KTestJUnitRunner
import io.kotlintest.properties.Gen
import kategory.Eq
import kategory.Option
import kategory.UnitSpec
import kategory.genFunctionAToB
import optikal.optics.laws.OptionalLaws
import org.junit.runner.RunWith

@RunWith(KTestJUnitRunner::class)
class OptionalSpec : UnitSpec() {

    init {
        testLaws(
                OptionalLaws.laws(
                        optional = head,
                        aGen = Gen.list(Gen.int()),
                        bGen = Gen.int(),
                        funcGen = genFunctionAToB(Gen.int()),
                        EQA = Eq.any(),
                        EQB = Eq.any()
                )
        )
    }

}

val head = Optional<List<Int>, Int>(
        { Option.fromNullable(it.firstOrNull()) },
        { int -> { list -> listOf(int) + if (list.size > 1) list.drop(1) else emptyList() } }
)