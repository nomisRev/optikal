package optikal

import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.runBlocking
import me.eugeniomarletti.kotlin.processing.KotlinAbstractProcessor
import java.io.File
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement

class OptikalProcessor : KotlinAbstractProcessor() {

    override fun getSupportedSourceVersion(): SourceVersion = SourceVersion.latestSupported()

    override fun getSupportedAnnotationTypes() = setOf(Lenses::class.java.canonicalName)

    override fun process(annotations: Set<TypeElement>, roundEnv: RoundEnvironment): Boolean {
        val dir = createKaptGeneratedDir()

        roundEnv.getElementsAnnotatedWith(Lenses::class.java)
                .takeIf(Set<Element>::isNotEmpty)
                ?.let(::LensesFileGenerator)
                ?.generate(dir, messager)

        return true
    }

    private fun createKaptGeneratedDir() = File(processingEnv.options["kapt.kotlin.generated"]).apply {
        if (!parentFile.exists()) {
            parentFile.mkdirs()
        }
    }

}

internal fun <A> Collection<A>.pforEach(f: suspend (A) -> Unit): List<Unit> = runBlocking {
    map { async(CommonPool) { f(it) } }.map { it.await() }
}

internal fun <A, B> Collection<A>.pmap(f: suspend (A) -> B): List<B> = runBlocking {
    map { async(CommonPool) { f(it) } }.map { it.await() }
}

internal fun <A, B> Collection<A>.pflatMap(f: suspend (A) -> List<B>): List<B> = runBlocking {
    flatMap {
        async(CommonPool) { f(it) }.await()
    }
}
