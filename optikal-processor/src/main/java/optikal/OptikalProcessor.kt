package optikal

import com.pacoworks.komprehensions.doLet
import com.squareup.kotlinpoet.*
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.runBlocking
import java.io.File
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import javax.lang.model.element.VariableElement
import javax.lang.model.type.TypeKind

class OptikalProcessor : AbstractProcessor() {

    override fun getSupportedSourceVersion(): SourceVersion = SourceVersion.latestSupported()

    override fun getSupportedAnnotationTypes() = setOf(Lenses::class.java.canonicalName)

    override fun process(annotations: Set<TypeElement>, roundEnv: RoundEnvironment): Boolean {
        roundEnv.getElementsAnnotatedWith(Lenses::class.java)
                .takeIf(Set<Element>::isNotEmpty)
                ?.let(this::buildOptikals)
                ?.writeTo(createKaptGeneratedDir())

        //organizing all lenses in a seperate object per type you cannot import all lenses or import lenses per type
        //start imports are not allowed for objects https://discuss.kotlinlang.org/t/public-extension-methods-in-objects-and-same-package-imports/2357/3
//        val namespace = TypeSpec.objectBuilder("Lenses")

        return true
    }

    internal fun buildOptikals(elements: Set<Element>): KotlinFile = doLet(
            { KotlinFile.builder("optikal", "Lenses").skipJavaLangImports(true) },
            { _ -> elements.pflatMap { processElement(it) } },
            { file, lenses -> lenses.forEach { file.addFun(it) } },
            { file, _, _ -> file.build() }
    )

    internal fun processElement(ele: Element): List<FunSpec> = ele.enclosedElements
            .filter { it.asType().kind == TypeKind.DECLARED }
            .map { it as VariableElement }
            .pmap { variable ->
                FunSpec.builder("${ele.simpleName.toString().toLowerCase()}${variable.simpleName.toString().capitalize()}")
                        .addStatement(""
                                + "return %T(\n"
                                + "\tget= { ${ele.simpleName.toString().toLowerCase()}: %T -> ${ele.simpleName.toString().toLowerCase()}.${variable.simpleName} },\n"
                                + "\tset= { ${variable.simpleName}: %T -> { ${ele.simpleName.toString().toLowerCase()}: %T ->" +
                                "${ele.simpleName.toString().toLowerCase()}.copy(${variable.simpleName}=  ${variable.simpleName}) } }\n"
                                + ")", Lens::class, ele, variable, ele)
                        .build()
            }

    internal fun createKaptGeneratedDir() = File(processingEnv.options["kapt.kotlin.generated"]).apply {
        if (!parentFile.exists()) {
            parentFile.mkdirs()
        }
    }

    internal fun <A, B> Iterable<A>.pmap(f: suspend (A) -> B): List<B> = runBlocking {
        map { async(CommonPool) { f(it) } }.map { it.await() }
    }

    internal fun <A, B> Iterable<A>.pflatMap(f: suspend (A) -> List<B>): List<B> = runBlocking {
        flatMap {
            async(CommonPool) { f(it) }.await()
        }
    }

}