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

import org.kie.maven.blueprinter.plugin.AbstractBluePrinterMojo.LOG_LEVEL
import org.kie.maven.blueprinter.plugin.PrintMojo
import org.kie.maven.blueprinter.plugin.dataclass.CommonLoggingHolder
import org.kie.maven.blueprinter.plugin.dataclass.ComponentModel
import org.kie.maven.blueprinter.plugin.dataclass.Relationship
import org.kie.maven.blueprinter.plugin.relationshipwriters.RelationshipWriter
import org.kie.maven.blueprinter.plugin.utils.logMessage
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
        override fun writeRelationships(
            relationshipSet: HashSet<Relationship>,
            outputDirectory: String,
            commonLoggingHolder: CommonLoggingHolder
        ) {
            logMessage("Write all Relationships to PUML files", LOG_LEVEL.DEBUG, commonLoggingHolder)
            val currentRelationshipMap: Map<ComponentModel, List<Relationship>> =
                relationshipSet.groupBy({ it.currentComponent }, { it })
            val aliasDeclarations = mutableSetOf<String>()
            val relationships = mutableSetOf<String>()
            val pumlFiles = currentRelationshipMap.entries.map {
                writeRelationshipsToPUML(it, outputDirectory, aliasDeclarations, relationships, commonLoggingHolder)
            }.toMutableSet()
            // This is to generate files for components that never appear as "currentComponent" on the relationships
            val relatedRelationshipMap: Map<ComponentModel, List<Relationship>> =
                relationshipSet.groupBy({ it.relatedComponent }, { it })
            val relatedComponentFiles = relatedRelationshipMap.entries
                .filterNot {
                    val targetFile = File("$outputDirectory${File.separator}${it.key.linkedFile}.puml");
                    pumlFiles.contains(targetFile)
                }
                .map {
                    writeRelationshipsToPUML(
                        it,
                        outputDirectory,
                        aliasDeclarations,
                        relationships,
                        commonLoggingHolder,
                        true
                    )
                }.toSet()
            pumlFiles.addAll(relatedComponentFiles)
            pumlFiles.add(createIndexFile(currentRelationshipMap.keys, outputDirectory, commonLoggingHolder))
            pumlFiles.add(createUberMap(outputDirectory, aliasDeclarations, relationships, commonLoggingHolder))
            createHTMLFiles(pumlFiles)
        }

        fun createUberMap(
            outputDirectory: String,
            aliasDeclarations: MutableSet<String>,
            relationships: MutableSet<String>,
            commonLoggingHolder: CommonLoggingHolder
        ): File {
            logMessage("Generate  PUML files in a single one", LOG_LEVEL.DEBUG, commonLoggingHolder)
            val uberFile = File("ubermap.puml")
            var toReturn : File
            if (uberFile.exists()) {
                uncompleteFile(uberFile, commonLoggingHolder)
                toReturn = uberFile
            } else {
                toReturn = initFile(
                    "Uber Map",
                    "ubermap",
                    outputDirectory,
                    true,
                    commonLoggingHolder
                )
            }
            aliasDeclarations.forEach { conditionallyAddText(toReturn, it, commonLoggingHolder) }
            relationships.forEach { conditionallyAddText(toReturn, it, commonLoggingHolder) }
            completeFile(toReturn, commonLoggingHolder)
            return toReturn
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
        private fun writeRelationshipsToPUML(
            relationshipMapEntry: Map.Entry<ComponentModel, List<Relationship>>,
            outputDirectory: String,
            aliasDeclarations: MutableSet<String>,
            relationships: MutableSet<String>,
            commonLoggingHolder: CommonLoggingHolder,
            relatedPointOfView: Boolean = false
        ): File {
            logMessage(
                "Write relationship' map entry to PUML file $outputDirectory${File.separator}${relationshipMapEntry.key.linkedFile}",
                LOG_LEVEL.DEBUG,
                commonLoggingHolder
            )
            val toReturn = initFile(
                relationshipMapEntry.key.gaIdentifier,
                relationshipMapEntry.key.linkedFile,
                outputDirectory,
                true,
                commonLoggingHolder
            )
            relationshipMapEntry.value.forEach { relationship ->
                val relatedComponent =
                    if (relatedPointOfView) relationship.currentComponent else relationship.relatedComponent
                aliasDeclarations.add(writeAliasDeclaration(relatedComponent, toReturn, commonLoggingHolder))
                relationships.add(writeRelationship(relationship, toReturn, commonLoggingHolder, relatedPointOfView))
            }
            completeFile(toReturn, commonLoggingHolder)
            return toReturn
        }

        private fun createIndexFile(
            currentComponents: Set<ComponentModel>,
            outputDirectory: String,
            commonLoggingHolder: CommonLoggingHolder
        ): File {
            logMessage(
                "Write components to PUML index file $outputDirectory${File.separator}index",
                LOG_LEVEL.DEBUG,
                commonLoggingHolder
            )
            val sortedComponents: List<ComponentModel> =
                currentComponents.sortedBy { it.gaIdentifier }.toCollection(ArrayList())
            val toReturn = initFile("INDEX", "index", outputDirectory, false, commonLoggingHolder)
            sortedComponents.forEach {
                writeAliasDeclaration(it, toReturn, commonLoggingHolder)
            }
            if (sortedComponents.size > 1) {
                IntRange(0, sortedComponents.size - 2).forEach {
                    writeAliasOrdering(
                        sortedComponents[it].alias,
                        sortedComponents[it + 1].alias,
                        toReturn,
                        commonLoggingHolder
                    )
                }
            }
            completeFile(toReturn, commonLoggingHolder)
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
        private fun initFile(
            title: String,
            localFileName: String,
            outputDirectory: String,
            leftToRight: Boolean,
            commonLoggingHolder: CommonLoggingHolder
        ): File {
            val actualFileName = "$outputDirectory${File.separator}$localFileName.puml"
            logMessage("initFile $actualFileName", LOG_LEVEL.DEBUG, commonLoggingHolder)
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
            toReturn.appendText("\r\n!option handwritten true")
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
        private fun writeAliasDeclaration(
            relatedComponent: ComponentModel,
            destination: File,
            commonLoggingHolder: CommonLoggingHolder
        ): String {
            logMessage("writeAliasDeclaration to ${destination.absolutePath}", LOG_LEVEL.DEBUG, commonLoggingHolder)
            val toReturn =
                "\r\n[${relatedComponent.gaIdentifier}] as ${relatedComponent.alias} [[${relatedComponent.linkedFile}.html]]"
            destination.appendText(toReturn)
            return toReturn
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
        private fun writeAliasOrdering(
            leftAlias: String,
            rightAlias: String,
            destination: File,
            commonLoggingHolder: CommonLoggingHolder
        ): String {
            logMessage("writeAliasOrdering to ${destination.absolutePath}", LOG_LEVEL.DEBUG, commonLoggingHolder)
            val toReturn = "\r\n$leftAlias -[hidden]down- $rightAlias"
            destination.appendText(toReturn)
            return toReturn
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
        private fun writeRelationship(
            relationship: Relationship,
            destination: File,
            commonLoggingHolder: CommonLoggingHolder,
            relatedPointOfView: Boolean = false
        ): String {
            logMessage("writeRelationship to ${destination.absolutePath}", LOG_LEVEL.DEBUG, commonLoggingHolder)
            val extend = "extend"
            val import = "import"
            var toReturn = ""
            when (relationship.relation) {
                PrintMojo.RELATION.PARENT -> {
                    val left =
                        if (relatedPointOfView) relationship.relatedComponent.gaIdentifier else relationship.relatedComponent.alias
                    val right =
                        if (relatedPointOfView) relationship.currentComponent.alias else relationship.currentComponent.gaIdentifier
                    toReturn = "\r\n[$left] <-- [$right] : $extend"
                }

                PrintMojo.RELATION.CHILD -> {
                    val left =
                        if (relatedPointOfView) relationship.currentComponent.alias else relationship.currentComponent.gaIdentifier
                    val right =
                        if (relatedPointOfView) relationship.relatedComponent.gaIdentifier else relationship.relatedComponent.alias
                    toReturn = "\r\n[$left] <-- [$right] : $extend"
                }

                PrintMojo.RELATION.IMPORT -> {
                    val left =
                        if (relatedPointOfView) relationship.currentComponent.alias else relationship.currentComponent.gaIdentifier
                    val right =
                        if (relatedPointOfView) relationship.relatedComponent.gaIdentifier else relationship.relatedComponent.alias
                    toReturn = "\r\n[$left] ..> [$right] : $import"
                }
            }
            destination.appendText(toReturn)
            return toReturn
        }


        /**
         * Write last - fixed - line to [destination] file
         *
         * [destination]
         * [log]
         */
        private fun completeFile(destination: File, commonLoggingHolder: CommonLoggingHolder) {
            logMessage("... done ${destination.absolutePath}", LOG_LEVEL.DEBUG, commonLoggingHolder)
            destination.appendText("\r\n@enduml")
        }

        /**
         * Write last - fixed - line to [destination] file
         *
         * [destination]
         * [log]
         */
        private fun uncompleteFile(destination: File, commonLoggingHolder: CommonLoggingHolder) {
            logMessage("... done ${destination.absolutePath}", LOG_LEVEL.DEBUG, commonLoggingHolder)
            val lines = destination.readLines()
            val cleanedString = lines.dropLast(1).joinToString("\r\n")
            destination.writeText(cleanedString)
        }

        /**
         * Write last - fixed - line to [destination] file
         *
         * [destination]
         * [log]
         */
        private fun conditionallyAddText(destination: File, toAdd: String, commonLoggingHolder: CommonLoggingHolder) {
            logMessage("... done ${destination.absolutePath}", LOG_LEVEL.DEBUG, commonLoggingHolder)
            val lines = destination.readLines()
            if (!lines.contains(toAdd)) {
                destination.appendText("\r\n$toAdd")
            }
        }
    }

}
