package optikal.optics

import kategory.Monoid
import kategory.Option

data class Tagged<A, T>(val value: A)

fun <A, T> A.tag(): Tagged<A, T> = Tagged(this)

fun <A, T> Tagged<A, T>.unwrap(): A = value

sealed class First

fun <A> firstOptionMonoid() = object : Monoid<Tagged<Option<A>, First>> {
    override fun empty(): Tagged<Option<A>, First> = Tagged(Option.None)

    override fun combine(first: Tagged<Option<A>, First>, second: Tagged<Option<A>, First>): Tagged<Option<A>, First> =
            if (first.value.isDefined) first else second
}

sealed class Last

fun <A> lastOptionMonoid() = object : Monoid<Tagged<Option<A>, Last>> {
    override fun empty(): Tagged<Option<A>, Last> = Tagged(Option.None)

    override fun combine(first: Tagged<Option<A>, Last>, second: Tagged<Option<A>, Last>): Tagged<Option<A>, Last> =
            if (second.value.isDefined) second else first
}