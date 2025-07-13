package me.santio.kdb.platform.sqlite

import me.santio.kdb.platform.Resolver

object SqliteResolver: Resolver {

    override fun getSqlType(clazz: Class<*>): String? {
        return when (clazz) {
            String::class.javaObjectType -> "TEXT"
            Int::class.javaObjectType -> "INTEGER"
            Long::class.javaObjectType -> "INTEGER"
            Double::class.javaObjectType -> "REAL"
            Float::class.javaObjectType -> "REAL"
            Short::class.javaObjectType -> "INTEGER"
            Byte::class.javaObjectType -> "INTEGER"
            else -> null
        }
    }

}
