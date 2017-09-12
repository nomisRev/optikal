package optikal.optics

import io.kotlintest.KTestJUnitRunner
import io.kotlintest.properties.Gen
import kategory.Eq
import kategory.Option
import kategory.StringMonoid
import kategory.Try
import kategory.UnitSpec
import kategory.applicative
import kategory.genFunctionAToB
import optikal.optics.laws.IsoLaws
import optikal.optics.laws.LensLaws
import optikal.optics.laws.OptionalLaws
import optikal.optics.laws.PrismLaws
import org.junit.runner.RunWith

@RunWith(KTestJUnitRunner::class)
class IsoSpec : UnitSpec() {

    init {

        val tokenIso: Iso<Token, String> = Iso(
                get = { token: Token -> token.value },
                reverseGet = ::Token
        )

        val aIso: Iso<SumType.A, String> = Iso(
                get = { a: SumType.A -> a.string },
                reverseGet = SumType::A
        )

        testLaws(
                LensLaws.laws(
                        lens = tokenIso.asLens(),
                        aGen = TokenGen,
                        bGen = Gen.string(),
                        funcGen = genFunctionAToB(Gen.string()),
                        EQA = Eq.any(),
                        EQB = Eq.any(),
                        FA = Try.applicative()
                ) + PrismLaws.laws(
                        prism = aIso.asPrism(),
                        aGen = AGen,
                        bGen = Gen.string(),
                        funcGen = genFunctionAToB(Gen.string()),
                        EQA = Eq.any(),
                        EQB = Eq.any(),
                        FA = Option.applicative()
                ) + IsoLaws.laws(
                        iso = tokenIso,
                        aGen = TokenGen,
                        bGen = Gen.string(),
                        funcGen = genFunctionAToB(Gen.string()),
                        EQA = Eq.any(),
                        EQB = Eq.any(),
                        bMonoid = StringMonoid
                ) +  OptionalLaws.laws(
                        optional = tokenIso.asOptional(),
                        aGen = TokenGen,
                        bGen = Gen.string(),
                        funcGen = genFunctionAToB(Gen.string()),
                        EQA = Eq.any(),
                        EQB = Eq.any()
                )
        )


    }

}