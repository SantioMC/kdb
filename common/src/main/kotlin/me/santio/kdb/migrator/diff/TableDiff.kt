package me.santio.kdb.migrator.diff

import me.santio.kdb.Kdb
import me.santio.kdb.definition.TableDefinition
import me.santio.kdb.node.TableNode

class TableDiff internal constructor(
    val definition: TableDefinition,
    val node: TableNode
) {

    val columns: List<ColumnDiff> by lazy {
        val definitions = definition.columns.associateBy { it.name }
        val nodes = node.columns.associateBy { it.name }

        (definitions.keys + nodes.keys).distinct().map { name ->
            ColumnDiff(definitions[name], nodes[name])
        }.sortedByDescending { it.priority() }
    }

    fun hasDifference(kdb: Kdb) = columns.any { it.hasDifference(kdb) }

}