package me.santio.kdb.processor.annotations

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class Table(
    val name: String = "__INFERRED"
)