package optikal

import io.kotlintest.KTestJUnitRunner
import io.kotlintest.properties.forAll
import io.kotlintest.specs.StringSpec
import org.junit.runner.RunWith

@RunWith(KTestJUnitRunner::class)
class LensTest : StringSpec() {

    data class Token(val value: String)

    val token = Token("1")

    val tokenLens: Lens<Token, String> = Lens(
            get = { token: Token -> token.value },
            set = { value: String -> { token: Token -> token.copy(value = value) } }
    )

    init {

        "Modifying the name of a employees company street" {
            forAll({ newToken: String ->
                val modifiedToken = tokenLens.modify({ newToken }, token)
                modifiedToken.value == newToken
            })
        }

    }

}