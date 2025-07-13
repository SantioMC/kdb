package me.santio.kdb.test

import me.santio.kdb.Kdb
import me.santio.kdb.event.KdbEvent
import me.santio.kdb.settings.KdbSettings
import java.nio.file.Files
import kotlin.io.path.absolutePathString
import kotlin.io.path.deleteIfExists

fun Kdb.Companion.test(settings: KdbSettings.() -> Unit = {}): Kdb {
    val tempFile = Files.createTempFile(
        "kdb-test-db",
        ".db"
    )

    return Kdb("sqlite:${tempFile.absolutePathString()}", settings).apply {
        this.events.subscribe<KdbEvent.OnConnectionClosed> {
            tempFile.deleteIfExists()
        }
    }
}