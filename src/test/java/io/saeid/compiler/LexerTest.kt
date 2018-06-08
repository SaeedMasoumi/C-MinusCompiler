package io.saeid.compiler

import org.junit.Test

/**
 * @author Saeed Masoumi (s-masoumi@live.com)
 */

class LexerTest {

    fun scan(input: String): List<Symbol> {
        return Lexer(normalize(input)).tokenize()
    }

    @Test(expected = CommentException::class)
    fun `detect comment syntax error on lhs`() {
        scan("""
            int main() {
             /* this is a comment
             int a = 2;
             }
            """)
    }

    @Test(expected = CommentException::class)
    fun `detect comment syntax error on rhs`() {
        scan("""
            int main() {
              this is a comment */
             int a = 2;
             }
            """)
    }

    @Test
    fun `don't detect comment syntax error`() {
        scan("""
            int main() {
              /* this is a comment */
             int a = 2;
             }
            """)
    }

    @Test(expected = InvalidIdentifierNameException::class)
    fun `detect invalid identifier`() {
        scan("""
        int main(){
        int 2a = 2;
        }
        """)
    }

    @Test
    fun `don't detect invalid identifier`() {
        scan("""
        int main(){
        int a2 = a2;
        }
        """)
    }

    @Test
    fun `foo bar`() {
        Lexer("int a=2;").tokenize().forEach {
            println(it)
        }
        Lexer("a=2;").tokenize().forEach {
            println(it)
        }
        Lexer("int foo(){ int a = 2; }").tokenize().forEach {
            println(it)
        }
        Lexer("int a = a2 + 2").tokenize().forEach {
            println(it)
        }
    }
}