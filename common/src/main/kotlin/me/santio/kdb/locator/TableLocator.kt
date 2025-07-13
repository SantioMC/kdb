package me.santio.kdb.locator

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import me.santio.kdb.Kdb
import me.santio.kdb.controller.ResolvedTable
import me.santio.kdb.node.TableNode

/**
 * Locate the generated tables from the annotation processor. These are normally stored inside the jar
 * file itself as a resource.
 * @author santio
 */
@Suppress("unused")
object TableLocator {

    private val json = Json {
        isLenient = true
        ignoreUnknownKeys = true
    }

    private var tables: List<TableNode>? = null

    @OptIn(ExperimentalSerializationApi::class)
    fun getTables(): List<TableNode> {
        if (tables != null) return tables!!

        load()
        return tables!!
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun load() {
        this.tables = json.decodeFromStream(KdbFileLocator.inputStream("tables.json"))
    }

    fun node(clazz: Class<*>): TableNode? {
        return getTables().firstOrNull { it.reference == clazz.name }
    }

    fun <T: Any> table(kdb: Kdb, node: TableNode, clazz: Class<T>): ResolvedTable<T> {
        return ResolvedTable.resolve(kdb, node, clazz)
    }

    inline fun <reified T: Any> tableNode() = node(T::class.java)

}