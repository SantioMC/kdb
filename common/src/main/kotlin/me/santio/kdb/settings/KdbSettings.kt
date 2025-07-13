package me.santio.kdb.settings

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.json.Json
import org.objenesis.Objenesis
import org.objenesis.ObjenesisStd

@Suppress("unused")
class KdbSettings {
    var username: String? = null
    var password: String? = null
    var dispatcher: CoroutineDispatcher = Dispatchers.IO
    var preloadCache: Boolean = false

    var objenesis: Objenesis = ObjenesisStd(true)
    var json: Json = Json {
        isLenient = true
        ignoreUnknownKeys = true
    }

    var cache: CacheSettings = CacheSettings()
        private set

    var migration: MigrationSettings = MigrationSettings()
        private set

    fun cache(settings: CacheSettings.() -> Unit) {
        this.cache.apply(settings)
    }

    fun migration(settings: MigrationSettings.() -> Unit) {
        this.migration.apply(settings)
    }
}