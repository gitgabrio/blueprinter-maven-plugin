package org.kie.maven.blueprinter.plugin

import net.sourceforge.plantuml.FileFormat
import net.sourceforge.plantuml.FileFormatOption
import net.sourceforge.plantuml.SourceFileReader
import java.io.File


/**
 * Functions using **PlantUml**
 */

fun createSVGFilesMap(pumlFiles: HashSet<File>): Map<File, HashSet<File>> {
    return pumlFiles.associateBy({it}, { createSVG(it)})
}


private fun createSVG(pumlFile: File): HashSet<File> {
    val reader = SourceFileReader(pumlFile, pumlFile.parentFile, FileFormatOption(FileFormat.SVG))
    val generatedImages = reader.generatedImages
    return generatedImages.map { it.pngFile }.toHashSet()
}


