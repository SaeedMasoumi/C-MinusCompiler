package io.saeid.compiler

import java.io.File

/**
 * @author Saeed Masoumi (s-masoumi@live.com)
 */
object SLRTable {

    fun get() {
        build()
    }

    private fun build() {
        val rawRules = getRawRules()
        val rules = extractRules(rawRules)
    }

    private fun getRawRules(): RawRules {
        val rawRules = arrayListOf<List<String>>()
        File("grammar.txt").forEachLine {
            rawRules.add(it.split(" "))
        }
        return rawRules
    }

    private fun extractRules(rawRules: RawRules): List<Rule> {
        val rules = arrayListOf<Rule>()
        rawRules.forEach {
            val left = it[0]
            val right: List<String> = if (it.containsEPS()) {
                emptyList()
            } else {
                it.subList(2, it.size)
            }
            rules.add(Rule(left, right))
        }
        return rules
    }

}

private fun List<String>.containsEPS(): Boolean {
    return this.size <= 2
}


