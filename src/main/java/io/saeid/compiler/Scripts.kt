package io.saeid.compiler

import org.apache.poi.ss.usermodel.CellType.NUMERIC
import org.apache.poi.ss.usermodel.CellType.STRING
import java.nio.file.Files
import java.nio.file.Paths

/**
 * @author Saeed Masoumi (s-masoumi@live.com)
 */

fun main(args: Array<String>) {
    makeSLRTable("generated-excel.xlsx")
}

private fun makeSLRTable(path: String) {
    //terminal-nonterminal -> list
    val lines = mutableListOf<MutableList<String>>()
    Excel.open(path).use { workbook ->
        val sheet = workbook.getSheetAt(0)
        sheet.forEachIndexed { rowIndex, row ->
            lines.add(mutableListOf())
            row.cellIterator().forEach {
                if (it.columnIndex > 0) {
                    val cellValue = when (it.cellTypeEnum) {
                        STRING -> it.stringCellValue
                        NUMERIC -> it.numericCellValue.toInt().toString()
                        else -> "E"
                    }
                    lines[rowIndex].add(cellValue)
                }
            }
        }
    }


    val file = Paths.get("new-table.table")
    val sb = StringBuilder()
    lines.forEach {
        it.forEach {
            sb.append(it).append(" ")
        }
        sb.append("\n")
    }
    Files.write(file, sb.toString().toByteArray())
}

