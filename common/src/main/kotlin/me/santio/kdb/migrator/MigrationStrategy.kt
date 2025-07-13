package me.santio.kdb.migrator

import me.santio.kdb.Kdb
import me.santio.kdb.controller.Controller

/**
 * Defines how conflicts in migrations should be resolved.
 */
sealed interface MigrationStrategy {

    suspend fun migrate(kdb: Kdb, controller: Controller<*>)

    object Alter: MigrationStrategy {
        override suspend fun migrate(kdb: Kdb, controller: Controller<*>) {
            controller.transaction {
                onFailure { err ->
                    error("Failed to migrate ${controller.table.node.name}, please write a manual migration instead. \n $err")
                }

                val definition = it.define()
                    ?: return@transaction it.create()

                val diff = definition.diff(controller.table.node)

                kdb.platform.execution(kdb, connection).alterTable(
                    it.table.node.name,
                    diff
                )
            }
        }
    }

    object DropRecreate: MigrationStrategy {
        override suspend fun migrate(kdb: Kdb, controller: Controller<*>) {
            controller.transaction {
                if (it.define() != null) {
                    it.drop()
                }

                it.create()
            }
        }
    }

}