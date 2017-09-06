package optikal

import me.eugeniomarletti.kotlin.metadata.*
import me.eugeniomarletti.kotlin.processing.KotlinAbstractProcessor
import org.jetbrains.kotlin.serialization.ProtoBuf
import java.io.File
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import javax.lang.model.element.VariableElement
import javax.lang.model.type.TypeKind

class OptikalProcessor : KotlinAbstractProcessor() {

    private val annotatedLenses = mutableListOf<AnnotatedLens.Element>()

    private val annotatedPrisms = mutableListOf<AnnotatedPrism.Element>()

    private val annotatedIsos = mutableListOf<AnnotatedIso.Element>()

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
                        .map(this::evalAnnotatedLensElement)
                        .map { annotatedLens ->
                            when (annotatedLens) {
                                is AnnotatedLens.InvalidElement -> throw KnownException(annotatedLens.reason)
                                is AnnotatedLens.Element -> annotatedLens
                            }
                        }

                annotatedPrisms += roundEnv
                        .getElementsAnnotatedWith(prismsAnnotationClass)
                        .map(this::evalAnnotatedPrismElement)
                        .map { annotatedPrism ->
                            when (annotatedPrism) {
                                is AnnotatedPrism.InvalidElement -> throw KnownException(annotatedPrism.reason)
                                is AnnotatedPrism.Element -> annotatedPrism
                            }
                        }

//                annotatedIsos += roundEnv
//                        .getElementsAnnotatedWith(isoAnnotationClass)
//                        .map(this::evalAnnotatedIsoElement)
//                        .map { annotatedIso ->
//                            when (annotatedIso) {
//                                is AnnotatedIso.InvalidElement -> throw KnownException(annotatedIso.reason)
//                                is AnnotatedIso.Element -> annotatedIso
//                            }
//                        }

                if (roundEnv.processingOver()) {
                    val generatedDir = File(options[kaptGeneratedOption].let(::File), lensesAnnotationClass.simpleName.toLowerCase()).also { it.mkdirs() }
                    LensesFileGenerator(annotatedLenses, generatedDir).generate()
                    PrismsFileGenerator(annotatedPrisms, generatedDir).generate()
//                    IsosFileGenerator(annotatedIsos, generatedDir).generate()
                }
            } catch (e: KnownException) {
                messager.logE(e.message)
            }
        }

        return false
    }

    fun evalAnnotatedLensElement(element: Element): AnnotatedLens = when {
        element.kotlinMetadata !is KotlinClassMetadata -> AnnotatedLens.InvalidElement("""
            |Cannot use @Lenses on ${element.enclosingElement}.${element.simpleName}.
            |It can only be used on data classes.""".trimMargin())

        (element.kotlinMetadata as KotlinClassMetadata).data.classProto.isDataClass ->
            AnnotatedLens.Element(element as TypeElement, element.enclosedElements.filter { it.asType().kind == TypeKind.DECLARED }.map { it as VariableElement })

        else -> AnnotatedLens.InvalidElement("${element.enclosingElement}.${element.simpleName} cannot be annotated with @Lenses")
    }

    fun evalAnnotatedPrismElement(element: Element): AnnotatedPrism = when {
        element.kotlinMetadata !is KotlinClassMetadata -> AnnotatedPrism.InvalidElement("""
            |Cannot use @Prisms on ${element.enclosingElement}.${element.simpleName}.
            |It can only be used on sealed classes.""".trimMargin())

        element.let { it.kotlinMetadata as KotlinClassMetadata }.data.classProto.modality == ProtoBuf.Modality.SEALED -> {
            val (nameResolver, classProto) = element.kotlinMetadata.let { it as KotlinClassMetadata }.data
            val sealedSubclasses = classProto.sealedSubclassFqNameList
                    .map(nameResolver::getString)
                    .map { it.replace('/', '.') }
                    .map(processingEnv.elementUtils::getTypeElement)

            AnnotatedPrism.Element(element as TypeElement, sealedSubclasses)
        }

        else -> AnnotatedPrism.InvalidElement("${element.enclosingElement}.${element.simpleName} cannot be annotated with @Prisms")
    }

    fun evalAnnotatedIsoElement(element: Element): AnnotatedIso = when {
        element.kotlinMetadata !is KotlinClassMetadata -> AnnotatedIso.InvalidElement("""
            |Cannot use @Iso on ${element.enclosingElement}.${element.simpleName}.
            |It can only be used on data classes.""".trimMargin())

        (element.kotlinMetadata as KotlinClassMetadata).data.classProto.isDataClass ->
            AnnotatedIso.Element(element as TypeElement, element.enclosedElements.filter { it.asType().kind == TypeKind.DECLARED }.map { it as VariableElement })

        else -> AnnotatedIso.InvalidElement("${element.enclosingElement}.${element.simpleName} cannot be annotated with @Iso")
    }
}
