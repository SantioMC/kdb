package me.santio.kdb.processor.visitor

import com.google.devtools.ksp.symbol.KSNode

interface KSPConverter<K: KSNode, T> {

    fun parse(node: K): T

}