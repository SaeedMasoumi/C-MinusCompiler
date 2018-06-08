package io.saeid.compiler

/**
 * @author Saeed Masoumi (s-masoumi@live.com)
 */
class CommentException(message: String) : RuntimeException(message)

class InvalidCharacterException(message: String) : RuntimeException(message)

class InvalidIdentifierNameException(message: String) : RuntimeException(message)

