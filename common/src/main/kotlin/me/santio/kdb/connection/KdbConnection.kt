@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

package me.santio.kdb.connection

import me.santio.kdb.Kdb
import me.santio.kdb.connection.transaction.Transaction
import me.santio.kdb.result.KdbResult
import me.santio.kdb.statement.Bindings
import me.santio.kdb.statement.KdbStatement
import org.intellij.lang.annotations.Language
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.sql.Connection
import java.sql.PreparedStatement

class KdbConnection(
    private val kdb: Kdb,
    private val connection: Connection? = null,
) {

    val isTransactionActive: Boolean
        get() = connection != null

    private fun <T> use(consumer: (Connection) -> T): T {
        val connection = this.connection ?: kdb.datasource?.connection

        return connection?.run(consumer)
            ?: error("Failed to get a database connection, did you forget to call #connect()?")
    }

    private fun prepare(connection: Connection, @Language("SQL") sql: String, vararg bindings: Bindings): PreparedStatement {
        println("Executing SQL: ${sql.replace("\n", " ").trim()}")
        if (logger.isDebugEnabled) {
        }

        val statement = KdbStatement(connection, sql, bindings.toList())
        val prepared = statement.build()
        prepared.execute()

        return prepared
    }

    fun execute(@Language("SQL") sql: String, vararg bindings: Bindings) = use { connection ->
        prepare(connection, sql, *bindings).apply {
            close()
            if (!isTransactionActive) connection.close()
        }

        Unit
    }

    fun query(@Language("SQL") sql: String, vararg bindings: Bindings) = use { connection ->
        val prepared = prepare(connection, sql, *bindings)
        val resultSet = prepared.resultSet

        KdbResult(kdb, resultSet) {
            prepared.close()
            if (!isTransactionActive) connection.close()
        }
    }

    fun <T: Any> transaction(action: Transaction.(KdbConnection) -> T): T = use { connection ->
        val transaction = Transaction(KdbConnection(kdb, connection))

        return@use try {
            connection.autoCommit = false
            val result = transaction.action(transaction.connection)

            connection.commit()
            transaction.commit()

            result
        } catch (ex: Exception) {
            connection.rollback()
            transaction.rollback(ex)

            throw ex
        } finally {
            connection.autoCommit = true
            connection.close()
        }
    }

    private companion object {
        private val logger: Logger = LoggerFactory.getLogger(KdbConnection::class.java)
    }

}