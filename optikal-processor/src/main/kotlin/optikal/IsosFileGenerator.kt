package optikal

import com.squareup.kotlinpoet.*
import kategory.*
import java.io.File
import javax.lang.model.element.VariableElement

class IsosFileGenerator(
        private val annotatedList: Collection<AnnotatedIso.Element>,
        private val generatedDir: File
) {
    private val letters = "abcdefghij"

    private val tuples = listOf(Tuple2::class, Tuple2::class, Tuple3::class,
            Tuple4::class, Tuple5::class, Tuple6::class,
            Tuple7::class, Tuple8::class, Tuple9::class, Tuple10::class)

    fun generate() = buildIsos(annotatedList).writeTo(generatedDir)

    private fun buildIsos(elements: Collection<AnnotatedIso.Element>) = elements.map(this::processElement)
            .fold(KotlinFile.builder("optikal", "Isos").skipJavaLangImports(true), { builder, isoSpec ->
                builder.addFun(isoSpec)
            }).build()

    private fun processElement(annotatedIso: AnnotatedIso.Element): FunSpec {
        val className = annotatedIso.type.simpleName.toString().toLowerCase()
        val properties = annotatedIso.properties.toList()
        val tuple = tuples[properties.size - 1]
        val propertiesTypes = properties.map { it.asType().asTypeName() }

        val startArgs = listOf(Iso::class, annotatedIso.type)
        val getFuncArgs = listOf(tuple)
        val setFuncArgs = listOf(tuple) + propertiesTypes
        val endArgs = listOf(annotatedIso.type)
        val args = startArgs + getFuncArgs + setFuncArgs + endArgs
        val arrayArgs = Array(args.size, { args[it] })

        return FunSpec.builder("${className}Iso")
                .returns(ParameterizedTypeName.get(Iso::class.asClassName(), annotatedIso.type.asType().asTypeName(),
                        ParameterizedTypeName.get(tuples[properties.size - 1].asClassName(), *properties.map { it.asType().asTypeName() }.toTypedArray())))
                .addStatement(
                        """return %T(
                                   |        get = { $className: %T -> ${getTuple(properties, className)} },
                                   |        reverseGet = { tuple: ${setTuple(properties.size)} -> %T(${classConstructor(properties.size)}) }
                                   |)""".trimMargin(), *arrayArgs
                ).build()
    }

    private fun getTuple(properties: List<VariableElement>, className: String) =
            properties.joinToString(prefix = "%T(", postfix = ")", transform = { "$className.${it.simpleName}" })

    private fun setTuple(propertiesSize: Int) = (0 until propertiesSize).joinToString(prefix = "%T<", postfix = ">", transform = { "%T" })

    private fun classConstructor(propertiesSize: Int) = (0 until propertiesSize).joinToString { "tuple.${letters[it]}" }
}