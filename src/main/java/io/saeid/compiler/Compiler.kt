package io.saeid.compiler

import java.io.File

fun main(args: Array<String>) {
    val language = "data/g1"
    val program = File("prog3.txt").toProgramString()
    val lexer = Lexer(language.lex(), normalize(program))
    val tokens = lexer.tokenize()
    val rules = SLRTable.rules(language.grammar())
    val table = SLRTable.slr(language.table())
    val parser = Parser(tokens, table, rules)
    val reduces = parser.parse()
    reduces.forEach {
    it.apply { println("${old.name} ${prev.name} ${cur.name}") }
    }
}

typealias Language = String

fun Language.lex() = File("$this.lex")
fun Language.grammar() = File("$this.grammar")

fun Language.table() = File("$this.table")

fun File.toProgramString(): String {
    return readLines().joinToString(separator = "\n")
}