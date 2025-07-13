package me.santio.kdb.settings

/**
 * Cache settings related to HikariCP
 * @author santio
 */
data class CacheSettings(
    /**
     * Whether to cache prepared statements
     */
    var enabled: Boolean = true,

    /**
     * The number of prepared statements that should be cached
     */
    var cacheSize: Int = 250,

    /**
     * The maximum length of an SQL statement can be for a cached statement
     */
    var sqlSize: Int = 2048
)