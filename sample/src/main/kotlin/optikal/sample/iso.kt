package optikal.sample

import optikal.Iso
import optikal.Isos
import java.util.*

@Isos data class Person2(val name: String, val age: Int, val lolo: Long, val doble: Double)

@Isos data class Person(val name: String, val age: Int)

//@Isos data class ShittyDataclass(val integere: Int, val doublee: Double, val longe: Long, val stringe: String, val booleane: Boolean, val arraye: Array<String>)

val personToTuple = Iso<Person, Pair<String, Int>>(
        get = { person -> Pair(person.name, person.age) },
        reverseGet = { (first, second) -> Person(first, second) })

fun <A> listToVector() = Iso<List<A>, Vector<A>>({ Vector(it) }, { it.toList() })

val stringToList = Iso<String, List<Char>>({ it.toList() }, { it.joinToString("") })

fun main(args: Array<String>) {

    println(personToTuple.get(Person("Zoe", 25)))
    println(personToTuple.reverseGet(Pair("Zoe", 25)))

    val toVector = listToVector<Int>().get(listOf(1, 2, 3, 4, 5))
    println("${toVector.javaClass.simpleName}: $toVector")
    val toVectorReversed = listToVector<Int>().reverse().get(Vector<Int>(listOf(1, 2, 3, 4, 5)))
    println("${toVectorReversed.javaClass.simpleName}: $toVectorReversed")

    println(stringToList.modify { it.drop(1) }("Hello"))
}