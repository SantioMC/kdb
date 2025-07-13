package me.santio.kdb.connection

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import me.santio.kdb.connection.transaction.Transaction
import me.santio.kdb.statement.Bindings
import org.intellij.lang.annotations.Language

@Suppress("unused")
class SuspendingConnection(
    private val dispatcher: CoroutineDispatcher,
    private val connection: KdbConnection
) {

    suspend fun execute(@Language("SQL") sql: String, vararg bindings: Bindings) = withContext(dispatcher) {
        connection.execute(sql, *bindings)
    }

    suspend fun query(@Language("SQL") sql: String, vararg bindings: Bindings) = withContext(dispatcher) {
        connection.query(sql, *bindings)
    }

    suspend fun <T: Any> transaction(action: suspend Transaction.(KdbConnection) -> T): T = withContext(dispatcher) {
        connection.transaction {
            runBlocking {
                action(connection)
            }
        }
    }

}