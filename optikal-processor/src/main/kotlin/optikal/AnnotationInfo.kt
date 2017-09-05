package optikal

val lensesAnnotationKClass = Lenses::class
val lensesAnnotationClass = lensesAnnotationKClass.java
val lensesAnnotationName = "@" + lensesAnnotationClass.simpleName

val prismsAnnotationKClass = Prisms::class
val prismsAnnotationClass = prismsAnnotationKClass.java
val prismsAnnotationName = "@" + prismsAnnotationClass.simpleName
