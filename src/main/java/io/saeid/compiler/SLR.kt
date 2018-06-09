package io.saeid.compiler

import org.apache.poi.ss.usermodel.CellType.NUMERIC
import org.apache.poi.ss.usermodel.CellType.STRING
import java.io.File

/**
 * @author Saeed Masoumi (s-masoumi@live.com)
 */
object SLRTable {

    private lateinit var rule: List<Rule>
    private lateinit var table: Table

    fun rules(): List<Rule> {
        if (!::rule.isInitialized)
            rule = extractRules(getRawRules())
        return rule
    }

    private fun getRawRules(): RawRules {
        val rawRules = arrayListOf<List<String>>()
        File("grammar.txt").forEachLine {
            rawRules.add(it.trim().split(" "))
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

    /**
     * RowIndex -> [Terminal or NonTerminal] -> Value
     */
    fun slr(): Table {
        if (!::table.isInitialized) {
            table = makeSLRTable()
        }
        return table
    }

    private fun makeSLRTable(): Map<Int, Map<String, String>> {
        val table = mutableMapOf<Int, Map<String, String>>()
        Excel.open("slr.xlsx").use { workbook ->
            val sheet = workbook.getSheetAt(0)
            val firstRow = sheet.getRow(0)

            sheet.forEachIndexed { rowIndex, row ->
                val cells = hashMapOf<String, String>()
                firstRow.forEach {
                    if (it.columnIndex > 0) {
                        cells[it.stringCellValue] = ""
                    }
                }
                if (rowIndex > 0) {
                    row.forEach { cell ->
                        if (cell.columnIndex > 0) {
                            val key = firstRow.getCell(cell.columnIndex).stringCellValue
                            when (cell.cellTypeEnum) {
                                STRING -> cells[key] = cell.stringCellValue
                                NUMERIC -> cells[key] = cell.numericCellValue.toString()
                                else -> {
                                    cells[key] = ""
                                }
                            }
                        }
                    }
                    table[rowIndex - 1] = (cells)
                }
            }
        }
        return table
    }

}

private fun List<String>.containsEPS(): Boolean {
    return this.size <= 2
}


