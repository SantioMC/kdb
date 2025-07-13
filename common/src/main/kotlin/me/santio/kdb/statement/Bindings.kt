package me.santio.kdb.statement

import me.santio.kdb.Kdb
import me.santio.kdb.node.ColumnNode

sealed interface Bindings {

    /**
     * A list of bindings to replace ":variable" placeholders in queries
     */
    data class Variables(val map: MutableMap<String, Any?> = mutableMapOf()): Bindings {
        infix fun String.to(value: Any?) {
            map[this] = value
        }

        fun resolve(column: String): Any? = map[column]
        fun resolve(column: ColumnNode): Any? = resolve(column.variable)
    }

    /**
     * A list of values to add in the order they appear, replacing "?"
     */
    data class Classic(val values: List<Any?>): Bindings

    companion object {
        fun <T: Any> resolve(kdb: Kdb, clazz: Class<T>, table: T) = kdb.table(clazz).bindings(table)
        inline fun <reified T: Any> resolve(kdb: Kdb, table: T) = resolve(kdb, T::class.java, table)
    }

}

@Suppress("unused")
fun bind(consumer: Bindings.Variables.() -> Unit): Bindings {
    return Bindings.Variables().apply(consumer)
}