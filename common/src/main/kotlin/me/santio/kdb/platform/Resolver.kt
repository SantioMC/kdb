package me.santio.kdb.platform

/**
 * Resolves java classes to their SQL counterpart
 * @author santio
 */
interface Resolver {

    /**
     * Attempts to resolve the provided class to the best-fitting SQL type as a string. If the
     * type can not be resolved successfully, then null shall be returned so that an error may be
     * raised.
     */
    fun getSqlType(clazz: Class<*>): String?

}