package me.santio.kdb.settings

import me.santio.kdb.migrator.MigrationStrategy

/**
 * Migration settings for configuring how Kdb should run migrations
 * @author santio
 */
data class MigrationSettings(
    /**
     * Whether to run the migrator when getting a controller for the first time
     */
    var enabled: Boolean = true,

    /**
     * The table to use and look for when saving migrations and persisting metadata on migrations
     */
    var table: String = "_migrations",

    /**
     * How should conflicts in the migration be handled
     */
    var strategy: MigrationStrategy = MigrationStrategy.Alter,

    /**
     * Whether Kdb should exit the process if the case of a migration failure. If this is enabled
     * and Kdb fails to run a migration, it will exit with error code 74
     */
    var exitOnFailure: Boolean = true,
)