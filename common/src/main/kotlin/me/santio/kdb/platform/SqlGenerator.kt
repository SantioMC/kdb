package me.santio.kdb.platform

import me.santio.kdb.node.ColumnNode
import me.santio.kdb.node.TableNode
import me.santio.kdb.query.Where
import me.santio.kdb.statement.Bindings

/**
 * Generates various statements for different operations. This is to cover the differences between different
 * database management systems and provide a consistent interface when interacting with databases.
 *
 * @author santio
 */
interface SqlGenerator {

    fun literal(content: String): String

    fun createColumnDef(column: ColumnNode, includeKeys: Boolean = true): String
    fun createTable(table: TableNode, name: String? = null): String
    fun insert(table: TableNode, bindings: Bindings.Variables): String
    fun select(table: TableNode, where: Where?, limit: Int?): String
    fun drop(table: String): String
    fun renameTable(from: String, to: String): String

    fun dropColumn(table: String, column: String): String
    fun addColumn(table: String, column: ColumnNode): String
    fun modifyColumn(table: String, column: String, to: ColumnNode): String

}