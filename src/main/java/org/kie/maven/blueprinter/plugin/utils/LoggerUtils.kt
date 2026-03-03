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
package org.kie.maven.blueprinter.plugin.utils

import org.apache.maven.model.Dependency
import org.apache.maven.model.InputLocation
import org.apache.maven.plugin.logging.Log
import org.apache.maven.project.MavenProject
import org.kie.maven.blueprinter.plugin.AbstractBluePrinterMojo.LOG_LEVEL
import org.kie.maven.blueprinter.plugin.PrintMojo
import org.kie.maven.blueprinter.plugin.dataclass.CommonLoggingHolder

/**
 * Functions used to log specific objects
 */

fun logMavenProject(mavenProject: MavenProject, relation: PrintMojo.RELATION, commonLoggingHolder: CommonLoggingHolder) =
    logMessage("$relation: ${mavenProjectToGAString(mavenProject)}:${mavenProject.version}", LOG_LEVEL.DEBUG, commonLoggingHolder)
fun logDependency(dependency: Dependency, relation: PrintMojo.RELATION, commonLoggingHolder: CommonLoggingHolder) =
    logMessage("$relation: ${dependencyToGAString(dependency)}:${dependency.version}", LOG_LEVEL.DEBUG, commonLoggingHolder)
fun logDependency(dependency: Dependency, location: InputLocation, commonLoggingHolder: CommonLoggingHolder) =
    logMessage("$location: ${dependencyToGAString(dependency)}:${dependency.version}", LOG_LEVEL.DEBUG, commonLoggingHolder)

fun logMessage(message: String, logLevel: LOG_LEVEL, commonLoggingHolder: CommonLoggingHolder) {
    when (logLevel) {
        LOG_LEVEL.INFO -> commonLoggingHolder.log.get().info(message)
        LOG_LEVEL.WARN -> commonLoggingHolder.log.get().warn(message)
        LOG_LEVEL.ERROR -> commonLoggingHolder.log.get().warn(message)
        LOG_LEVEL.DEBUG -> if (commonLoggingHolder.debug) {
            commonLoggingHolder.log.get().debug(message)
        }
    }
}