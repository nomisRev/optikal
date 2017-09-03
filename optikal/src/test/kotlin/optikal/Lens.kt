package optikal

import io.kotlintest.KTestJUnitRunner
import io.kotlintest.properties.forAll
import io.kotlintest.specs.StringSpec
import org.junit.runner.RunWith

@RunWith(KTestJUnitRunner::class)
class LensTest : StringSpec() {

    data class Token(val value: String)

    val tokenLens: Lens<Token, String> = Lens(
            get = { token: Token -> token.value },
            set = { value: String -> { token: Token -> token.copy(value = value) } }
    )

    init {
        "Can set a new value for a type" {
            forAll({ newValue: String ->
                val modifiedToken = tokenLens.set(newValue)(Token("old value"))
                modifiedToken.value == newValue
            })
        }

        "Can get a value from a type" {
            forAll({ value: String ->
                val getValue = tokenLens.get(Token(value))
                getValue == value
            })
        }

        "Can modify a value of a type" {
            forAll({ modifiedValue: String ->
                val modifiedToken = tokenLens.modify({ modifiedValue }, Token("old value"))
                modifiedToken.value == modifiedValue
            })
        }
    }

}