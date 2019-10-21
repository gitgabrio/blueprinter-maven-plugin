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
import org.apache.maven.plugin.logging.Log
import org.apache.maven.project.MavenProject
import org.kie.maven.blueprinter.plugin.PrintMojo

/**
 * Functions used to log specific objects
 */

fun logMavenProject(mavenProject: MavenProject, relation: PrintMojo.RELATION, log: Log) = log.debug("$relation: ${mavenProjectToGAString(mavenProject)}:${mavenProject.version}")
fun logDependency(dependency: Dependency, relation: PrintMojo.RELATION, log: Log) = log.debug("$relation: ${dependencyToGAString(dependency)}:${dependency.version}")
