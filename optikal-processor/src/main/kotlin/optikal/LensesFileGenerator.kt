package optikal

import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KotlinFile
import java.io.File

class LensesFileGenerator(
        private val annotatedList: Collection<AnnotatedLens.Element>,
        private val generatedDir: File
) {

    fun generate() = buildLenses(annotatedList).writeTo(generatedDir)

    private fun buildLenses(elements: Collection<AnnotatedLens.Element>) = elements.flatMap(this::processElement)
            .fold(KotlinFile.builder("optikal", "Lenses").skipJavaLangImports(true), { builder, lensSpec ->
                builder.addFun(lensSpec)
            }).build()

    private fun processElement(annotatedLens: AnnotatedLens.Element): List<FunSpec> =
            annotatedLens.properties.map { variable ->
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

}

