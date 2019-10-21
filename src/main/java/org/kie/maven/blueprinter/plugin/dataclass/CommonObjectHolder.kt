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
package org.kie.maven.blueprinter.plugin.dataclass

import org.apache.maven.execution.MavenSession
import org.apache.maven.plugin.logging.Log
import org.apache.maven.project.MavenProject
import org.apache.maven.project.ProjectBuilder
import org.apache.maven.repository.RepositorySystem

/**
 * Utility class to store objects used throughout execution
 * [repositorySystem]
 * [mavenProjectBuilder]
 * [session]
 * [targetProject] the **maven** module currently built in a multi-module project, otherwise the only module present
 * [targetProjectCollectedProjects] the [MavenProject]s collected for the **target** one
 * [globalRelationshipSet] all the [Relationship]s identified by recursively read during overall execution
 * [outputDirectory] the **directory** where all html files will be written
 * [log] the single [Log] used throughout whole execution
 *
 */
data class CommonObjectHolder(val repositorySystem: RepositorySystem, val mavenProjectBuilder: ProjectBuilder, val session: MavenSession, val targetProject: MavenProject, val targetProjectCollectedProjects: ArrayList<MavenProject>, val globalRelationshipSet: HashSet<Relationship>,  val outputDirectory: String, val log: Log)