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
    private val reduces = arrayListOf<Reduce>()

    init {
        val newTokens = mutableListOf<Symbol>()
        newTokens.addAll(rawTokens)
        newTokens.add(Symbol("$", type = ANY))
        stack.apply {
            add("0")
        }
        tokens = newTokens
    }

    fun parse(): List<Reduce> {
        log("Start parsing........")
        while (cursor < tokens.size) {
            log("Stack: $stack")
            tokens[cursor].let {
                when (action(it)) {
                    ACCEPT -> return reduces
                    SHIFT -> shift(it)
                    REDUCE -> reduce(it)
                    ERROR->{
                        handleError(it)
                    }
                }
            }
        }
        return arrayListOf()
    }

    private fun handleError(token: Symbol) {
    }

    private fun action(token: Symbol): Action {
        val cell = take(token)
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

    private fun shift(token: Symbol) {
        val number = take(token).substring(1)
        stack.add(token.typeToTableName())
        stack.add(number)
        cursor++
    }

    private fun reduce(token: Symbol) {
        val grammarNumber = take(token).substring(1).toInt()
        val rule = rules[grammarNumber - 1]
        reduces.add(Reduce(rule, getFromRawToken(cursor), getFromRawToken(cursor - 1),
                getFromRawToken(cursor - 2)))
        //remove 2*size from stack
        if (!rule.right.contains("EPS")) {
            var rhsSize = rule.right.size * 2
            while (rhsSize > 0) {
                if (stack.size > 0) {
                    stack.removeAt(stack.size - 1)
                }
                rhsSize--
            }
        }
        stack.add(rule.left)
        //goto
        val top = stack.last() //top
        val index = stack[stack.size - 2].toDouble().toInt() // top-1
        stack.add(table.take(index, top))
    }

    private fun getFromRawToken(cursor: Int): Symbol {
        var index = cursor
        if (index < 0)
            index += tokens.size
        val token = tokens[index]
        if(token.name == "$"){
            return tokens[index-1]
        }
        return token
    }

    private fun Table.take(index: Int, token: String): String {
        return get(index)!![token]!!
    }

    private fun String.isDigit() = this.matches("\\d+".toRegex())

}