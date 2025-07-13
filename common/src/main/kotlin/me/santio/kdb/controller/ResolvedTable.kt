package me.santio.kdb.controller

import me.santio.kdb.Kdb
import me.santio.kdb.node.ColumnNode
import me.santio.kdb.node.TableNode
import me.santio.kdb.statement.Bindings
import java.lang.reflect.Field

@ConsistentCopyVisibility
data class ResolvedTable<T> private constructor(
    private val kdb: Kdb,
    val clazz: Class<T>,
    val node: TableNode,
    val fields: Map<String, Field>,
) {

    /**
     * Create a new instance of the specified class without calling any constructors
     * @param values The list of values to set fields to
     * @throws IllegalStateException If the provided values map is missing a required column field value
     * @return The newly created instance
     */
    fun create(values: Map<String, Any?>): T {
        val instance = kdb.settings.objenesis.newInstance(clazz)

        for (field in fields.values) {
            field.set(instance, values[field.name] ?: error("Missing value for field: ${field.name}"))
        }

        return instance
    }

    /**
     * Get the default value of a field
     */
//    fun defaultValue(column: ColumnNode): Any? {
//        val field = fields[column.reference.substringAfterLast('.')] ?: run {
//            error("The provided column node doesn't have a field, so failed to find default")
//        }
//
//
//    }

    /**
     * Creates bindings for an existing table class of this kind
     */
    fun bindings(instance: T): Bindings.Variables {
        return Bindings.Variables(node.columns.associate { column ->
            column.variable to fields[column.variable]!!.get(instance)
        }.toMutableMap())
    }

    companion object {

        private val cache = mutableMapOf<Class<*>, ResolvedTable<*>>()

        /**
         * Getting the field reflection this way as apposed to getting all fields is significantly better
         * for performance and allows us to quickly find the field that we already know of
         */
        private fun findField(clazz: Class<*>?, column: ColumnNode): Field? {
            if (clazz == null) return null
            return runCatching { clazz.getDeclaredField(column.reference.substringAfterLast(".")) }.getOrNull()
                ?.apply { if (!trySetAccessible()) error("Failed to make ${column.reference} accessible") }
                ?: findField(clazz.superclass, column)
        }

        @Suppress("UNCHECKED_CAST") // - Checked
        internal fun <T: Any> resolve(kdb: Kdb, node: TableNode, clazz: Class<T>) = cache.getOrPut(clazz) {
            val fields = node.columns.associate {
                it.variable to (findField(clazz, it) ?: error("Failed to find field ${it.reference}, this is a bug"))
            }

            return ResolvedTable(kdb, clazz, node, fields)
        } as ResolvedTable<T>
    }

}