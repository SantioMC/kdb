package me.santio.kdb.platform.mysql

import me.santio.kdb.Kdb
import me.santio.kdb.connection.KdbConnection
import me.santio.kdb.platform.Platform
import me.santio.kdb.platform.Resolver
import me.santio.kdb.platform.SqlGenerator

class MysqlPlatform: Platform() {
    override val scheme: String = "mysql"
    override val className: String = "com.mysql.cj.jdbc.Driver"
    override val resolver: Resolver = MysqlResolver
    override val generator: SqlGenerator = MysqlGenerator

    override fun execution(kdb: Kdb, connection: KdbConnection?) = MysqlExecution(kdb, connection)
}