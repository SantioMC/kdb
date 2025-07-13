package me.santio.kdb.platform.mysql.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.santio.kdb.definition.ColumnDefinition

@Serializable
data class DescriptionModel(
    @SerialName("COLUMN_NAME") val field: String,
    @SerialName("COLUMN_TYPE") val type: String,
    @SerialName("IS_NULLABLE") val nullable: String,
    @SerialName("COLUMN_KEY") val key: String,
    @SerialName("COLUMN_DEFAULT") val default: String?,
    @SerialName("EXTRA") val extra: String,
) {
    val isNullable: Boolean
        get() = nullable.equals("YES", ignoreCase = true)

    fun definition(): ColumnDefinition {
        val extras = extra.split(" ")

        return ColumnDefinition(
            name = field,
            type = type,
            nullable = isNullable,
            primaryKey = key.equals("PRI", ignoreCase = true),
            default = default,
            autoIncrement = extras.contains("auto_increment"),
            unique = key.equals("UNI", ignoreCase = true),
        )
    }

}