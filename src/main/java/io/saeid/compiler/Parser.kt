package io.saeid.compiler

import io.saeid.compiler.Logger.log
import io.saeid.compiler.Parser.Action.ACCEPT
import io.saeid.compiler.Parser.Action.ERROR
import io.saeid.compiler.Parser.Action.REDUCE
import io.saeid.compiler.Parser.Action.SHIFT
import io.saeid.compiler.SymbolType.ANY

/**
 * @author Saeed Masoumi (s-masoumi@live.com)
 */
class Parser(rawTokens: List<Symbol>,
        private val table: Table, private val rules: List<Rule>) {

    private val stack = mutableListOf<String>()
    private val tokens: List<Symbol>
    private var cursor = 0

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
        log("Start parsing........")
        while (cursor < tokens.size) {
            tokens[cursor].let {
                when (action(it)) {
                    ACCEPT -> accept(it)
                    SHIFT -> shift(it)
                    REDUCE -> reduce(it)
                    else -> {
                        throw ParserException("")
                    }
                }
            }
        }
    }

    private fun action(token: Symbol): Action {
        log("lookup action for ${token.name}")
        val cell = take(token)
        log("cell found: $cell")
        when {
            cell == "acc" -> return ACCEPT
            cell.startsWith("s") -> return SHIFT
            cell.startsWith("r") -> return REDUCE
        }
        return ERROR
    }

    private enum class Action {
        SHIFT, REDUCE, ACCEPT, ERROR
    }

    private fun take(token: Symbol): String {
        val last = stack.last()
        val index = last.toDouble().toInt()
        val column = token.typeToTableName()
        return table.take(index, column)
    }

    private fun accept(token: Symbol) {
        throw Exception("Accepted")
    }


    private fun shift(token: Symbol) {
        val number = take(token).substring(1)
        stack.add(token.typeToTableName())
        stack.add(number)
        log(">>>>>> Shift stack -> add ${token.typeToTableName()}, $number")
        log("------ stack is $stack")
        cursor++
    }

    private fun reduce(token: Symbol) {
        val grammarNumber = take(token).substring(1).toInt()
        val grammarLine = rules[grammarNumber - 1]
        if (!grammarLine.right.contains("EPS")) {
            var rhsSize = grammarLine.right.size * 2
            while (rhsSize > 0) {
                if (stack.size > 0) {
                    stack.removeAt(stack.size - 1)
                }
                rhsSize--
            }
        }
        stack.add(grammarLine.left)
        //goto
        val top = stack.last() //top
        val index = stack[stack.size - 2].toDouble().toInt() // top-1
        log("reduce goto $index $top")
        println(table.take(index,top))
        stack.add(table.take(index, top))
        log(">>>>>> Reduce")
        log("------ stack is $stack")
    }

    private fun Table.take(index: Int, token: String): String {
        return get(index)!![token]!!
    }

    private fun String.isDigit() = this.matches("\\d+".toRegex())

}