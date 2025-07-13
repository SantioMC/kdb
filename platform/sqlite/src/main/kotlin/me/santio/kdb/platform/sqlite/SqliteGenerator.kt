package me.santio.kdb.platform.sqlite

import me.santio.kdb.node.ColumnNode
import me.santio.kdb.node.TableNode
import me.santio.kdb.platform.SqlGenerator
import me.santio.kdb.query.Where
import me.santio.kdb.statement.Bindings

object SqliteGenerator: SqlGenerator {

    override fun literal(content: String): String {
        check(!content.contains('"')) { "The provided value '$content' contains '\"' which is not allowed" }

        return "\"$content\""
    }

    override fun createColumnDef(column: ColumnNode, includeKeys: Boolean): String {
        val clazz = Class.forName(column.clazz)
        val sqlType = column.type ?: SqliteResolver.getSqlType(clazz) ?: error("Failed to figure out how to resolve ${clazz.simpleName}")
        if (sqlType.contains(" ") || sqlType.contains("\"")) error("The provided SQL type is not permitted: $sqlType")

        return buildString {
            append(literal(column.name))
            append(" ")
            append(sqlType)

            if (column.required) append(" NOT NULL")
            if (includeKeys && column.unique) append(" UNIQUE")
            if (includeKeys && column.primaryKey) append(" PRIMARY KEY")
            if (column.autoIncrement) append(" AUTOINCREMENT")
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
        append("ALTER TABLE ")
        append(literal(from))
        append(" RENAME TO ")
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
        append(" ADD COLUMN ")
        append(createColumnDef(column))
    }

    override fun modifyColumn(table: String, column: String, to: ColumnNode): String {
        throw UnsupportedOperationException("Sqlite doesn't support altering columns")
    }

    fun copy(from: String, to: String, columns: List<String>) = buildString {
        append("INSERT INTO ")
        append(literal(to))
        append("(")

        for (column in columns.withIndex()) {
            if (column.index > 0) append(", ")
            append(literal(column.value))
        }

        append(")\n")

        append("SELECT ")

        for (column in columns.withIndex()) {
            if (column.index > 0) append(", ")
            append(literal(column.value))
        }

        append(" FROM ")
        append(literal(from))
    }

}
