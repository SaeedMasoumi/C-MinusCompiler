package io.saeid.compiler

import java.io.File

/**
 * @author Saeed Masoumi (s-masoumi@live.com)
 */
object SLRTable {

    private lateinit var rule: List<Rule>
    private lateinit var table: Table

    fun rules(path: File): List<Rule> {
        if (!::rule.isInitialized)
            rule = extractRules(getRawRules(path))
        return rule
    }

    private fun getRawRules(path: File): RawRules {
        val rawRules = arrayListOf<List<String>>()
        path.forEachLine {
            rawRules.add(it.trim().split("\\s+".toRegex()))
        }
        return rawRules
    }

    private fun extractRules(rawRules: RawRules): List<Rule> {
        val rules = arrayListOf<Rule>()
        rawRules.forEach {
            val left = it[0]
            val right = mutableListOf<String>()
            for (i in 2 until it.size) {
                val token = it[i]
                if (token == "|") {
                    rules.add(Rule(left, right))
                    right.clear()
                } else /*if(token != "EPS")*/ {
                    right.add(token)
                }
            }
            rules.add(Rule(left, right))
        }
        return rules
    }

    /**
     * RowIndex -> [Terminal or NonTerminal] -> Value
     */
    fun slr(path: File): Table {
        if (!::table.isInitialized) {
            table = makeSLRTable(path)
        }
        return table
    }

    private fun makeSLRTable(path: File): Map<Int, Map<String, String>> {
        val table = mutableMapOf<Int, MutableMap<String, String>>()
        var line = 0
        val tokens = arrayListOf<String>()
        path.forEachLine {
            if (line == 0) {
                it.split("\\s+".toRegex()).forEach {
                    tokens.add(it)
                }
            } else {
                val curLine = line - 1
                if (!table.containsKey(curLine)) table[curLine] = mutableMapOf()
                it.split("\\s+".toRegex()).forEachIndexed { index, s ->
                    table[curLine]!![tokens[index]] = if (s == "E") "" else s
                }
            }
            line++
        }
        return table
    }

}


