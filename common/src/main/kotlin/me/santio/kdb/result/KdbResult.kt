package me.santio.kdb.result

import kotlinx.serialization.Contextual
import me.santio.kdb.Kdb
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.sql.ResultSet

@Suppress("unused")
class KdbResult(
    private val kdb: Kdb,
    val resultSet: ResultSet,
    private val onClose: () -> Unit
): AutoCloseable {

    var keepOpen: Boolean = false

    inline fun <reified T: @Contextual Any> all(close: Boolean = true) = all(T::class.java, close)
    fun <T: @Contextual Any> all(clazz: Class<T>, close: Boolean = true): Sequence<T> {
        check(!resultSet.isClosed) { "ResultSet is closed" }

        return sequence {
            try {
                while (resultSet.next()) {
                    val mapped = kdb.mapper.parse(resultSet, clazz)
                    if (mapped != null) yield(mapped)
                }
            } finally {
                attemptClose(close)
            }
        }
    }

    inline fun <reified T: @Contextual Any> firstOrNull(close: Boolean = true) = firstOrNull(T::class.java, close)
    fun <T: @Contextual Any> firstOrNull(clazz: Class<T>, close: Boolean = true): T? {
        check(!resultSet.isClosed) { "ResultSet is closed" }

        if (!resultSet.next()) return null
        return kdb.mapper.parse(resultSet, clazz).apply {
            attemptClose(close)
        }
    }

    inline fun <reified T: @Contextual Any> singleOrNull(close: Boolean = true) = singleOrNull(T::class.java, close)
    fun <T: @Contextual Any> singleOrNull(clazz: Class<T>, close: Boolean = true): T? {
        check(!resultSet.isClosed) { "ResultSet is closed" }

        if (!resultSet.next()) return null

        val value = kdb.mapper.parse(resultSet, clazz)

        if (resultSet.next()) {
            error("Expected to find one result, but found more than one")
        }

        attemptClose(close)
        return value
    }

    inline fun <reified T: @Contextual Any> first(close: Boolean = true) = first(T::class.java, close)
    fun <T: @Contextual Any> first(clazz: Class<T>, close: Boolean = true): T = firstOrNull(clazz, close)
        ?: error("Expected to find one result, but found none")

    inline fun <reified T: @Contextual Any> single(close: Boolean = true) = single(T::class.java, close)
    fun <T: @Contextual Any> single(clazz: Class<T>, close: Boolean = true): T = singleOrNull(clazz, close)
        ?: error("Expected to find one result, but found none")

    fun columns(): List<String> {
        check(!resultSet.isClosed) { "ResultSet is closed" }

        return resultSet.metaData.let {
            (1..it.columnCount).map { index -> it.getColumnName(index) }
        }
    }

    fun <R: Any> use(block: (KdbResult) -> R): R {
        keepOpen = true
        return block(this)
    }

    private fun attemptClose(close: Boolean) {
        if (close && !keepOpen) this.close()
    }

    override fun close() {
        if (logger.isDebugEnabled) {
            logger.debug("Closed the KdbResult object, along with all dependent items")
        }

        resultSet.close()
        this.onClose()
    }

    private companion object {
        private val logger: Logger = LoggerFactory.getLogger(KdbResult::class.java)
    }
}