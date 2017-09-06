package optikal

import com.squareup.kotlinpoet.*
import java.io.File

class IsosFileGenerator(
        private val annotatedList: Collection<AnnotatedIso.Element>,
        private val generatedDir: File
) {
    fun generate() = buildIsos(annotatedList).writeTo(generatedDir)

    private fun buildIsos(elements: Collection<AnnotatedIso.Element>) = elements.flatMap(this::processElement)
            .fold(KotlinFile.builder("optikal", "Isos").skipJavaLangImports(true), { builder, isoSpec ->
                builder.addFun(isoSpec)
            }).build()

    private fun processElement(annotatedIso: AnnotatedIso.Element): List<FunSpec> =
            annotatedIso.properties.map { variable ->
                val className = annotatedIso.type.simpleName.toString().toLowerCase()
                val variableName = variable.simpleName

                FunSpec.builder("$className${variableName.toString().capitalize()}")
                        .returns(ParameterizedTypeName.get(Iso::class.asClassName(), annotatedIso.type.asType().asTypeName(), variable.asType().asTypeName()))
                        .addStatement(
                                """return %T(
                                   |        get = { $className: %T -> $className.$variableName },
                                   |        set = { $variableName: %T ->
                                   |            { $className: %T ->
                                   |                $className.copy($variableName = $variableName)
                                   |            }
                                   |        }
                                   |)""".trimMargin(), Iso::class, annotatedIso.type, variable, annotatedIso.type
                        ).build()
            }

}