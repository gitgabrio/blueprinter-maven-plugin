/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kie.maven.blueprinter.plugin.relationshipwriters.pumlwriter

import net.sourceforge.plantuml.FileFormat
import net.sourceforge.plantuml.FileFormatOption
import net.sourceforge.plantuml.SourceFileReader
import java.io.File


/**
 * Functions using **PlantUml**
 */

fun createSVGFilesMap(pumlFiles: Set<File>): Map<File, HashSet<File>> {
    return pumlFiles.associateBy({it}, { createSVG(it) })
}


private fun createSVG(pumlFile: File): HashSet<File> {
    val reader = SourceFileReader(pumlFile, pumlFile.parentFile, FileFormatOption(FileFormat.SVG))
    val generatedImages = reader.generatedImages
    return generatedImages.map { it.pngFile }.toHashSet()
}


