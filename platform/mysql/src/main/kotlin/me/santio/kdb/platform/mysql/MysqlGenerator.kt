package me.santio.kdb.platform.mysql

import me.santio.kdb.node.ColumnNode
import me.santio.kdb.node.TableNode
import me.santio.kdb.platform.SqlGenerator
import me.santio.kdb.query.Where
import me.santio.kdb.statement.Bindings

object MysqlGenerator: SqlGenerator {

    override fun literal(content: String): String {
        check(!content.contains("`")) { "The provided value '$content' contains \"`\" which is not allowed" }

        return "`$content`"
    }

    override fun createColumnDef(column: ColumnNode, includeKeys: Boolean): String {
        val clazz = Class.forName(column.clazz)
        val sqlType = column.type ?: MysqlResolver.getSqlType(clazz) ?: error("Failed to figure out how to resolve ${clazz.simpleName}")
        if (sqlType.contains(" ") || sqlType.contains("`")) error("The provided SQL type is not permitted: $sqlType")

        return buildString {
            append(literal(column.name))
            append(" ")
            append(sqlType)

            if (column.required) append(" NOT NULL")
            if (includeKeys && column.unique) append(" UNIQUE")
            if (includeKeys && column.primaryKey) append(" PRIMARY KEY")
            if (column.autoIncrement) append(" AUTO_INCREMENT")
        }
    }

    override fun createTable(table: TableNode, name: String?) = buildString {
        append("CREATE TABLE IF NOT EXISTS ")
        append(literal(name ?: table.name))
        append("(\n")
        table.columns.forEachIndexed { index, column ->
            if (index > 0) append(",\n")
            append("  ")
            append(createColumnDef(column))
        }
        append("\n)")
    }

    override fun insert(table: TableNode, bindings: Bindings.Variables): String {
        val columns = table.columns.filter { bindings.map.keys.contains(it.variable) }

        return buildString {
            append("INSERT INTO ")
            append(literal(table.name))
            append("(")

            for (column in columns.withIndex()) {
                if (column.index > 0) append(", ")
                append(literal(column.value.name))
            }

            append(") VALUES (")

            for (column in columns.withIndex()) {
                if (column.index > 0) append(", ")
                append(":${column.value.variable}")
            }

            append(")")
        }
    }

    override fun select(table: TableNode, where: Where?, limit: Int?) = buildString {
        append("SELECT * FROM ")
        append(literal(table.name))

        if (where != null) {
            append(" WHERE ")
            append(where.toSql())
        }

        if (limit != null) {
            append(" LIMIT ")
            append(limit)
        }
    }

    override fun drop(table: String) = buildString {
        append("DROP TABLE ")
        append(literal(table))
    }

    override fun renameTable(from: String, to: String) = buildString {
        append("RENAME TABLE ")
        append(literal(from))
        append(" TO ")
        append(literal(to))
    }

    override fun dropColumn(table: String, column: String) = buildString {
        append("ALTER TABLE ")
        append(literal(table))
        append(" DROP COLUMN ")
        append(literal(column))
    }

    override fun addColumn(table: String, column: ColumnNode) = buildString {
        append("ALTER TABLE ")
        append(literal(table))
        append(" ADD ")
        append(createColumnDef(column, includeKeys = true))
    }

    override fun modifyColumn(table: String, column: String, to: ColumnNode) = buildString {
        append("ALTER TABLE ")
        append(literal(table))
        append(" CHANGE ")
        append(literal(column))
        append(" ")
        append(createColumnDef(to, includeKeys = false))
    }

    fun addKey(table: String, column: String, type: KeyType) = buildString {
        append("ALTER TABLE ")
        append(literal(table))
        append(" ADD CONSTRAINT ")

        when (type) {
            KeyType.UniqueKey -> append(literal(column)).append(" ")
            KeyType.PrimaryKey -> {}
        }

        append(type.sql)
        append(" (")
        append(literal(column))
        append(")")
    }

    fun dropKey(table: String, column: String, keyType: KeyType) = buildString {
        append("ALTER TABLE ")
        append(literal(table))
        append(" DROP KEY ")

        when (keyType) {
            KeyType.UniqueKey -> append(literal(column))
            KeyType.PrimaryKey -> append(literal("PRIMARY"))
        }
    }

    enum class KeyType(internal val sql: String) {
        PrimaryKey("PRIMARY KEY"),
        UniqueKey("UNIQUE"),
    }

}
