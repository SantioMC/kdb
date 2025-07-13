@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

package me.santio.kdb.query

import me.santio.kdb.Kdb
import me.santio.kdb.controller.ResolvedTable
import kotlin.internal.OnlyInputTypes
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1

@Suppress("unused")
data class Condition<Table: Any>(
    val kdb: Kdb,
    val table: ResolvedTable<Table>,
    val values: MutableList<Any?> = mutableListOf()
) {

    private fun columnName(property: KProperty<*>): String {
        val column = table.node.columns.first { it.reference.substringAfterLast(".") == property.name }.name
        return kdb.platform.generator.literal(column)
    }

    private fun <@OnlyInputTypes T: Any?> filter(property: KProperty1<Table, T>, op: String, value: T): Filter {
        values.add(value)
        return Filter(columnName(property), "?", op)
    }

    infix fun <@OnlyInputTypes T: Any?> KProperty1<Table, T>.eq(value: T) = filter(this, "=", value)
    infix fun <@OnlyInputTypes T: Any?> KProperty1<Table, T>.neq(value: T) = filter(this, "!=", value)

    infix fun <N: Number> KProperty1<Table, N>.gt(value: N) = filter(this, ">", value)
    infix fun <N: Number> KProperty1<Table, N>.lt(value: N) = filter(this, "<", value)
    infix fun <N: Number> KProperty1<Table, N>.gte(value: N) = filter(this, ">=", value)
    infix fun <N: Number> KProperty1<Table, N>.lte(value: N) = filter(this, "<=", value)

    infix fun KProperty1<Table, String>.like(value: String) = filter(this, "LIKE", value)
    infix fun KProperty1<Table, String>.notLike(value: String) = filter(this, "NOT LIKE", value)

}