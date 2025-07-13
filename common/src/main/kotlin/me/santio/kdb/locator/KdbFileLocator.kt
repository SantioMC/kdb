package me.santio.kdb.locator

import me.santio.kdb.base.Migration
import me.santio.kdb.locator.loader.KotlinServiceLoader
import java.io.InputStream

internal object KdbFileLocator {

    fun inputStream(file: String): InputStream {
        return this::class.java.classLoader.getResourceAsStream("META-INF/kdb/$file")
            ?: error("Failed to find the generated tables in the JAR file")
    }

    suspend fun migrations(classLoader: ClassLoader = this::class.java.classLoader): List<Migration> {
        return KotlinServiceLoader(classLoader).load<Migration>(
            file = "META-INF/kdb/migrations.kdb"
        )
    }

}