package io.saeid.compiler

import org.apache.poi.ss.usermodel.CellType.NUMERIC
import org.apache.poi.ss.usermodel.CellType.STRING
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

/**
 * @author Saeed Masoumi (s-masoumi@live.com)
 */

fun main(args: Array<String>) {
//    makeSLRTable("grammar-n1-slr.xlsx")
    changeGrammar("data/n1.lex", "data/n1.grammar")
}

typealias Old = String
typealias New = String

fun changeGrammar(lex: String, grammar: String) {
    val mapOutput = StringBuilder()
    val rawRules = arrayListOf<MutableList<String>>()
    File(grammar).forEachLine {
        rawRules.add(it.trim().split("\\s+".toRegex()).toMutableList())
    }
    // TERMINAL
    val terminalMap = hashMapOf<Old, New>()
    val oldTerminals = arrayListOf<String>()
    val newTerminals = arrayOf("a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n",
            "o",
            "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z"
    )

    File(lex).forEachLine {
        if (!it.isEmpty())
            oldTerminals.add(it.trim())
    }
    oldTerminals.add("NUM")
    oldTerminals.add("ID")

    oldTerminals.forEachIndexed { index, s ->
        terminalMap[s] = newTerminals[index]
    }

    // NON TERMINAL
    val oldNoneTerminal = mutableSetOf<String>()
    val newNoneTerminal = listOf("A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M",
            "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"
            , "Á", "Ḃ", "Ć", "Ď", "É", "Ḟ", "Ǵ", "Ĥ", "Í", "Ĵ", "Ḱ", "Ĺ", "Ḿ", "Ń", "Ó", "Ṕ", "Ɋ",
            "Ŕ", "Ź", "Ý", "Ẍ", "Ẃ", "Ṽ", "Ú", "Ť", "Ā", "Ḇ", "Ç", "Ḏ", "Ḙ", "Ģ",
            "Ḥ", "Ḵ", "Ḷ", "Ṃ", "Ņ", "Ọ", "Ṟ", "Ş", "Ṯ", "Ṷ", "Ṿ", "Ẉ", "Ẕ", "Ẽ", "Ñ", "Õ",
            "Ä", "Ë", "Ḧ", "Ï", "Ö", "Ȑ", "Ẅ", "Ű", "Ÿ", "Ệ")
    val noneTerminalMap = hashMapOf<Old, New>()
    rawRules.forEach { row ->
        row.forEach {
            if (!it.contains("->") && !it.contains("|") && !oldTerminals.contains(it)) {
                oldNoneTerminal.add(it)
            }
        }
    }

    oldNoneTerminal.forEachIndexed { index, s ->
        noneTerminalMap[s] = newNoneTerminal[index]
    }

    // switch
    rawRules.forEachIndexed { rowIndex, list ->
        list.forEachIndexed { colIndex, s ->
            val col = rawRules[rowIndex][colIndex]
            if (oldTerminals.contains(col)) {
                rawRules[rowIndex][colIndex] = terminalMap[col]!!
            } else if (oldNoneTerminal.contains(col)) {
                rawRules[rowIndex][colIndex] = noneTerminalMap[col]!!
            }
        }
    }
    mapOutput.append("TERMINALS\n----------\n")
    terminalMap.forEach { t, u ->
        mapOutput.append("${t.padEnd(8)}->${u.padStart(4)}\n")
    }
    mapOutput.append("NON-TERMINALS\n----------\n")

    noneTerminalMap.forEach { t, u ->
        mapOutput.append("${t.padEnd(24)}->${u.padStart(8)}\n")
    }
    mapOutput.append("Grammar\n----------\n")
    rawRules.forEach {
        it.forEach {
            mapOutput.append(it)
        }
        mapOutput.append("\n")
    }

    Files.write(Paths.get("terminal-nonterminals-map.txt"), mapOutput.toString().toByteArray())

    println("----------")
//    Collections.sort(newNoneTerminal, Collections.reverseOrder())
//    File("ghasem.txt").forEachLine {
//        print("$it\t")
//    }
    val lines = mutableListOf<MutableList<String>>()
    Excel.open("grammar-n1-slr.xlsx").use { workbook ->
        val sheet = workbook.getSheetAt(0)
        var length = 0
        sheet.forEachIndexed { rowIndex, row ->
            lines.add(mutableListOf())
            if (rowIndex > 0) {
                for (i in 0 until length) {
                    lines.last().add("E")
                }
            }
            row.cellIterator().forEach {
                if (rowIndex == 0) {
                    length++
                }
                if (it.columnIndex > 0) {
                    val cellValue = when (it.cellTypeEnum) {
                        STRING -> it.stringCellValue
                        NUMERIC -> it.numericCellValue.toInt().toString()
                        else -> "E"
                    }
                    if (rowIndex > 0)
                        lines[rowIndex][it.columnIndex - 1] = cellValue
                    else lines[rowIndex].add(cellValue)
                }
            }
        }
    }

    lines.first().forEachIndexed { index, s ->
        if (newTerminals.contains(s)) {
            terminalMap.forEach { old, new ->
                if (new == s) {
                    lines[0][index] = old
                }
            }
        }
        else if (newNoneTerminal.contains(s)) {
            noneTerminalMap.forEach { old, new ->
                if (new == s) {
                    lines[0][index] = old
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

