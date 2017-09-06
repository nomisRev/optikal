package optikal

import com.squareup.kotlinpoet.*
import kategory.*
import me.eugeniomarletti.kotlin.metadata.kotlinMetadata
import java.io.File

class IsosFileGenerator(
        private val annotatedList: Collection<AnnotatedIso.Element>,
        private val generatedDir: File
) {
    val letters = "abcdefghij"

    fun generate() = buildIsos(annotatedList).writeTo(generatedDir)

    private fun buildIsos(elements: Collection<AnnotatedIso.Element>) = elements.map(this::processElement)
            .fold(KotlinFile.builder("optikal", "Isos").skipJavaLangImports(true), { builder, isoSpec ->
                builder.addFun(isoSpec)
            }).build()

    private fun processElement(annotatedIso: AnnotatedIso.Element): FunSpec {
        val className = annotatedIso.type.simpleName.toString().toLowerCase()
        val properties = annotatedIso.properties.toList()

        return FunSpec.builder("${className}Iso")
                .returns(ParameterizedTypeName.get(Iso::class.asClassName(), annotatedIso.type.asType().asTypeName(),
                        ParameterizedTypeName.get(tuples[properties.size - 1], *properties.map { it.asType().asTypeName() }.toTypedArray())))
                .addStatement(
                        """return %T(
                                   |        get = { $className: %T -> ${tupleNameString(properties.map { it.simpleName.toString() }, "$className.")} },
                                   |        reverseGet = { tuple: ${tupleNameString(properties.map { it.asType().asTypeName().toString() }, "", prefix = "<", postFix = ">")} -> %T(${classConstructor(properties.size)}) }
                                   |)""".trimMargin(), Iso::class, annotatedIso.type, annotatedIso.type
                ).build()
    }

    fun classConstructor(propertiesSize: Int) = (0 until propertiesSize).joinToString { "tuple.${letters[it]}" }

    fun tupleNameString(properties: List<String>, className: String, prefix: String = "(", postFix: String = ")") =
            "Tuple${properties.size}${properties.joinToString(prefix = prefix, postfix = postFix, transform = { "$className$it" })}"

    val tuples = listOf<ClassName>(Tuple2::class.asClassName(), Tuple2::class.asClassName(), Tuple3::class.asClassName(), Tuple4::class.asClassName(),
            Tuple5::class.asClassName(), Tuple6::class.asClassName(), Tuple7::class.asClassName(), Tuple8::class.asClassName()
            , Tuple9::class.asClassName(), Tuple10::class.asClassName())
}