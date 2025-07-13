package me.santio.kdb.migrator

import me.santio.kdb.connection.KdbConnection
import me.santio.kdb.connection.transaction.Transaction
import me.santio.kdb.base.Migration

@Suppress("UnusedReceiverParameter") // - Used to scope access
fun Migration.migrate(action: suspend Transaction.(KdbConnection) -> Unit): suspend Transaction.(KdbConnection) -> Unit {
    return action
}
