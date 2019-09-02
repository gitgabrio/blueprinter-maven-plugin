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
import org.kie.maven.blueprinter.plugin.dataclass.ComponentModel
import org.kie.maven.blueprinter.plugin.dataclass.Relationship
import java.io.File

/**
 * Functions used to write **PUML** files
 */


fun writeRelationshipsToPUML(relationshipSet: HashSet<Relationship>, outputDirectory: String, log: Log): Set<File> {
    log.debug("Write relationships")
    val relationshipMap: Map<ComponentModel, List<Relationship>> = relationshipSet.groupBy({ it.currentComponent }, { it })
    return relationshipMap.entries.map {
        val writtenFile = initFile(it.key.gaIdentifier, it.key.linkedFile, outputDirectory, log)
        it.value.forEach { relationship ->
            writeAliasDeclaration(relationship.relatedComponent, writtenFile, log)
            writeRelationship(relationship, writtenFile, log)
        }
        completeFile(writtenFile, log)
        writtenFile
    }.toSet()
}

private fun initFile(gaIdentifier: String, localFileName: String, outputDirectory: String, log: Log): File {
    val actualFileName = "$outputDirectory${File.separator}$localFileName.puml"
    log.debug("initFile $actualFileName")
    val outputDir = File(outputDirectory)
    if (!outputDir.exists()) {
        outputDir.mkdir()
    }
    val toReturn = File(actualFileName)
    toReturn.writeText("@startuml")
    toReturn.appendText("\r\nleft to right direction")
    toReturn.appendText("\r\nskinparam titleBorderRoundCorner 15")
    toReturn.appendText("\r\nskinparam titleBorderThickness 2")
    toReturn.appendText("\r\nskinparam titleBorderColor red")
    toReturn.appendText("\r\nskinparam titleBackgroundColor Aqua-CadetBlue")
    toReturn.appendText("\r\nskinparam svgLinkTarget _new")
    toReturn.appendText("\r\ntitle $gaIdentifier")
    return toReturn
}

private fun writeAliasDeclaration(relatedComponent: ComponentModel, destination: File, log: Log) {
    log.debug("writeRelationship to ${destination.absolutePath}")
    destination.appendText("\r\n[${relatedComponent.gaIdentifier}] as ${relatedComponent.alias} [[${relatedComponent.linkedFile}.html]]")
}

private fun writeRelationship(relationship: Relationship, destination: File, log: Log) {
    log.debug("writeRelationship to ${destination.absolutePath}")
    var extend = "extend"
    var import = "import"
    when (relationship.relation) {
        PrintMojo.RELATION.PARENT -> {
            destination.appendText("\r\n[${relationship.relatedComponent.alias}] <-- [${relationship.currentComponent.gaIdentifier}] : $extend")
        }
        PrintMojo.RELATION.CHILD -> {
            destination.appendText("\r\n[${relationship.currentComponent.gaIdentifier}] <-- [${relationship.relatedComponent.alias}] : $extend")
        }
        PrintMojo.RELATION.IMPORT -> {
            destination.appendText("\r\n[${relationship.currentComponent.gaIdentifier}] ..> [${relationship.relatedComponent.alias}] : $import")
        }
    }
}

private fun completeFile(destination: File, log: Log) {
    log.debug("... done ${destination.absolutePath}")
    destination.appendText("\r\n@enduml")
}
