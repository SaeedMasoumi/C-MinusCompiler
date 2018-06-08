package io.saeid.compiler

import java.io.File

fun main(args: Array<String>) {
//    val program = File("prog.txt").toProgramString()
//    val lexer = Lexer(normalize(program))
//    val tokens = lexer.tokenize()
    SLRTable.get()
}

fun File.toProgramString(): String {
    return readLines().joinToString(separator = "\n")
}