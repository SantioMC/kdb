package me.santio.kdb.controller

import me.santio.kdb.Kdb
import me.santio.kdb.connection.KdbConnection
import me.santio.kdb.connection.transaction.Transaction
import me.santio.kdb.connection.transaction.Transactional
import me.santio.kdb.definition.TableDefinition
import me.santio.kdb.query.Condition
import me.santio.kdb.query.Filter
import me.santio.kdb.query.Where
import me.santio.kdb.statement.Bindings

@Suppress("unused")
class Controller<Table: Any> internal constructor(
    private val clazz: Class<Table>,
    override val kdb: Kdb,
    override val connection: KdbConnection? = null,
): Transactional() {

    internal val table: ResolvedTable<Table> = kdb.table(clazz)

    fun bindings(entity: Table) = Bindings.resolve(kdb, clazz, entity)

    fun definition(): String {
        return kdb.platform.generator.createTable(table.node)
    }

    suspend fun define(): TableDefinition? {
        return kdb.platform.execution(kdb, connection).define(table.node.name)
    }

    suspend fun save(entity: Table) {
        val bindings = Bindings.resolve(kdb, clazz, entity)
        execute(kdb.platform.generator.insert(table.node, bindings), bindings)
    }

    suspend fun select(limit: Int? = null, filter: (Condition<Table>.() -> Filter)? = null): Sequence<Table> {
        val condition = Condition(kdb, table)
        val where = filter?.let { Where(condition, condition.it()) }
        val bindings = where?.let { arrayOf(where.bindings()) } ?: emptyArray()

        return query(
            kdb.platform.generator.select(table.node, where, limit),
            *bindings
        ).all(clazz)
    }

    internal suspend fun create() {
        execute(kdb.platform.generator.createTable(table.node))
    }

    suspend fun drop() {
        execute(kdb.platform.generator.drop(table.node.name))
    }

    suspend fun firstOrNull(filter: (Condition<Table>.() -> Filter)? = null) = select(filter = filter, limit = 1).firstOrNull()
    suspend fun first(filter: (Condition<Table>.() -> Filter)? = null) = firstOrNull(filter = filter) ?: error("No results were found")

    suspend fun transaction(block: suspend Transaction.(Controller<Table>) -> Unit) {
        kdb.transaction { connection ->
            val controller = Controller(clazz, kdb, connection)
            block(controller)
        }
    }

}