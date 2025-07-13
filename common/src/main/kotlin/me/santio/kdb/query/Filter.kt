@file:Suppress("unused")

package me.santio.kdb.query

data class Filter(
    val lhs: String,
    val rhs: String,
    val operator: String
) {

    override fun toString(): String {
        return "($lhs $operator $rhs)"
    }

    infix fun and(other: Filter) = Filter(this.toString(), other.toString(), "AND")
    infix fun or(other: Filter) = Filter(this.toString(), other.toString(), "OR")

}