package me.santio.kdb.processor.annotations

import me.santio.kdb.processor.type.OptionalBool

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD, AnnotationTarget.TYPE_PARAMETER)
annotation class Column(
    /**
     * The name of the column inside SQL, defaults to the property name
     */
    val name: String = "__INFERRED",
    /**
     * The SQL data type of the column automatically resolves when not provided
     */
    val type: String = "__INFERRED",
    /**
     * The name to be used when referencing this column in variable notation (:variable), automatically uses
     * the property name when not provided
     */
    val variable: String = "__INFERRED",
    /**
     * Whether this column is a primary key
     */
    val primaryKey: Boolean = false,
    /**
     * Whether the column is required
     */
    val required: OptionalBool = OptionalBool.UNSPECIFIED,
    /**
     * Whether the column is uniquely identifiable
     */
    val unique: Boolean = false,
    /**
     * Whether the column should automatically increment
     */
    val autoIncrement: Boolean = false,
    /**
     * The default value in the database
     */
    val default: String = "",
)