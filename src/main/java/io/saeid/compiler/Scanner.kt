package io.saeid.compiler

import io.saeid.compiler.Logger.log
import io.saeid.compiler.SymbolType.ANY
import io.saeid.compiler.SymbolType.DIGIT
import io.saeid.compiler.SymbolType.ID
import io.saeid.compiler.SymbolType.RESERVED
import java.io.File
import java.util.function.Function

object ReservedTable {
    fun reservedSymbols(lex: File): MutableMap<String, Symbol> {
        val symbolTable = hashMapOf<String, Symbol>()
        lex.forEachLine {
            symbolTable[it] = Symbol(it)
        }
        return symbolTable
    }
}

class Lexer(lex: File, private var input: String) {

    private val symbolTable = ReservedTable.reservedSymbols(lex)
    private var tokens: List<Symbol> = emptyList()

    fun tokenize(): List<Symbol> {
        checkCommentSyntax(input)
        tokens = Tokenizer(symbolTable).apply(input)
        return tokens
    }

    private fun checkCommentSyntax(input: String) {
        if (input.hasCommentSyntaxError()) {
            throw CommentException("Comment has not finished")
        }
    }

}

internal class Tokenizer(
        private val symbolTable: MutableMap<String, Symbol>) : Function<String, List<Symbol>> {
    private var begin = 0
    private var end = 2
    private var cursor = 0
    private val tokens = mutableListOf<Symbol>()
    private var reachEOF = false
    private var detectedSymbolType = ANY

    override fun apply(input: String): List<Symbol> {
        begin = 0
        end = 1
        cursor = 0
        tokens.clear()
        detectedSymbolType = ANY
        while (cursor.notReachEOF(input)) {
            log("Tokenizer Loop: begin=$begin, end=$end, cursor=$cursor")
            if (reachEOF) {
                log("reach EOF, insert last token")
                insertToken(input)
                break
            } else {
                val token = getToken(input)
                log("Lookup for $token")
                val continueLookup = nextToken(input, token)
                if (!continueLookup) {
                    insertToken(input)
                }
            }
        }
        return tokens
    }

    private fun insertToken(input: String) {
        end-- // because we're on lookahead
        val token = getToken(input)
        val symbol = if (symbolTable.containsKey(token)) {
            symbolTable[token]!!
        } else {
            val symbol = Symbol(name = token, type = detectedSymbolType)
            symbolTable[token] = symbol
            symbol
        }
        log("Token has been found $symbol")
        tokens.add(symbol)
        if (!reachEOF) {
            moveCursor(input)
        }
        detectedSymbolType = ANY
    }

    private fun moveCursor(input: String) {
        begin = end
        ignoreSpace(input)
        end = begin + 1
        cursor = begin
    }

    private fun ignoreSpace(input: String) {
        while (input[begin] == ' ') {
            begin++
            log("ignore space at $begin ")
        }
    }

    private fun nextToken(input: String, token: String): Boolean {
        checkInvalidIdentifier(token)
        when {
            token.isDigit() -> {
                detectedSymbolType = DIGIT
                log("$token is digit")
                incrementEnd(input)
                return true
            }
            symbolTable.containsKey(token) -> {
                detectedSymbolType = RESERVED
                log("$token already exists in symbol table")
                incrementEnd(input)
                return true
            }
            token.isAlphabetDigitAlphabet() -> {
                detectedSymbolType = ID
                log("$token is alphabet")
                incrementEnd(input)
                return true
            }
            else -> {
                if (detectedSymbolType != ANY) {
                    log("stop lookahead")
                    return false
                }
                incrementEnd(input)
                return true
            }
        }
    }

    private fun checkInvalidIdentifier(token: String) {
        if(token.hasInvalidIdentifier()){
            throw InvalidIdentifierNameException("[$token] has invalid identifier name")
        }
    }

    private fun incrementEnd(input: String) {
        if (end + 1 > input.length) {
            println("reach eof")
            reachEOF = true
        }
        end++
    }

    private fun getToken(input: String) = input.substring(begin, end)

    private fun Int.notReachEOF(input: String) = this < input.length
}

private fun String.isDigit() = this.matches("\\d+".toRegex())
private fun String.isAlphabetDigitAlphabet() = this.matches(
        "[a-zA-Z]+[0-9]*[a-zA-Z]*".toRegex())

private fun String.hasCommentSyntaxError(): Boolean {
    val lhsComment = "/\\*".toRegex().containsMatchIn(this)
    val rhsComment = "\\*/".toRegex().containsMatchIn(this)
    return (lhsComment && !rhsComment) || (!lhsComment && rhsComment)
}

private fun String.hasInvalidIdentifier(): Boolean {
    return "^[0-9]+[a-zA-Z]+".toRegex().containsMatchIn(this)
}
