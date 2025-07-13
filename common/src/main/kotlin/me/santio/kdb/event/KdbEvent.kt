package me.santio.kdb.event

import me.santio.kdb.base.Migration

@Suppress("unused")
sealed interface KdbEvent {

    object OnConnectionOpened : KdbEvent
    object OnConnectionClosed : KdbEvent

    class PreMigrationRun(val migration: Migration) : KdbEvent
    class PostMigrationRun(val migration: Migration) : KdbEvent
    class MigrationFailed(val migration: Migration, exception: Throwable) : KdbEvent

}