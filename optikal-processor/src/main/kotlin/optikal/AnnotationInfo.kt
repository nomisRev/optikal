package optikal

val lensesAnnotationKClass = Lenses::class
val lensesAnnotationClass = lensesAnnotationKClass.java
val lensesAnnotationName = "@" + lensesAnnotationClass.simpleName