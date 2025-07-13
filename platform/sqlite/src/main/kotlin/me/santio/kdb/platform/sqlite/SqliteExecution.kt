package me.santio.kdb.platform.sqlite

import me.santio.kdb.Kdb
import me.santio.kdb.base.Migration
import me.santio.kdb.connection.KdbConnection
import me.santio.kdb.definition.ColumnDefinition
import me.santio.kdb.definition.TableDefinition
import me.santio.kdb.migrator.diff.ColumnDiff
import me.santio.kdb.migrator.diff.TableDiff
import me.santio.kdb.platform.Execution
import me.santio.kdb.platform.sqlite.SqliteGenerator.literal
import me.santio.kdb.statement.bind
import java.time.Instant
import kotlin.time.Duration

class SqliteExecution(override val kdb: Kdb, override val connection: KdbConnection?): Execution(kdb, connection) {

    override suspend fun define(table: String): TableDefinition? {
        val sql = query(
            "SELECT sql FROM sqlite_master WHERE name = :name AND type = :type",
            bind {
                "name" to table
                "type" to "table"
            }
        ).singleOrNull<String>() ?: return null

        val columns = sql.substring(sql.indexOf('(') + 1, sql.indexOf(')'))
            .split(",")
            .map { it.trim() }

        return TableDefinition(
            name = table,
            columns = columns.map {
                val keywords = it.split(" ").mapIndexed { index, name ->
                    if (index > 0) name.uppercase() else name
                }

                val defaultIndex = keywords.indexOf("DEFAULT").takeIf { index -> index != -1 }

                return@map ColumnDefinition(
                    name = keywords.first().removeSurrounding("\""),
                    type = keywords[1],
                    primaryKey = it.uppercase().contains("PRIMARY KEY", ignoreCase = true),
                    autoIncrement = keywords.contains("AUTOINCREMENT"),
                    nullable = it.contains("NOT NULL", ignoreCase = true),
                    default = defaultIndex?.let { keywords[defaultIndex + 1] },
                    unique = keywords.contains("UNIQUE"),
                )
            }
        )
    }

    override suspend fun createMigrationTable(name: String) {
        execute("""
            CREATE TABLE IF NOT EXISTS ${literal(name)}(
                id TEXT NOT NULL,
                installed INTEGER NOT NULL,
                execution_time INTEGER NOT NULL
            )
        """.trimIndent())
    }

    override suspend fun trackMigration(table: String, duration: Duration, migration: Migration) {
        execute(
            """
                INSERT INTO ${literal(table)}(id, installed, execution_time) 
                VALUES(:id, :installed, :execution_time)
            """.trimIndent(),

            bind {
                "id" to migration.id
                "installed" to Instant.now().epochSecond
                "execution_time" to duration.inWholeMilliseconds
            }
        )
    }

    override suspend fun alterColumn(table: String, diff: ColumnDiff) {
        throw UnsupportedOperationException("Sqlite doesn't support altering columns")
    }

    override suspend fun alterTable(table: String, diff: TableDiff) {
        if (!diff.hasDifference(kdb)) return

        val tempTable = "${table}_kdb_tmp"
        val columns = diff.definition.columns.map { it.name }

        execute(SqliteGenerator.createTable(diff.node, tempTable))
        execute(SqliteGenerator.copy(diff.definition.name, tempTable, columns))
        execute(SqliteGenerator.drop(diff.definition.name))
        execute(SqliteGenerator.renameTable(tempTable, diff.node.name))
    }

}
