package io.saeid.compiler

import org.junit.Test
import java.io.FileOutputStream
import java.io.ObjectOutputStream

/**
 * @author Saeed Masoumi (s-masoumi@live.com)
 */

class SLRTest {
    var language: Language = "../../data/g1"
    var language2: Language = "../../data/f1"

    @Test
    fun rules() {
        SLRTable.rules(language2.grammar()).forEach {
            println(it)
        }
    }

    @Test
    fun foo() {
        val file = "belchers.burgers"

        //A map of family
        val family = mapOf(
                "Bob" to "Father",
                "Linda" to "Mother",
                "Tina" to "Oldest",
                "Gene" to "Middle",
                "Louise" to "Youngest")

        //Write the family map object to a file
        ObjectOutputStream(FileOutputStream(file)).use { it -> it.writeObject(family) }

        println("Wrote $file")
        println()
    }
}