package me.santio.kdb.connection.transaction

import me.santio.kdb.Kdb
import me.santio.kdb.connection.KdbConnection
import me.santio.kdb.statement.Bindings
import org.intellij.lang.annotations.Language

abstract class Transactional {

    protected abstract val kdb: Kdb
    protected abstract val connection: KdbConnection?

    protected suspend fun execute(@Language("SQL") sql: String, vararg bindings: Bindings) =
        connection?.execute(sql, *bindings) ?: kdb.execute(sql, *bindings)

    protected suspend fun query(@Language("SQL") sql: String, vararg bindings: Bindings) =
        connection?.query(sql, *bindings) ?: kdb.query(sql, *bindings)

}