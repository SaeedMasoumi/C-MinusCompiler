package io.saeid.compiler

import java.io.File

fun main(args: Array<String>) {
    val program = File("prog.txt").toProgramString()
    val lexer = Lexer(normalize(program))
    lexer.tokenize()
}

fun File.toProgramString(): String {
    return readLines().joinToString(separator = "\n")
}