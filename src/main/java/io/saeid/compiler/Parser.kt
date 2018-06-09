package io.saeid.compiler

import io.saeid.compiler.SymbolType.ANY

/**
 * @author Saeed Masoumi (s-masoumi@live.com)
 */
class Parser(rawTokens: List<Symbol>,
        private val table: Table, private val rules: List<Rule>) {

    private val stack = mutableListOf<String>()
    private val tokens: List<Symbol>

    init {
        val newTokens = mutableListOf<Symbol>()
        newTokens.addAll(rawTokens)
        newTokens.add(Symbol("$", type = ANY))
        stack.apply {
            add("0")
        }
        tokens = newTokens
    }

    fun parse() {
        tokens.forEach {
            val token = it.name
            when {
                accept(token) -> {
                    Logger.log("accept")
                }
                shift(token) -> {
                    val number = take(token).substring(1)
                    stack.add(token)
                    stack.add(number)
                }
                reduce(token) -> {
                    val grammarNumber = take(token).substring(1).toInt()
                    val grammarLine = rules[grammarNumber - 1]
                    var rhsSize = grammarLine.right.size * 2
                    while (rhsSize > 0) {
                        stack.removeAt(stack.size - 1)
                        rhsSize--
                    }
                    stack.add(grammarLine.left)
                    //goto
                    val top = stack.last() //top
                    val index = stack[stack.size - 2].toInt() // top-1
                    stack.add(table.take(index, top))
                }
                else -> {
                    throw ParserException("")
                }
            }
        }
    }


    private fun take(token: String): String {
        val last = stack.last()
        val index = last.toInt()
        return table.take(index, token)
    }

    private fun accept(token: String) = take(token) == "acc"

    private fun shift(token: String) = take(token).startsWith("s")

    private fun reduce(token: String) = take(token).startsWith("r")

    private fun Table.take(index: Int, token: String): String {
        return get(index)!![token]!!
    }

    private fun String.isDigit() = this.matches("\\d+".toRegex())

}