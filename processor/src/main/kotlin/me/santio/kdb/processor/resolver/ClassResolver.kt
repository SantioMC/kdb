package me.santio.kdb.processor.resolver

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSClassDeclaration
import me.santio.kdb.processor.helper.subtypeOf

class ClassResolver(
    private val resolver: Resolver
) {

    fun subclasses(subclass: Class<*>): List<KSClassDeclaration> {
        return resolver.getAllFiles()
            .flatMap { it.declarations }
            .filterIsInstance<KSClassDeclaration>()
            .filter { it.subtypeOf(subclass) }
            .toList()
    }

    inline fun <reified T: Any> subclasses() = subclasses(T::class.java)

}