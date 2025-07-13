package me.santio.kdb.processor.type

enum class OptionalBool {

    TRUE,
    FALSE,
    UNSPECIFIED,
    ;

    fun toBoolean(): Boolean {
        return when (this) {
            TRUE -> true
            FALSE -> false
            UNSPECIFIED -> error("Cannot convert $this to Boolean")
        }
    }

    fun orElse(default: Boolean): Boolean {
        return this.takeIf { it != UNSPECIFIED }?.toBoolean() ?: default
    }

}