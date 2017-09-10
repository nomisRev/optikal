package optikal.optics

import io.kotlintest.KTestJUnitRunner
import io.kotlintest.properties.Gen
import io.kotlintest.properties.forAll
import kategory.Applicative
import kategory.Eq
import kategory.HK
import kategory.Option
import kategory.UnitSpec
import kategory.applicative
import kategory.exists
import kategory.functor
import kategory.genFunctionAToB
import kategory.left
import kategory.right
import optikal.optics.laws.LensLaws
import optikal.optics.laws.OptionalLaws
import org.junit.runner.RunWith

@RunWith(KTestJUnitRunner::class)
class LensSpec : UnitSpec() {

    init {

        testLaws(
                LensLaws.laws(
                        lens = tokenLens,
                        aGen = TokenGen,
                        bGen = Gen.string(),
                        funcGen = genFunctionAToB(Gen.string()),
                        EQA = Eq.any(),
                        EQB = Eq.any(),
                        FA = Option.applicative()
                ) +  OptionalLaws.laws(
                        optional = tokenLens.asOptional(),
                        aGen = TokenGen,
                        bGen = Gen.string(),
                        funcGen = genFunctionAToB(Gen.string()),
                        EQA = Eq.any(),
                        EQB = Eq.any()
                )
        )

        "Get should extract the target" {
            forAll({ value: String ->
                val getValue = tokenLens.get(Token(value))
                getValue == value
            })
        }

        "Set should replace the target" {
            forAll({ newValue: String ->
                val modifiedToken = tokenLens.set(newValue)(token)
                modifiedToken.value == newValue
            })
        }

        "Modify should modify the target using a function" {
            forAll({ modifiedValue: String ->
                val modifiedToken = tokenLens.modify({ modifiedValue }, token)
                modifiedToken.value == modifiedValue
            })
        }

        "ModifyF should modify the target using a Functor function" {
            forAll({ modifiedValue: String ->
                tokenLens.modifyF(Option.functor(), f = { getVal(Option.applicative(), a = modifiedValue) }, a = token)
                        .exists(f = { it.value == modifiedValue })
            })
        }

        "Finding a target using a predicate within a Lens should be wrapped in the correct option result" {
            forAll({ predicate: Boolean ->
                tokenLens.find { predicate }(Token("any value")).isDefined == predicate
            })
        }

        "Checking existence predicate over the target should result in same result as predicate" {
            forAll({ predicate: Boolean ->
                tokenLens.exist { predicate }(Token("any value")) == predicate
            })
        }

        "Joining two lenses together with same target should yield same result" {
            val userTokenStringLens = userLens composeLens tokenLens
            val joinedLens = tokenLens.choice(userTokenStringLens)

            forAll({ tokenValue: String ->
                val token = Token(tokenValue)
                val user = User(token)
                joinedLens.get(token.left()) == joinedLens.get(user.right())
            })
        }

        "Pairing two disjoint lenses should yield a pair of their results" {
            val spiltLens: Lens<Pair<Token, User>, Pair<String, Token>> = tokenLens.split(userLens)
            forAll(TokenGen, UserGen, { token: Token, user: User ->
                spiltLens.get(token to user) == token.value to user.token
            })
        }

        "Creating a first pair with a type should result in the target to value" {
            val first = tokenLens.first<Int>()
            forAll(TokenGen, Gen.int(), { token: Token, int: Int ->
                first.get(token to int) == token.value to int
            })
        }

        "Creating a second pair with a type should result in the value target" {
            val first = tokenLens.second<Int>()
            forAll(Gen.int(), TokenGen, { int: Int, token: Token ->
                first.get(int to token) == int to token.value
            })
        }

        "Composing multiple lenses together" {
            forAll({ newValue: String ->
                userTokenLens.set(newValue)(oldUser) == userLens.set(tokenLens.modify({ newValue }, oldUser.token))(oldUser)
            })
        }

        "Composing multiple lenses together with the plus operator should yield same result as composeLens" {
            forAll({ newValue: String ->
                (userLens + tokenLens).set(newValue)(oldUser) == userTokenLens.set(newValue)(oldUser)
            })
        }

    }

}

data class Token(val value: String)
object TokenGen : Gen<Token> {
    override fun generate() = Token(Gen.string().generate())
}

val tokenLens: Lens<Token, String> = Lens(
        get = { token: Token -> token.value },
        set = { value: String -> { token: Token -> token.copy(value = value) } }
)

data class User(val token: Token)
object UserGen : Gen<User> {
    override fun generate() = User(TokenGen.generate())
}

val userLens: Lens<User, Token> = Lens(
        get = { user: User -> user.token },
        set = { token: Token -> { user: User -> user.copy(token = token) } }
)

val token = Token("old value")
val oldUser = User(token)

val userTokenLens = userLens composeLens tokenLens

inline fun <reified F> getVal(FA: Applicative<F> = applicative(), a: String): HK<F, String> = FA.pure(a)