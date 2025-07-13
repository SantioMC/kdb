package me.santio.kdb.node

import kotlinx.serialization.Serializable

/**
 * A representation of an SQL column
 * @author santio
 */
@Suppress("unused")
@Serializable
data class ColumnNode(
    /**
     * The effective SQL name for this column
     */
    val name: String,
    /**
     * The property name used to represent the column
     */
    val reference: String,
    /**
     * The SQL type to use for the column, or null if not specified manually
     */
    val type: String?,
    /**
     * The name to be used when referencing this column in variable notation (:variable), automatically uses
     * the property name when not provided
     */
    val variable: String,
    /**
     * The class name of the type used to represent the property
     */
    val clazz: String,
    /**
     * Whether this column is a primary key
     */
    var primaryKey: Boolean,
    /**
     * Whether this column is required
     */
    val required: Boolean,
    /**
     * Whether this column is unique
     */
    val unique: Boolean,
    /**
     * Whether this column is auto incrementing
     */
    val autoIncrement: Boolean,
) {

    fun resolved(): Class<*> = Class.forName(clazz)

}