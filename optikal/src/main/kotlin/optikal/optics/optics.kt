package optikal.optics

import kotlin.annotation.AnnotationRetention.SOURCE
import kotlin.annotation.AnnotationTarget.CLASS

@Retention(SOURCE)
@Target(CLASS)
annotation class Prisms

@Retention(SOURCE)
@Target(CLASS)
annotation class Lenses