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

import org.apache.maven.plugin.logging.Log
import org.kie.maven.blueprinter.plugin.PrintMojo
import org.kie.maven.blueprinter.plugin.dataclass.ComponentModel
import org.kie.maven.blueprinter.plugin.dataclass.Relationship
import org.kie.maven.blueprinter.plugin.relationshipwriters.RelationshipWriter
import java.io.File


/**
 * **PUML** implementation of [RelationshipWriter].
 *
 * Write the **Relationship** tree on **HTML** files in **puml** component format.
 * Such diagrams are added to html files as **svg** images.
 *
 */
class PUMLWriter {

    companion object : RelationshipWriter {

        /**
         * Write all the given [Relationship]s to **puml** files
         *
         * [relationshipSet] the [Relationship]s to write
         * [outputDirectory]
         * [log]
         */
        override fun writeRelationships(relationshipSet: HashSet<Relationship>, outputDirectory: String, log: Log)/*: Set<File>*/ {
            log.debug("Write all Relationships to PUML files")
            val currentRelationshipMap: Map<ComponentModel, List<Relationship>> = relationshipSet.groupBy({ it.currentComponent }, { it })
            val pumlFiles = currentRelationshipMap.entries.map {
                writeRelationshipsToPUML(it, outputDirectory, log)
            }.toMutableSet()
            // This is to generate files for components that never appear as "currentComponent" on the relationships
            val relatedRelationshipMap: Map<ComponentModel, List<Relationship>> = relationshipSet.groupBy({ it.relatedComponent }, { it })
            val relatedComponentFiles = relatedRelationshipMap.entries
                    .filterNot {
                        val targetFile = File("$outputDirectory${File.separator}${it.key.linkedFile}.puml");
                        pumlFiles.contains(targetFile)
                    }
                    .map {
                        writeRelationshipsToPUML(it, outputDirectory, log, true)
                    }.toSet()
            pumlFiles.addAll(relatedComponentFiles)
            pumlFiles.add(createIndexFile(currentRelationshipMap.keys, outputDirectory, log))
            createHTMLFiles(pumlFiles)
        }

        /**
         * Create all the _html_ [File]s from the given [Set] of _puml_ ones
         *
         * [pumlFiles]
         */
        private fun createHTMLFiles(pumlFiles: Set<File>) {
            val svgFilesMap = createSVGFilesMap(pumlFiles)
            createHTMLFiles(svgFilesMap)
        }

        /**
         * Write a [Map.Entry&lt;ComponentModel, List&lt;Relationship&gt;&gt;] to a PUML file
         *
         * [relationshipMapEntry] the [Map.Entry&lt;ComponentModel, List&lt;Relationship&gt;&gt;] to write
         * [outputDirectory]
         * [log]
         * [relatedPointOfView] if [true], it consider the PUML file to describe the **relatedComponent** relationships, otherwise (default) it consider the **currentComponent** one
         */
        private fun writeRelationshipsToPUML(relationshipMapEntry: Map.Entry<ComponentModel, List<Relationship>>, outputDirectory: String, log: Log, relatedPointOfView: Boolean = false): File {
            log.debug("Write relationship' map entry to PUML file $outputDirectory${File.separator}${relationshipMapEntry.key.linkedFile}")
            val toReturn = initFile(relationshipMapEntry.key.gaIdentifier, relationshipMapEntry.key.linkedFile, outputDirectory, true, log)
            relationshipMapEntry.value.forEach { relationship ->
                val relatedComponent = if (relatedPointOfView) relationship.currentComponent else relationship.relatedComponent
                writeAliasDeclaration(relatedComponent, toReturn, log)
                writeRelationship(relationship, toReturn, log, relatedPointOfView)
            }
            completeFile(toReturn, log)
            return toReturn
        }

        private fun createIndexFile(currentComponents: Set<ComponentModel>, outputDirectory: String, log: Log): File {
            log.debug("Write components to PUML index file $outputDirectory${File.separator}index")
            val sortedComponents: List<ComponentModel> = currentComponents.sortedBy { it.gaIdentifier }.toCollection(ArrayList())
            val toReturn = initFile("INDEX", "index", outputDirectory, false, log)
            sortedComponents.forEach {
                writeAliasDeclaration(it, toReturn, log)
            }
            if (sortedComponents.size > 1) {
                IntRange(0, sortedComponents.size - 2).forEach { writeAliasOrdering(sortedComponents[it].alias, sortedComponents[it + 1].alias, toReturn, log) }
            }
            completeFile(toReturn, log)
            return toReturn;
        }

        /**
         * Create PUML file with initial - fixed -lines
         *
         * [title]
         * [localFileName]
         * [outputDirectory]
         * [log]
         */
        private fun initFile(title: String, localFileName: String, outputDirectory: String, leftToRight: Boolean, log: Log): File {
            val actualFileName = "$outputDirectory${File.separator}$localFileName.puml"
            log.debug("initFile $actualFileName")
            val outputDir = File(outputDirectory)
            if (!outputDir.exists()) {
                outputDir.mkdir()
            }
            val toReturn = File(actualFileName)
            toReturn.writeText("@startuml")
            if (leftToRight) {
                toReturn.appendText("\r\nleft to right direction")
            }
            toReturn.appendText("\r\nskinparam titleBorderRoundCorner 15")
            toReturn.appendText("\r\nskinparam titleBorderThickness 2")
            toReturn.appendText("\r\nskinparam titleBorderColor red")
            toReturn.appendText("\r\nskinparam titleBackgroundColor Aqua-CadetBlue")
            toReturn.appendText("\r\nskinparam svgLinkTarget _new")
            toReturn.appendText("\r\nskinparam handwritten true")
            toReturn.appendText("\r\ntitle $title (preview version)")
            return toReturn
        }

        /**
         * Write alias declaration for [relatedComponent] to [destination] file
         *
         * [relatedComponent] the component to be considered as "related"
         * [destination]
         * [log]
         *
         */
        private fun writeAliasDeclaration(relatedComponent: ComponentModel, destination: File, log: Log) {
            log.debug("writeAliasDeclaration to ${destination.absolutePath}")
            destination.appendText("\r\n[${relatedComponent.gaIdentifier}] as ${relatedComponent.alias} [[${relatedComponent.linkedFile}.html]]")
        }

        /**
         * Write alias ordering to [destination] file
         *
         * [leftAlias]
         * [rightAlias]
         * [destination]
         * [log]
         *
         */
        private fun writeAliasOrdering(leftAlias: String, rightAlias: String, destination: File, log: Log) {
            log.debug("writeAliasOrdering to ${destination.absolutePath}")
            destination.appendText("\r\n$leftAlias -[hidden]down- $rightAlias")
        }

        /**
         * Write components [Relationship] to [destination] file
         *
         * [relationship]
         * [destination]
         * [log]
         * [relatedPointOfView] if [true], it consider the relation to describe the **relatedComponent** relationships, otherwise (default) it consider the **currentComponent** one
         *
         */
        private fun writeRelationship(relationship: Relationship, destination: File, log: Log, relatedPointOfView: Boolean = false) {
            log.debug("writeRelationship to ${destination.absolutePath}")
            val extend = "extend"
            val import = "import"
            when (relationship.relation) {
                PrintMojo.RELATION.PARENT -> {
                    val left = if (relatedPointOfView) relationship.relatedComponent.gaIdentifier else relationship.relatedComponent.alias
                    val right = if (relatedPointOfView) relationship.currentComponent.alias else relationship.currentComponent.gaIdentifier
                    destination.appendText("\r\n[$left] <-- [$right] : $extend")
                }
                PrintMojo.RELATION.CHILD -> {
                    val left = if (relatedPointOfView) relationship.currentComponent.alias else relationship.currentComponent.gaIdentifier
                    val right = if (relatedPointOfView) relationship.relatedComponent.gaIdentifier else relationship.relatedComponent.alias
                    destination.appendText("\r\n[$left] <-- [$right] : $extend")
                }
                PrintMojo.RELATION.IMPORT -> {
                    val left = if (relatedPointOfView) relationship.currentComponent.alias else relationship.currentComponent.gaIdentifier
                    val right = if (relatedPointOfView) relationship.relatedComponent.gaIdentifier else relationship.relatedComponent.alias
                    destination.appendText("\r\n[$left] ..> [$right] : $import")
                }
            }
        }

        /**
         * Write last - fixed - line to [destination] file
         *
         * [destination]
         * [log]
         */
        private fun completeFile(destination: File, log: Log) {
            log.debug("... done ${destination.absolutePath}")
            destination.appendText("\r\n@enduml")
        }

    }

}
