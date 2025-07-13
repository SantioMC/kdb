package me.santio.kdb.base

import me.santio.kdb.connection.KdbConnection
import me.santio.kdb.connection.transaction.Transaction

/**
 * Extending this class with the Kdb processor will automatically register it.
 *
 * This class will allow you to register migrations where you can manually interact with the
 * database before Kdb is used for anything. For modifying how Kdb runs migrations, see
 * MigrationSettings.
 *
 * An example of how you would use this class is provided below.
 * ```kt
 * object MyMigration: Migration("create users table", 1) {
 *      override fun Transaction.up(db: KdbConnection) {
 *          db.execute("CREATE TABLE users ...")
 *      }
 * }
 * ```
 *
 * This class takes in a value, which is used for resolving which order the migrations need to be
 * ran in. While you could use an incrementing index to define them, we recommend using a unix timestamp
 * to make collaboration between developers easier. Just make sure you remember which format you're using
 * (seconds vs milliseconds) if you use unix timestamps.
 *
 * @author santio
 */
abstract class Migration(
    val name: String,
    val value: Long
) {

    val id: String
        get() = buildString {
            val identifier = name
                .lowercase()
                .trim()
                .replace("-", "_")
                .replace(" ", "_")

            append(value)
            append("_")

            append(identifier)
        }

    abstract fun Transaction.up(db: KdbConnection)

}