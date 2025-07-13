package me.santio.kdb.result

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.json.*
import kotlinx.serialization.serializerOrNull
import me.santio.kdb.Kdb
import me.santio.kdb.controller.ResolvedTable
import me.santio.kdb.locator.TableLocator.node
import java.sql.ResultSet
import java.sql.SQLException

internal class ResultSetMapper(private val kdb: Kdb) {

    fun columns(set: ResultSet): List<String> {
        return (1..set.metaData.columnCount).map {
            set.metaData.getColumnName(it)
        }
    }

    fun toElement(value: Any?): JsonElement = when (value) {
        null -> JsonNull
        is Boolean -> JsonPrimitive(value)
        is Number -> JsonPrimitive(value)
        is String -> JsonPrimitive(value)
        is Collection<*> -> JsonArray(value.map { toElement(it) })
        is JsonElement -> value
        else -> kdb.settings.json.encodeToJsonElement(serializer(value::class.javaObjectType) as KSerializer<Any>, value)
    }

    fun toJsonElement(set: ResultSet, serializer: KSerializer<*>): JsonElement {
        val metadata = set.metaData

        if (serializer == JsonObject.serializer()) {
            return JsonObject((1..metadata.columnCount).associate {
                metadata.getColumnName(it) to toElement(set.getObject(it))
            })
        }

        if (serializer.descriptor.kind != StructureKind.CLASS) {
            check(metadata.columnCount == 1) { "Expected only one column to convert to a ${serializer.descriptor.serialName}, but found ${metadata.columnCount}" }
            return toElement(set.getObject(1))
        }

        val mapped = mutableMapOf<String, JsonElement>()

        for (i in 0..<metadata.columnCount) {
            val value = set.getObject(i + 1)
            mapped[metadata.getColumnName(i + 1)] = toElement(value)
        }

        return JsonObject(mapped)
    }

    fun <T: Any> serializer(clazz: Class<T>): KSerializer<T>? {
        @Suppress("UNCHECKED_CAST") // - Checked
        return kdb.settings.json.serializersModule.serializerOrNull(clazz) as KSerializer<T>?
    }

    fun <T: Any> deserialize(element: JsonElement, serializer: KSerializer<T>, clazz: Class<T>): T? {
        return runCatching {
            kdb.settings.json.decodeFromJsonElement(serializer, element)
        }.getOrElse {
            error("Failed to deserialize ResultSet to '${clazz.name}': $it")
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun <T: Any> parse(set: ResultSet, clazz: Class<T>): T? {
        val node = node(clazz)

        return when {
            node != null -> toTable(set, kdb.table(clazz))
            clazz == JsonObject::class.java -> {
                @Suppress("UNCHECKED_CAST") // - Checked
                toJsonElement(set, serializer(clazz)!!) as T
            }
            else -> {
                val serializer = serializer(clazz)
                    ?: error("Failed to find serializer for '${clazz.name}'! Annotate it with @Serializable from Kotlinx")

                deserialize(toJsonElement(set, serializer), serializer, clazz)
            }
        }
    }

    fun <T: Any> toTable(set: ResultSet, table: ResolvedTable<T>): T {
        val values = table.node.columns.associate {
            val value = try {
                set.getObject(it.name)
            } catch (exception: SQLException) {
                error("Failed to get value for column '${it.name}', returned columns: ${columns(set).joinToString()} \n$exception")
            }

            it.reference.substringAfterLast(".") to deserialize(
                toElement(value),
                serializer(value::class.java) as KSerializer<Any>,
                value::class.java as Class<Any>
            )
        }

        return table.create(values)
    }

}