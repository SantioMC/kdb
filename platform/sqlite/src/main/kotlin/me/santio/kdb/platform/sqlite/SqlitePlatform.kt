package me.santio.kdb.platform.sqlite

import me.santio.kdb.Kdb
import me.santio.kdb.connection.KdbConnection
import me.santio.kdb.platform.Platform
import me.santio.kdb.platform.Resolver
import me.santio.kdb.platform.SqlGenerator

class SqlitePlatform: Platform() {
    override val scheme: String = "sqlite"
    override val className: String = "org.sqlite.JDBC"
    override val resolver: Resolver = SqliteResolver
    override val generator: SqlGenerator = SqliteGenerator

    override fun execution(kdb: Kdb, connection: KdbConnection?) = SqliteExecution(kdb, connection)
}