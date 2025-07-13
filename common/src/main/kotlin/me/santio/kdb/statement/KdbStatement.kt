package me.santio.kdb.statement

import java.sql.Connection
import java.sql.PreparedStatement

class KdbStatement internal constructor(
    val connection: Connection,
    val statement: String,
    val bindings: List<Bindings>
) {

    private fun compartmentalize(): Pair<String, List<String>> {
        var modified = statement
        val variables = placeholderRegex.findAll(modified)
            .filter { it.groupValues[1].isNotBlank() || it.groupValues[2].isNotBlank() }
            .toList()

        // Quick exit, sortedBy {} is an expensive call (~10ms)
        if (variables.isEmpty()) return modified to emptyList()

        for (variable in variables.sortedBy { -it.range.first }) {
            modified = modified.replaceRange(variable.range.first, variable.range.last + 1, "?")
        }

        return modified to variables.map {
            it.groupValues[1].takeIf { name -> name.isNotBlank() } ?: "?"
        }.toList()
    }

    private fun extractNamedParameters(): Map<String, Any?> {
        return bindings.filterIsInstance<Bindings.Variables>()
            .map { it.map }
            .flatMap { it.entries }
            .associate { it.toPair() }
    }

    private fun extractPositionalParameters(): MutableList<Any?> {
        return bindings.filterIsInstance<Bindings.Classic>()
            .flatMap { it.values }
            .toMutableList()
    }

    fun build(): PreparedStatement {
        val (sqlStatement, parameterNames) = compartmentalize()
        val preparedStatement = runCatching { connection.prepareStatement(sqlStatement) }
            .getOrElse { ex -> error("Failed to create prepared statement: $sqlStatement \n $ex") }

        val namedParameters = extractNamedParameters()
        val positionalParameters = extractPositionalParameters()

        parameterNames.forEachIndexed { index, paramName ->
            val parameterIndex = index + 1

            when {
                paramName != "?" -> {
                    if (!namedParameters.containsKey(paramName)) error("Expected binding for parameter '$paramName' but none was specified.")
                    preparedStatement.setObject(parameterIndex, namedParameters[paramName])
                }
                else -> {
                    if (positionalParameters.isEmpty()) error("Expected binding for positional parameter ${index + 1}, but none was specified.")
                    preparedStatement.setObject(parameterIndex, positionalParameters.removeFirst())
                }
            }
        }

        return preparedStatement
    }

    private companion object {
        val placeholderRegex = "'(?:\\\\'|[^'])*'|\"(?:\\\\\"|[^\"])*\"|`(?:\\\\`|[^`])*`|::?(\\w+)|(\\?)".toRegex()
    }

}