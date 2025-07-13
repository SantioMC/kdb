package me.santio.kdb.migrator

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.santio.kdb.Kdb
import me.santio.kdb.controller.Controller
import me.santio.kdb.event.KdbEvent
import me.santio.kdb.locator.KdbFileLocator
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.system.exitProcess
import kotlin.time.TimeSource

class KdbMigrator internal constructor(
    private val kdb: Kdb
) {

    /**
     * Find and run all user-defined manual migrations
     * @see me.santio.kdb.base.Migration
     */
    suspend fun migrate(classLoader: ClassLoader = Thread.currentThread().contextClassLoader) = withContext(Dispatchers.IO) {
        val migrations = KdbFileLocator.migrations(classLoader)
        val execution = kdb.platform.execution(kdb)
        var createdTable = false

        migrations.forEach { migration ->
            kdb.events.call(KdbEvent.PreMigrationRun(migration))

            val started = TimeSource.Monotonic.markNow()

            runCatching {
                kdb.transaction {
                    migration.apply { this@transaction.up(it) }
                }
            }.onSuccess {
                if (!createdTable) {
                    execution.createMigrationTable(kdb.settings.migration.table)
                    createdTable = true
                }

                execution.trackMigration(
                    kdb.settings.migration.table,
                    TimeSource.Monotonic.markNow() - started,
                    migration
                )

                kdb.events.call(KdbEvent.PostMigrationRun(migration))
            }.onFailure { error ->
                logger.error("Failed to run migration '{}'", migration.id, error)
                kdb.events.call(KdbEvent.MigrationFailed(migration, error))

                if (kdb.settings.migration.exitOnFailure) {
                    exitProcess(74)
                }
            }
        }
    }

    /**
     * Automatically attempt to migrate changes made in a model
     */
    suspend fun migrate(controller: Controller<*>) {
        logger.debug("Starting migration on ${controller.table.node.name}...")

        val strategy = kdb.settings.migration.strategy
        strategy.migrate(kdb, controller)
    }

    private companion object {
        val logger: Logger = LoggerFactory.getLogger(KdbMigrator::class.java)
    }

}