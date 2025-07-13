package me.santio.kdb.connection

import kotlinx.coroutines.*
import me.santio.kdb.connection.transaction.Transaction
import me.santio.kdb.statement.Bindings
import org.intellij.lang.annotations.Language

@Suppress("unused")
class DeferredConnection(
    dispatcher: CoroutineDispatcher,
    private val connection: KdbConnection
) {

    private val scope = CoroutineScope(dispatcher + SupervisorJob())

    fun execute(@Language("SQL") sql: String, vararg bindings: Bindings) = scope.async {
        connection.execute(sql, *bindings)
    }

    fun query(@Language("SQL") sql: String, vararg bindings: Bindings) = scope.async {
        connection.query(sql, *bindings)
    }

    fun <T: Any> transaction(action: suspend Transaction.(KdbConnection) -> T): Deferred<T> = scope.async {
        connection.transaction {
            runBlocking {
                action(connection)
            }
        }
    }

}