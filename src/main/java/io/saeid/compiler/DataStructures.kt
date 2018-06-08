package io.saeid.compiler

import io.saeid.compiler.SymbolType.ANY

/**
 * @author Saeed Masoumi (s-masoumi@live.com)
 */

data class Symbol(val name: String, val type: SymbolType = ANY, val scope: Int = -1)

enum class SymbolType {
    DIGIT, ID, ANY
}

typealias RawRules = List<List<String>>

data class Rule(val left: String, val right: List<String>)