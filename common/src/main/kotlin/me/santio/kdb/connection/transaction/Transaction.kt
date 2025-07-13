package me.santio.kdb.connection.transaction

import me.santio.kdb.connection.KdbConnection
import java.util.function.Consumer

@Suppress("unused")
class Transaction internal constructor(
    val connection: KdbConnection,
) {

    private var committed = mutableListOf<Runnable>()
    private var rollback = mutableListOf<Consumer<Exception>>()

    fun onSuccess(block: Runnable) {
        committed += block
    }

    fun onFailure(block: Consumer<Exception>) {
        rollback += block
    }

    internal fun commit() {
        committed.onEach { it.run() }
    }

    internal fun rollback(error: Exception) {
        rollback.onEach { it.accept(error) }
    }

}
