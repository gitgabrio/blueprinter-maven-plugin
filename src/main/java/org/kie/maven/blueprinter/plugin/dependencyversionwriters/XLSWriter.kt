package org.kie.maven.blueprinter.plugin.dependencyversionwriters

import org.apache.commons.io.FilenameUtils
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.kie.maven.blueprinter.plugin.AbstractBluePrinterMojo.LOG_LEVEL
import org.kie.maven.blueprinter.plugin.dataclass.CommonLoggingHolder
import org.kie.maven.blueprinter.plugin.utils.logMessage
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import java.util.function.Consumer

class XLSWriter {

    fun getSheet(toPopulate: Workbook, tweakerProfile: String, commonLoggingHolder: CommonLoggingHolder): Sheet {
        logMessage("getSheet from %s for profile $toPopulate $tweakerProfile", LOG_LEVEL.DEBUG, commonLoggingHolder)
        if (toPopulate.getSheet(tweakerProfile) != null) {
            logMessage("Removing $tweakerProfile", LOG_LEVEL.DEBUG, commonLoggingHolder)
            val sheetIndexToRemove: Int = toPopulate.getSheetIndex(tweakerProfile)
            toPopulate.removeSheetAt(sheetIndexToRemove)
        }
        return toPopulate.createSheet(tweakerProfile)
    }


    fun populateSheetWithUserTaskInsight(
        toPopulate: Sheet,
        user: String,
        task: String,
        answer: String,
        rowCounter: Int,
        commonLoggingHolder: CommonLoggingHolder
    ) {
        logMessage(
            "populateSheetWithUserTaskInsight $toPopulate with $user $task $answer",
            LOG_LEVEL.DEBUG,
            commonLoggingHolder
        )
        createUserTaskHeaders(toPopulate, commonLoggingHolder)
        createUserTaskDataRow(toPopulate, user, task, answer, rowCounter, commonLoggingHolder)
    }

    fun populateSheetWithProcessInsight(
        toPopulate: Sheet,
        process: String,
        answer: String,
        rowCounter: Int,
        commonLoggingHolder: CommonLoggingHolder
    ) {
        logMessage(
            "populateSheetWithProcessInsight $toPopulate with $process $answer", LOG_LEVEL.DEBUG, commonLoggingHolder
        )
        createProcessesHeaders(toPopulate, commonLoggingHolder)
        createProcessDataRow(toPopulate, process, answer, rowCounter, commonLoggingHolder)
    }

    fun createUserTaskDataRow(
        toPopulate: Sheet,
        user: String,
        task: String,
        answer: String,
        rowCounter: Int,
        commonLoggingHolder: CommonLoggingHolder
    ) {
        logMessage(
            "populateSheet $toPopulate with $user $task $answer", LOG_LEVEL.DEBUG, commonLoggingHolder
        )
        val data = toPopulate.createRow(rowCounter)
        /*      data.createCell(0).setCellValue(taskExecutor.getAIProvider())
              data.createCell(1).setCellValue(taskExecutor.getChatModel())
              data.createCell(2).setCellValue(taskExecutor.getEmbeddingModel())
              data.createCell(3).setCellValue(taskExecutor.getCommonPromptTemplate())
              data.createCell(4).setCellValue(taskExecutor.getGenericSystemPromptTemplate())
              data.createCell(5).setCellValue(taskExecutor.getUserTaskSpecificSystemPromptTemplate())
              data.createCell(6).setCellValue(taskExecutor.getDataDescription())
              data.createCell(7).setCellValue(taskExecutor.getUserTaskSpecificQuestionTemplate())
              data.createCell(8).setCellValue(user)
              data.createCell(9).setCellValue(task)
              data.createCell(10).setCellValue(answer)*/
    }

    fun createProcessDataRow(
        toPopulate: Sheet,
        process: String?,
        answer: String,
        rowCounter: Int,
        commonLoggingHolder: CommonLoggingHolder
    ) {
        logMessage(
            "populateSheet $toPopulate with $process $answer", LOG_LEVEL.DEBUG, commonLoggingHolder
        )
        val data = toPopulate.createRow(rowCounter)
        /*data.createCell(0).setCellValue(taskExecutor.getAIProvider())
        data.createCell(1).setCellValue(taskExecutor.getChatModel())
        data.createCell(2).setCellValue(taskExecutor.getEmbeddingModel())
        data.createCell(3).setCellValue(taskExecutor.getProcessSpecificSystemPromptTemplate())
        data.createCell(4).setCellValue(taskExecutor.getProcessSpecificQuestionTemplate())
        data.createCell(5).setCellValue(process)
        data.createCell(6).setCellValue(answer)*/
    }

    fun createUserTaskHeaders(toPopulate: Sheet, commonLoggingHolder: CommonLoggingHolder) {
        logMessage(
            "createUserTaskHeaders $toPopulate", LOG_LEVEL.DEBUG, commonLoggingHolder
        )
        createHeaders(
            toPopulate,
            listOf("com.ibm.bamoe.usertask.summarization.service.tweaker.TweakerExecutor.USER_TASK_SETTINGS_COLUMNS"),
            commonLoggingHolder
        )
    }

