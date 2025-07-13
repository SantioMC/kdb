package me.santio.kdb.migrator.diff

import me.santio.kdb.Kdb
import me.santio.kdb.definition.ColumnDefinition
import me.santio.kdb.node.ColumnNode
import java.util.*

class ColumnDiff internal constructor(
    val definition: ColumnDefinition?,
    val node: ColumnNode?
) {

    fun isSame(property: ColumnProperty, kdb: Kdb): Boolean {
        if (node == null || definition == null) return false
        return property.check(kdb, definition, node)
    }

    fun hasDifference(kdb: Kdb, enumSet: EnumSet<ColumnProperty> = EnumSet.allOf(ColumnProperty::class.java)): Boolean {
        if (node == null || definition == null) return true
        return !enumSet.all { isSame(it, kdb) }
    }

    fun priority(): Int {
        if (definition == null || node == null) return 0

        return when {
            definition.autoIncrement && !node.autoIncrement -> 3
            definition.primaryKey && !node.primaryKey -> 2
            definition.unique && !node.unique -> 1
            else -> 0
        }
    }

}