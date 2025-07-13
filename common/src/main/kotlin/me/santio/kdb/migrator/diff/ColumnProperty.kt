package me.santio.kdb.migrator.diff

import me.santio.kdb.Kdb
import me.santio.kdb.definition.ColumnDefinition
import me.santio.kdb.node.ColumnNode

@Suppress("unused")
enum class ColumnProperty(
    val check: (Kdb, ColumnDefinition, ColumnNode) -> Boolean
) {

    Nullability({ _, def, node -> def.nullable != node.required }),
    AutoIncrement({ _, def, node -> def.autoIncrement == node.autoIncrement }),
    PrimaryKey({ _, def, node -> def.primaryKey == node.primaryKey }),
    UniqueKey({ _, def, node -> def.unique == node.unique }),
    Name({ _, def, node -> def.name == node.name }),

    Type({ kdb, def, node ->
        val clazz = Class.forName(node.clazz)
        val type = node.type ?: kdb.platform.resolver.getSqlType(clazz)
        def.type == type
    }),
    ;

}