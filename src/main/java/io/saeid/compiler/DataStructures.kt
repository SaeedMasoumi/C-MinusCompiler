package io.saeid.compiler

import io.saeid.compiler.SymbolType.RESERVED

/**
 * @author Saeed Masoumi (s-masoumi@live.com)
 */

data class Symbol(val name: String, val type: SymbolType = RESERVED, val scope: Int = -1)

enum class SymbolType {
    DIGIT, ID, ANY, RESERVED
}

typealias RawRules = List<List<String>>

typealias Table = Map<Int, Map<String, String>>

data class Rule(val left: String, val right: List<String>)