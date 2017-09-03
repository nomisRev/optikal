package optikal

import me.eugeniomarletti.kotlin.metadata.KotlinClassMetadata
import me.eugeniomarletti.kotlin.metadata.isDataClass
import me.eugeniomarletti.kotlin.metadata.kaptGeneratedOption
import me.eugeniomarletti.kotlin.metadata.kotlinMetadata
import me.eugeniomarletti.kotlin.processing.KotlinAbstractProcessor
import java.io.File
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import javax.lang.model.element.VariableElement
import javax.lang.model.type.TypeKind

class OptikalProcessor : KotlinAbstractProcessor() {

    private val annotatedLenses = mutableListOf<AnnotatedLens.Element>()

    override fun getSupportedSourceVersion(): SourceVersion = SourceVersion.latestSupported()

    override fun getSupportedAnnotationTypes() = setOf(lensesAnnotationClass.canonicalName)

    class KnownException(message: String) : RuntimeException(message) {
        override val message: String get() = super.message as String
        operator fun component1() = message
    }

    override fun process(annotations: Set<TypeElement>, roundEnv: RoundEnvironment): Boolean {
        if (!roundEnv.errorRaised()) {
            try {
                annotatedLenses += roundEnv
                        .getElementsAnnotatedWith(lensesAnnotationClass)
                        .map(this::evalAnnotatedElement)
                        .map { annotatedLens ->
                            when (annotatedLens) {
                                is AnnotatedLens.InvalidElement -> throw KnownException(annotatedLens.reason)
                                is AnnotatedLens.Element -> annotatedLens
                            }
                        }

                if (roundEnv.processingOver()) {
                    val generatedDir = File(options[kaptGeneratedOption].let(::File), lensesAnnotationClass.simpleName).also { it.mkdirs() }
                    LensesFileGenerator(annotatedLenses, generatedDir).generate()
                }
            } catch (e: KnownException) {
                messager.logE(e.message)
            }
        }

        return false
    }

    fun evalAnnotatedElement(element: Element): AnnotatedLens = when {
        element.kotlinMetadata !is KotlinClassMetadata -> AnnotatedLens.InvalidElement("""
            |Cannot use @Lenses on ${element.enclosingElement}.${element.simpleName}.
            |It can only be used on data classes.""".trimMargin())

        (element.kotlinMetadata as KotlinClassMetadata).data.classProto.isDataClass ->
            AnnotatedLens.Element(element as TypeElement, element.enclosedElements.filter { it.asType().kind == TypeKind.DECLARED }.map { it as VariableElement })

        else -> AnnotatedLens.InvalidElement("${element.enclosingElement}.${element.simpleName} cannot be annotated with @Lenses")
    }

}
