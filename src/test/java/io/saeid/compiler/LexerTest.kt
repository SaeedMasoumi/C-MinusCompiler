package io.saeid.compiler

import io.saeid.compiler.SymbolType.DIGIT
import io.saeid.compiler.SymbolType.ID
import io.saeid.compiler.SymbolType.RESERVED
import junit.framework.TestCase.assertEquals
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
    fun `validate tokenizer`() {
    scan("33;").assert("33",";")
        scan("int a =2;")
                .assert("int", "a", "=", "2", ";")
                .assert(RESERVED, ID, RESERVED, DIGIT, RESERVED)
        scan("a=2;")
                .assert("a", "=", "2", ";")
                .assert(ID, RESERVED, DIGIT, RESERVED)
        scan("int a = a2 + 2")
                .assert("int", "a", "=", "a2", "+", "2")
                .assert(RESERVED, ID, RESERVED, ID, RESERVED, DIGIT)
        scan("""
               while( a==3 && a<b){
          int c;
          c=45;
          output(c-b);
      }""")
                .assert("while","(","a","==","3","&&","a","<","b",")","{","int","c",";","c","=","45",";"
                ,"output","(","c","-","b",")",";","}")

    }


    private fun List<Symbol>.assert(vararg tokens: String) = apply {
        this.forEach { println(it.name) }
        assertEquals(size, tokens.size)
        forEachIndexed { index, symbol ->
            assertEquals(symbol.name, tokens[index])
        }
    }

    private fun List<Symbol>.assert(vararg symbolType: SymbolType) = apply {
        assertEquals(size, symbolType.size)
        forEachIndexed { index, symbol ->
            println(symbol)
            assertEquals(symbol.type, symbolType[index])
        }
    }
}