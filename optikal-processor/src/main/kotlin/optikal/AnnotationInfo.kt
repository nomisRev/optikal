package optikal

val lensesAnnotationKClass = Lenses::class
val lensesAnnotationClass = lensesAnnotationKClass.java
val lensesAnnotationName = "@" + lensesAnnotationClass.simpleName

val prismsAnnotationKClass = Prisms::class
val prismsAnnotationClass = prismsAnnotationKClass.java
val prismsAnnotationName = "@" + prismsAnnotationClass.simpleName

val isoAnnotationKClass = Isos::class
val isoAnnotationClass = isoAnnotationKClass.java
val isoAnnotationName = "@" + isoAnnotationClass.simpleName
