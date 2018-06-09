package io.saeid.compiler

import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.ss.usermodel.WorkbookFactory
import java.io.FileInputStream
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths

/**
 * @author Saeed Masoumi (s-masoumi@live.com)
 */

object Excel {
    fun open(fileName: String): Workbook {
        return WorkbookFactory.create(FileInputStream(Paths.get(fileName).toFile()))
    }

    fun write(workbook: Workbook, fileName: String) {
        val outputPath = Paths.get(fileName)
        try {
            Files.newOutputStream(outputPath).use {
                workbook.write(it)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}

