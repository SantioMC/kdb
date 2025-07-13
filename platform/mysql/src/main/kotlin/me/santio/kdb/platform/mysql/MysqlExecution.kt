package me.santio.kdb.platform.mysql

import me.santio.kdb.Kdb
import me.santio.kdb.base.Migration
import me.santio.kdb.connection.KdbConnection
import me.santio.kdb.definition.ColumnDefinition
import me.santio.kdb.definition.TableDefinition
import me.santio.kdb.migrator.diff.ColumnDiff
import me.santio.kdb.migrator.diff.ColumnProperty
import me.santio.kdb.node.ColumnNode
import me.santio.kdb.platform.Execution
import me.santio.kdb.platform.mysql.MysqlGenerator.literal
import me.santio.kdb.platform.mysql.models.DescriptionModel
import me.santio.kdb.statement.bind
import java.time.Instant
import java.util.*
import kotlin.time.Duration

class MysqlExecution(override val kdb: Kdb, override val connection: KdbConnection?): Execution(kdb, connection) {
    override suspend fun define(table: String): TableDefinition? = runCatching {
        val columns = query("DESCRIBE ${literal(table)}").all<DescriptionModel>()
        return TableDefinition(
            name = table,
            columns = columns.map { it.definition() }.toList()
        )
    }.getOrNull()

    override suspend fun createMigrationTable(name: String) {
        execute("""
            CREATE TABLE IF NOT EXISTS ${literal(name)}(
                id VARCHAR(255) NOT NULL,
                installed INT(11) UNSIGNED NOT NULL,
                execution_time INT(11) NOT NULL
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

    override suspend fun modifyColumn(
        table: String,
        diff: ColumnDiff,
        definition: ColumnDefinition,
        node: ColumnNode
    ) {
        val keys = mapOf(
            ColumnProperty.PrimaryKey to node.primaryKey,
            ColumnProperty.UniqueKey to node.unique
        ).filter { !diff.isSame(it.key, kdb) }.mapKeys { PropertyMapping[it.key]!! }

        for ((key, value) in keys) {
            if (value) execute(MysqlGenerator.addKey(table, node.name, key))
        }

        if (diff.hasDifference(kdb, NonKeys)) {
            super.modifyColumn(table, diff, definition, node)
        }

        for ((key, value) in keys) {
            if (!value) execute(MysqlGenerator.dropKey(table, node.name, key))
        }
    }

    private companion object {
        val NonKeys: EnumSet<ColumnProperty> = EnumSet.allOf(ColumnProperty::class.java).apply {
            remove(ColumnProperty.PrimaryKey)
            remove(ColumnProperty.UniqueKey)
        }

        val PropertyMapping = mapOf(
            ColumnProperty.PrimaryKey to MysqlGenerator.KeyType.PrimaryKey,
            ColumnProperty.UniqueKey to MysqlGenerator.KeyType.UniqueKey,
        )
    }

}
