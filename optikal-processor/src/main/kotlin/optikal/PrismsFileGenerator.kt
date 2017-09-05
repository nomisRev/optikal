package optikal

import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KotlinFile
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.asTypeName
import java.io.File

class PrismsFileGenerator(
        private val annotatedList: Collection<AnnotatedPrism.Element>,
        private val dir: File
) {

    fun generate() = buildPrisms(annotatedList).writeTo(dir)

    private fun buildPrisms(elements: Collection<AnnotatedPrism.Element>) = elements.flatMap(this::processElement)
            .fold(KotlinFile.builder("optikal", "Prisms").skipJavaLangImports(true), { builder, prismSpec ->
                builder.addFun(prismSpec)
            })
            .addStaticImport("kategory", "right", "left")
            .build()

    private fun processElement(annotatedPrism: AnnotatedPrism.Element): List<FunSpec> =
            annotatedPrism.subTypes.map { subClass ->
                val sealedClassName = annotatedPrism.type.simpleName.toString().toLowerCase()
                val subTypeName = subClass.simpleName.toString()
                FunSpec.builder("$sealedClassName$subTypeName")
                        .returns(ParameterizedTypeName.get(Prism::class.asClassName(), annotatedPrism.type.asType().asTypeName(), subClass.asType().asTypeName()))
                        .addStatement(
                                """return %T(
                                   |        getOrModify = { $sealedClassName: %T ->
                                   |            when ($sealedClassName) {
                                   |                is %T -> $sealedClassName.right()
                                   |                else -> $sealedClassName.left()
                                   |            }
                                   |        },
                                   |        reverseGet = { it }
                                   |)""".trimMargin(), Prism::class, annotatedPrism.type, subClass)
                        .build()
            }

}