package me.santio.kdb.definition

data class ColumnDefinition(
    val name: String,
    val type: String,
    val nullable: Boolean,
    val primaryKey: Boolean,
    val unique: Boolean,
    val default: String?,
    val autoIncrement: Boolean,
)