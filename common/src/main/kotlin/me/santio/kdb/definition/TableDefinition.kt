package me.santio.kdb.definition

import me.santio.kdb.migrator.diff.TableDiff
import me.santio.kdb.node.TableNode

data class TableDefinition(
    val name: String,
    val columns: List<ColumnDefinition>
) {
    fun diff(node: TableNode) = TableDiff(this, node)
}