package me.santio.kdb

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import me.santio.kdb.connection.DeferredConnection
import me.santio.kdb.connection.KdbConnection
import me.santio.kdb.connection.SuspendingConnection
import me.santio.kdb.connection.transaction.Transaction
import me.santio.kdb.controller.Controller
import me.santio.kdb.event.EventBus
import me.santio.kdb.event.KdbEvent
import me.santio.kdb.locator.TableLocator
import me.santio.kdb.migrator.KdbMigrator
import me.santio.kdb.platform.Platform
import me.santio.kdb.result.ResultSetMapper
import me.santio.kdb.settings.KdbSettings
import me.santio.kdb.statement.Bindings
import org.intellij.lang.annotations.Language

@Suppress("unused")
class Kdb internal constructor(
    val uri: String,
    val settings: KdbSettings,
    val platform: Platform
) {

    val migrator = KdbMigrator(this)
    val events = EventBus<KdbEvent>()

    internal val mapper = ResultSetMapper(this)
    private val kdbConnection = KdbConnection(this)
    private val controllerCache = mutableMapOf<Class<*>, Controller<*>>()

    var datasource: HikariDataSource? = null
        private set

    suspend fun connect(): Kdb {
        val config = HikariConfig()

        config.jdbcUrl = "jdbc:$uri"
        settings.username?.let { config.username = it }
        settings.password?.let { config.password = it }

        config.addDataSourceProperty("cachePrepStmts", settings.cache.enabled)
        config.addDataSourceProperty("prepStmtCacheSize", settings.cache.cacheSize)
        config.addDataSourceProperty("prepStmtCacheSqlLimit", settings.cache.sqlSize)

        this.datasource = HikariDataSource(config)

        // Run Migrations
        if (settings.migration.enabled) {
            this.migrator.migrate()
        }

        // Setup controllers
        if (settings.preloadCache) {
            TableLocator.load()
        }

        this.events.call(KdbEvent.OnConnectionOpened)

        Runtime.getRuntime().addShutdownHook(Thread { close() })
        return this
    }

    fun close() {
        if (this.datasource?.isClosed != false) return
        this.events.call(KdbEvent.OnConnectionClosed)
        this.datasource?.close()
    }

    @Suppress("UNCHECKED_CAST")
    suspend fun <C: Any> controller(clazz: Class<C>): Controller<C> = controllerCache.getOrPut(clazz) {
        val controller = Controller(clazz, this)

        if (settings.migration.enabled) {
            migrator.migrate(controller)
        }

        return@getOrPut controller
    } as Controller<C>

    suspend inline fun <reified C: Any> controller() = controller(C::class.java)

    // Expose different execution functions
    fun blocking() = kdbConnection
    fun suspending() = SuspendingConnection(settings.dispatcher, kdbConnection)
    fun deferred() = DeferredConnection(settings.dispatcher, kdbConnection)

    // Expose suspending functions by default for ease of use
    suspend fun execute(@Language("SQL") sql: String, vararg bindings: Bindings) = suspending().execute(sql, *bindings)
    suspend fun query(@Language("SQL") sql: String, vararg bindings: Bindings) = suspending().query(sql, *bindings)
    suspend fun <T: Any> transaction(action: suspend Transaction.(KdbConnection) -> T): T = suspending().transaction(action)

//    @JvmName("innerTransaction")
//    context(transaction: Transaction)
//    suspend fun <T: Any> transaction(action: suspend Transaction.(KdbConnection) -> T): T = transaction.action(transaction.connection)
//
//    @JvmName("executeTransactional")
//    context(transaction: Transaction)
//    fun execute(@Language("SQL") sql: String, vararg bindings: Bindings) = transaction.connection.execute(sql, *bindings)
//
//    @JvmName("queryTransactional")
//    context(transaction: Transaction)
//    fun query(@Language("SQL") sql: String, vararg bindings: Bindings) = transaction.connection.query(sql, *bindings)

    // Table resolver
    inline fun <reified T: Any> table() = table(T::class.java)
    fun <T: Any> table(clazz: Class<T>) = TableLocator.table(
        this,
        TableLocator.node(clazz) ?: error("Failed to find SQL table for ${clazz.simpleName}, make sure this is class is annotated with @Table and that Kdb's processor is running"),
        clazz
    )

    /**
     * Empty companion object to allow for extension methods to be added
     */
    companion object
}

fun Kdb(uri: String, settings: KdbSettings.() -> Unit = {}): Kdb {
    var uri = uri.removePrefix("jdbc:")
    var scheme = uri.substring(0, uri.indexOf("://"))
    uri = uri.removePrefix("$scheme://")

    // Remapping
    scheme = when (scheme) {
        "mariadb" -> "mysql"
        "psql" -> "postgresql"
        else -> scheme
    }

    return Kdb(
        "$scheme://$uri",
        KdbSettings().apply(settings),
        Platform.forUri("$scheme://$uri")
    )
}