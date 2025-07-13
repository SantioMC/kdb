package me.santio.kdb.node

import kotlinx.serialization.Serializable

/**
 * A representation of a table class defined
 * @author santio
 */
@Serializable
data class TableNode(
    /**
    * The effective name of the SQL table
    */
    val name: String,
    /**
     * The full class name referencing the defining class
     */
    val reference: String,
    /**
     * The list of column representations
     */
    val columns: List<ColumnNode>
)