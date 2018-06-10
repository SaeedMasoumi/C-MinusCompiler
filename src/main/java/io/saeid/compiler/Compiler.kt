package io.saeid.compiler

import java.io.File

fun main(args: Array<String>) {
    val language = "data/n1"
    val program = File("prog4.txt").toProgramString()
    val lexer = Lexer(language.lex(), normalize(program))
    val tokens = lexer.tokenize()
    val rules = SLRTable.rules(language.grammar())
    val table = SLRTable.slr(language.table())
    val follow = SLRTable.follow(language.follow())
    val parser = Parser(tokens, table, rules, follow)
    val reduces = parser.parse()
    val codeGenerator = CodeGenerator(reduces)
    codeGenerator.generator()
}

typealias Language = String

fun Language.lex() = File("$this.lex")
fun Language.grammar() = File("$this.grammar")
fun Language.table() = File("$this.table")
fun Language.follow() = File("$this.follow")

fun File.toProgramString(): String {
    return readLines().joinToString(separator = "\n")
}