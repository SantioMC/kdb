package me.santio.kdb.query

import me.santio.kdb.statement.Bindings

data class Where(
    val condition: Condition<*>,
    val filter: Filter
) {

    fun bindings(): Bindings {
        return Bindings.Classic(condition.values)
    }

    fun toSql(): String {
        return filter.toString().removeSurrounding("(", ")")
    }

}