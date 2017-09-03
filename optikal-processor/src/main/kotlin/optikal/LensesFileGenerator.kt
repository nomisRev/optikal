package optikal

import com.pacoworks.komprehensions.doLet
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KotlinFile
import kategory.Either
import kategory.flatMap
import me.eugeniomarletti.kotlin.metadata.KotlinClassMetadata
import me.eugeniomarletti.kotlin.metadata.isDataClass
import me.eugeniomarletti.kotlin.metadata.kotlinMetadata
import java.io.File
import javax.annotation.processing.Messager
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import javax.lang.model.element.VariableElement
import javax.lang.model.type.TypeKind

internal class LensesFileGenerator(private val annotatedList: Iterable<Element>) {

    sealed class AnnotatedLens {
        data class Element(val type: TypeElement, val properties: Collection<VariableElement>)
        data class InvalidElement(val reason: String)
    }

    internal fun generate(generatedDir: File, messager: Messager) {
        evalAnnotatedElement(annotatedList).fold(
                { messager.logE("OptikalProcessor: \n ${it.reason}\n.") },
                { buildLenses(it).writeTo(generatedDir) }
        )
    }

    internal fun buildLenses(elements: Collection<AnnotatedLens.Element>): KotlinFile = doLet(
            { KotlinFile.builder("optikal", "Lenses").skipJavaLangImports(true) },
            { _ -> elements.pflatMap { processElement(it) } },
            { file, lenses -> lenses.pforEach { file.addFun(it) } },
            { file, _, _ -> file.build() }
    )

    internal fun processElement(annotatedLens: AnnotatedLens.Element): List<FunSpec> =
            annotatedLens.properties.pmap { variable ->
                val className = annotatedLens.type.simpleName.toString().toLowerCase()
                val variableName = variable.simpleName

                FunSpec.builder("$className${variableName.toString().capitalize()}")
                        .addStatement(
                                """return %T(
                                   |        get = { $className: %T -> $className.$variableName },
                                   |        set = { $variableName: %T ->
                                   |            { $className: %T ->
                                   |                $className.copy($variableName = $variableName)
                                   |            }
                                   |        }
                                   |)""".trimMargin(), Lens::class, annotatedLens.type, variable, annotatedLens.type)
                        .build()
            }


    internal fun evalAnnotatedElement(elements: Iterable<Element>): Either<AnnotatedLens.InvalidElement, Collection<AnnotatedLens.Element>> {
        fun evalAnnotatedElement(element: Element): Either<AnnotatedLens.InvalidElement, Collection<AnnotatedLens.Element>> = when {
            element.kotlinMetadata !is KotlinClassMetadata -> Either.Left(AnnotatedLens.InvalidElement("""
            |Cannot use @Lenses on ${element.enclosingElement}.${element.simpleName}.
            |It can only be used on data classes.""".trimMargin()))

            (element.kotlinMetadata as KotlinClassMetadata).data.classProto.isDataClass ->
                Either.Right(listOf(AnnotatedLens.Element(element as TypeElement, element.enclosedElements.filter { it.asType().kind == TypeKind.DECLARED }.map { it as VariableElement })))

            else -> Either.Left(AnnotatedLens.InvalidElement("${element.enclosingElement}.${element.simpleName} cannot be annotated with @Lenses"))
        }

        return elements.map(::evalAnnotatedElement).reduce { a, b ->
            a.flatMap { aEles ->
                b.map { aEles + it }
            }
        }

    }

}