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


fun addComponentToFile(component: String, fileName: String) = File("$fileName.puml").appendText("\r\n[$component]")

fun initFile(localFileName: String, log: Log) {
    log.debug("initFile with $localFileName")
    val actualFileName = "$localFileName.puml"
    File(actualFileName).writeText("@startuml")
    File(actualFileName).appendText("\r\nleft to right direction")
}

fun done(localFileName: String, log: Log) {
    log.debug("... done $localFileName")
    val actualFileName = "$localFileName.puml"
    File(actualFileName).appendText("\r\n@enduml")
}

fun writeRelationship(relationship: PrintMojo.Relationship, localFileName: String, log: Log) {
    log.debug("writeRelationship to $localFileName")
    val actualFileName = "$localFileName.puml"
    when (relationship.relation) {
        PrintMojo.RELATION.PARENT -> {
            File(actualFileName).appendText("\r\n[${relationship.relatedComponent}] <-- [${relationship.currentComponent}] : extend")
        }
        PrintMojo.RELATION.CHILD -> {
            File(actualFileName).appendText("\r\n[${relationship.currentComponent}] <-- [${relationship.relatedComponent}] : extend")
        }
        PrintMojo.RELATION.IMPORT -> {
            File(actualFileName).appendText("\r\n[${relationship.currentComponent}] ..> [${relationship.relatedComponent}] : import")
        }
    }
}