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
package org.kie.maven.blueprinter.plugin

import org.apache.maven.plugin.logging.Log
import java.io.File

/**
 * Functions used to write **PUML** files
 */


fun addComponentToFile(component: String, destination: File) = destination.appendText("\r\n[$component]")

fun initFile(localFileName: String, outputDirectory: String, generatedFiles: HashSet<File>, log: Log): File {
    val actualFileName = "$outputDirectory${File.separator}$localFileName.puml"
    log.debug("initFile $actualFileName")
    val outputDir = File(outputDirectory)
    if (!outputDir.exists()) {
        outputDir.mkdir()
    }
    val toReturn = File(actualFileName)
    toReturn.writeText("@startuml")
    toReturn.appendText("\r\nleft to right direction")
    generatedFiles.add(toReturn)
    return toReturn
}

fun done(destination: File, log: Log) {
    log.debug("... done ${destination.absolutePath}")
    destination.appendText("\r\n@enduml")
}

fun writeRelationship(relationship: PrintMojo.Relationship, destination: File, log: Log) {
    log.debug("writeRelationship to ${destination.absolutePath}")
    when (relationship.relation) {
        PrintMojo.RELATION.PARENT -> {
            destination.appendText("\r\n[${relationship.relatedComponent}] <-- [${relationship.currentComponent}] : extend")
        }
        PrintMojo.RELATION.CHILD -> {
            destination.appendText("\r\n[${relationship.currentComponent}] <-- [${relationship.relatedComponent}] : extend")
        }
        PrintMojo.RELATION.IMPORT -> {
            destination.appendText("\r\n[${relationship.currentComponent}] ..> [${relationship.relatedComponent}] : import")
        }
    }
}