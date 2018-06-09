package io.saeid.compiler

import java.io.File

fun main(args: Array<String>) {
    val program = File("prog.txt").toProgramString()
    val lexer = Lexer(normalize(program))
    val tokens = lexer.tokenize()
    val rules = SLRTable.rules()
    val table = SLRTable.slr("generated-excel.xlsx")
    val parser = Parser(tokens, table, rules)
    parser.parse()

}

fun File.toProgramString(): String {
    return readLines().joinToString(separator = "\n")
}