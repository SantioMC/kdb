package me.santio.kdb.platform

import me.santio.kdb.Kdb
import me.santio.kdb.base.Migration
import me.santio.kdb.connection.KdbConnection
import me.santio.kdb.connection.transaction.Transactional
import me.santio.kdb.definition.ColumnDefinition
import me.santio.kdb.definition.TableDefinition
import me.santio.kdb.migrator.diff.ColumnDiff
import me.santio.kdb.migrator.diff.TableDiff
import me.santio.kdb.node.ColumnNode
import kotlin.time.Duration

/**
 * Kdb requires querying and executing actual SQL on the backing datastore to perform properly. This
 * implementation needs to be implemented for various features to work.
 *
 * @author santio
 */
abstract class Execution(override val kdb: Kdb, override val connection: KdbConnection?): Transactional() {

    abstract suspend fun createMigrationTable(name: String)
    abstract suspend fun trackMigration(table: String, duration: Duration, migration: Migration)

    abstract suspend fun define(table: String): TableDefinition?

    open suspend fun alterColumn(table: String, diff: ColumnDiff) {
        if (!diff.hasDifference(kdb)) return

        if (diff.definition == null && diff.node == null) throw IllegalArgumentException("from and to cannot be null, one must be set!")
        val generator = kdb.platform.generator

        if (diff.definition == null) execute(generator.addColumn(table, diff.node!!))
        if (diff.node == null) execute(generator.dropColumn(table, diff.definition!!.name))

        modifyColumn(table, diff, diff.definition!!, diff.node!!)
    }

    protected open suspend fun modifyColumn(
        table: String,
        diff: ColumnDiff,
        definition: ColumnDefinition,
        node: ColumnNode
    ) {
        execute(kdb.platform.generator.modifyColumn(table, definition.name, node))
    }

    open suspend fun alterTable(table: String, diff: TableDiff) {
        if (!diff.hasDifference(kdb)) return

        diff.columns.forEach { column ->
            alterColumn(table, column)
        }
    }

}