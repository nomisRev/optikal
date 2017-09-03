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

    data class User(val token: Token)

    val userLens: Lens<User, Token> = Lens(
            get = { user: User -> user.token },
            set = { token: Token -> { user: User -> user.copy(token = token) } }
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

        "Evaluating a target within a Lens" {

            "With a positive predicate" {
                forAll({ targetValue: String ->
                    tokenLens.exist { it == targetValue }(Token(targetValue))
                })
            }

            "With a negative predicate" {
                forAll({ targetValue: String ->
                    tokenLens.exist { it != targetValue }(Token(targetValue)).not()
                })
            }

        }

        "finding a target within a Lens" {
            forAll({ targetValue: String ->
                tokenLens.find { it == targetValue }(Token(targetValue)).isDefined
            })
        }

        "Composing multiple lenses together" {
            val userTokenLens = userLens composeLens tokenLens

            forAll({ newValue: String ->
                val user = User(Token("old value"))
                userTokenLens.set(newValue)(user) == userLens.set(tokenLens.modify({ newValue }, user.token))(user)
            })

        }

        "Composing multiple lenses together" {
            val userTokenLens = userLens + tokenLens

            forAll({ newValue: String ->
                val user = User(Token("old value"))
                userTokenLens.set(newValue)(user) == userLens.set(tokenLens.modify({ newValue }, user.token))(user)
            })

        }

    }

}