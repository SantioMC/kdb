package me.santio.kdb.processor

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.validate
import kotlinx.serialization.json.Json
import me.santio.kdb.base.Migration
import me.santio.kdb.node.TableNode
import me.santio.kdb.processor.annotations.Table
import me.santio.kdb.processor.helper.write
import me.santio.kdb.processor.resolver.ClassResolver
import me.santio.kdb.processor.visitor.TableVisitor

class KdbProcessor(
    private val environment: SymbolProcessorEnvironment
): SymbolProcessor {

    lateinit var classResolver: ClassResolver

    private val tables = mutableListOf<TableNode>()
    private val json = Json {
        isLenient = true
        ignoreUnknownKeys = true
    }

    override fun process(resolver: Resolver): List<KSAnnotated> {
        classResolver = ClassResolver(resolver)
        val visitor = TableVisitor(environment.logger)

        val annotated = resolver.getSymbolsWithAnnotation(
            Table::class.qualifiedName!!,
        ).filter { it.validate() }

        val tables = annotated.filterIsInstance<KSClassDeclaration>().map {
            visitor.parse(it)
        }

        this.tables.addAll(tables)
        return annotated.toList()
    }

    override fun finish() {
        environment.codeGenerator.write("tables.json", json.encodeToString(tables))
        environment.codeGenerator.write("migrations.kdb", classResolver.subclasses<Migration>().joinToString("\n") {
            it.qualifiedName!!.asString()
        })
    }

}