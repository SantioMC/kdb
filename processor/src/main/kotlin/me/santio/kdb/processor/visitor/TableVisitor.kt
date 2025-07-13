package me.santio.kdb.processor.visitor

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration
import me.santio.kdb.node.TableNode
import me.santio.kdb.processor.annotations.Table
import me.santio.kdb.processor.helper.annotation
import me.santio.kdb.processor.helper.value

class TableVisitor(logger: KSPLogger): KSPConverter<KSClassDeclaration, TableNode> {

    val columnVisitor = ColumnVisitor(logger)

    override fun parse(node: KSClassDeclaration): TableNode {
        val annotation = node.annotation<Table>() ?: error("Failed to find @Table annotation on class ${node.qualifiedName?.asString()}")
        val columns = node.getAllProperties().map { columnVisitor.parse(it) }.toList()

        return TableNode(
            annotation.value(Table::name)?.takeIf { it != "__INFERRED" } ?: node.simpleName.asString(),
            node.qualifiedName!!.asString(),
            columns
        )
    }

}