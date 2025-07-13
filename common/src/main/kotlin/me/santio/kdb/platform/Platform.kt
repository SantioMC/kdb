package me.santio.kdb.platform

import me.santio.kdb.Kdb
import me.santio.kdb.connection.KdbConnection
import org.slf4j.LoggerFactory
import java.util.*

abstract class Platform {

    abstract val scheme: String
    abstract val className: String
    abstract val resolver: Resolver
    abstract val generator: SqlGenerator

    abstract fun execution(kdb: Kdb, connection: KdbConnection? = null): Execution

    companion object {
        private val logger = LoggerFactory.getLogger(Platform::class.java)
        private val platforms: MutableMap<String, Platform> = mutableMapOf()

        init {
            ServiceLoader.load(Platform::class.java).forEach { platform ->
                this.register(platform.scheme, platform)
            }
        }

        fun register(scheme: String, platform: Platform) {
            if (platforms.containsKey(scheme)) {
                logger.warn("A platform already exists for scheme $scheme, it will be replaced.")
            }

            platforms[scheme] = platform
        }

        fun forScheme(scheme: String): Platform {
            return platforms[scheme] ?: error("No platform exists for scheme $scheme, install it with kdbPlatform(\"$scheme\") in your dependencies")
        }

        fun forUri(uri: String): Platform {
            val scheme = uri.trim().substringBefore(":")
            return forScheme(scheme)
        }
    }

}