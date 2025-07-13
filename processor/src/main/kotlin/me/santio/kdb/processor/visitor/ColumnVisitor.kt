package me.santio.kdb.processor.visitor

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import me.santio.kdb.node.ColumnNode
import me.santio.kdb.processor.annotations.Column
import me.santio.kdb.processor.helper.annotation
import me.santio.kdb.processor.helper.value

class ColumnVisitor(private val logger: KSPLogger): KSPConverter<KSPropertyDeclaration, ColumnNode> {

    override fun parse(node: KSPropertyDeclaration): ColumnNode {
        val annotation = node.annotation<Column>()
        val isNullable = node.type.resolve().isMarkedNullable
        val clazz = node.type.resolve().declaration.qualifiedName!!.asString()
        val isPrimaryKey = annotation?.value(Column::primaryKey) ?: false

        return ColumnNode(
            name = annotation?.value(Column::name)?.takeIf { it != "__INFERRED" } ?: node.simpleName.asString(),
            reference = node.qualifiedName!!.asString(),
            type = annotation?.value(Column::type)?.takeIf { it != "__INFERRED" },
            variable = annotation?.value(Column::variable)?.takeIf { it != "__INFERRED" } ?: node.simpleName.asString(),
            clazz = toJavaClass(clazz),
            primaryKey = isPrimaryKey,
            required = annotation?.value(Column::required)?.orElse(!isNullable) ?: !isNullable,
            unique = annotation?.value(Column::unique)?.takeIf { !isPrimaryKey } ?: false,
            autoIncrement = annotation?.value(Column::autoIncrement) ?: false,
        )
    }

    private fun toJavaClass(clazz: String): String {
        return when (clazz) {
            String::class.qualifiedName -> String::class.javaObjectType.name
            Int::class.qualifiedName -> Int::class.javaObjectType.name
            Boolean::class.qualifiedName -> Boolean::class.javaObjectType.name
            Byte::class.qualifiedName -> Byte::class.javaObjectType.name
            Short::class.qualifiedName -> Short::class.javaObjectType.name
            Long::class.qualifiedName -> Long::class.javaObjectType.name
            Float::class.qualifiedName -> Float::class.javaObjectType.name
            else -> clazz
        }
    }

}