    fun createProcessesHeaders(toPopulate: Sheet, commonLoggingHolder: CommonLoggingHolder) {
        logMessage(
            "createProcessesHeaders $toPopulate", LOG_LEVEL.DEBUG, commonLoggingHolder
        )
        createHeaders(
            toPopulate,
            listOf("com.ibm.bamoe.usertask.summarization.service.tweaker.TweakerExecutor.PROCESSES_SETTINGS_COLUMNS"),
            commonLoggingHolder
        )
    }

    fun createHeaders(toPopulate: Sheet, headers: List<String>, commonLoggingHolder: CommonLoggingHolder) {
        logMessage("createHeaders $toPopulate", LOG_LEVEL.DEBUG, commonLoggingHolder)
        val header = toPopulate.createRow(0)
        val counter = AtomicInteger(0)
        headers.forEach(Consumer { column: String? -> header.createCell(counter.getAndAdd(1)).setCellValue(column) })
        /*com.ibm.bamoe.usertask.summarization.service.tweaker.TweakerExecutor.SCORING_COLUMNS.forEach(Consumer { column: String? ->
            header.createCell(
                counter.getAndAdd(1)
            ).setCellValue(column)
        })*/
    }

    fun createWorkbook(outputFilePath: String, commonLoggingHolder: CommonLoggingHolder): Workbook? {
        logMessage(
            "createWorkbook $outputFilePath", LOG_LEVEL.DEBUG, commonLoggingHolder
        )
        val xssf: Boolean = outputFilePath.lowercase(Locale.getDefault()).endsWith(".xlsx")
        try {
            return WorkbookFactory.create(xssf)
        } catch (e: Exception) {
            throw IllegalStateException(
                "Failed to create Workbook on file $outputFilePath. Please check logs.", e
            )
        }
    }

    fun saveWorkbook(toSave: Workbook, outputFilePath: String, commonLoggingHolder: CommonLoggingHolder) {
        logMessage(
            "saveWorkbook $toSave to $outputFilePath", LOG_LEVEL.DEBUG, commonLoggingHolder
        )
        saveWorkbook(toSave, outputFilePath, commonLoggingHolder)
    }

    fun saveWorkbook(
        toSave: Workbook,
        outputPath: String,
        outputFilePath: String,
        commonLoggingHolder: CommonLoggingHolder
    ) {
        logMessage(
            "saveWorkbook $toSave to $outputPath", LOG_LEVEL.DEBUG, commonLoggingHolder
        )
        val originalPath = Paths.get(outputPath)
        val originalOutputFile = originalPath.toFile()
        val newFile = FilenameUtils.getBaseName(originalOutputFile.getName()) + "_new." + FilenameUtils.getExtension(
            originalOutputFile.getName()
        )
        val newPath = Paths.get(newFile)
        logMessage(
            "Full path ${newPath.toFile().absolutePath}", LOG_LEVEL.DEBUG, commonLoggingHolder
        )
        try {
            val outputStream: OutputStream = FileOutputStream(newPath.toFile())
            toSave.write(outputStream)
            toSave.close()
            outputStream.close()
            logMessage(
                "Moving new file ${newPath.toFile().absolutePath} to old one $outputPath",
                LOG_LEVEL.DEBUG,
                commonLoggingHolder
            )
            Files.move(newPath, originalPath, StandardCopyOption.REPLACE_EXISTING)
        } catch (e: java.lang.Exception) {
            throw java.lang.IllegalStateException(
                "Failed to save Workbook on file $outputFilePath. Please check logs.", e
            )
        }
    }

    fun readWorkbook(xlsFile: File, commonLoggingHolder: CommonLoggingHolder): Workbook {
        logMessage("readWorkbook $xlsFile", LOG_LEVEL.DEBUG, commonLoggingHolder)
        try {
            return WorkbookFactory.create(xlsFile)
        } catch (e: Exception) {
            throw IllegalStateException(
                "Problems with content of xls file ${xlsFile.absolutePath}. Please check it is a proper xlsx file.", e
            )
        }
    }

    fun getXlsFile(outputFilePath: String, commonLoggingHolder: CommonLoggingHolder): File? {
        logMessage("getXlsFile $outputFilePath", LOG_LEVEL.DEBUG, commonLoggingHolder)
        check(!outputFilePath.isEmpty()) { "Output file path is null or empty: please check and provide the 'xls.output.file' property inside 'application.properties" }
        try {
            val path: Path = Paths.get(outputFilePath)
            if (!path.toFile().exists()) {
                return null
            }
            check(
                !(!path.toFile().canRead() || !path.toFile().canWrite() || path.toFile().isDirectory())
            ) {
                String.format(
                    "Problems with permissions or structure of xls file %s. Please check it is a proper readable/writable file.",
                    path.toFile().absolutePath
                )
            }
            return path.toFile()
        } catch (e: Exception) {
            throw IllegalStateException(
                String.format("Failed to retrieve file %s.", outputFilePath), e
            )
        }
    }
}