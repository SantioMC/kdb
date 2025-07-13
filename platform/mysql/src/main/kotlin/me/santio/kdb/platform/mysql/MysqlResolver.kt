package me.santio.kdb.platform.mysql

import me.santio.kdb.platform.Resolver

object MysqlResolver: Resolver {

    override fun getSqlType(clazz: Class<*>): String? {
        return when (clazz) {
            String::class.javaObjectType -> "varchar(255)"
            Int::class.javaObjectType -> "int(11)"
            Long::class.javaObjectType -> "bigint(20)"
            Double::class.javaObjectType -> "double"
            Float::class.javaObjectType -> "float"
            Short::class.javaObjectType -> "smallint(6)"
            Byte::class.javaObjectType -> "tinyint(4)"
            else -> null
        }
    }

}
