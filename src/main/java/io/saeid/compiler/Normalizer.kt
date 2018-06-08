package io.saeid.compiler

/**
 * Removes whitespace and comments from given input.
 */
fun normalize(input: String): String {
    return input
            .trim()
            .replace("\\n+".toRegex(), " ")
            .replace("\\s+".toRegex(), " ")
            .replace("\\/\\*(.*?)\\*\\/".toRegex(), "")
//            .replace("/\\*((.|\\s)*?)\\*/".toRegex(), "") for multiline comment
}
