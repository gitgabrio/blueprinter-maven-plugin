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
package org.kie.maven.blueprinter.plugin.mavenprojectvisitors

import org.apache.maven.model.Dependency
import org.apache.maven.model.DependencyManagement
import org.apache.maven.plugin.MojoFailureException
import org.apache.maven.plugin.logging.Log
import org.apache.maven.project.MavenProject
import org.apache.maven.project.ProjectBuildingException
import org.kie.maven.blueprinter.plugin.PrintMojo
import org.kie.maven.blueprinter.plugin.dataclass.CommonObjectHolder
import org.kie.maven.blueprinter.plugin.dataclass.ComponentModel
import org.kie.maven.blueprinter.plugin.dataclass.Relationship
import org.kie.maven.blueprinter.plugin.utils.dependencyToComponentModel
import org.kie.maven.blueprinter.plugin.utils.dependencyToGAString
import org.kie.maven.blueprinter.plugin.utils.logDependency
import java.util.function.Consumer

/**
 * [DependencyManagement] specific [Visitor]
 */
class DependencyManagementVisitor {

    companion object : Visitor {
        private lateinit var dependencyManagement: DependencyManagement
        private lateinit var currentComponent: ComponentModel

        @Throws(MojoFailureException::class)
        override fun visit(commonObjectHolder: CommonObjectHolder) {

            dependencyManagement.dependencies?.filter { dependency -> dependency.scope == "import" }?.forEach(Consumer { dependency ->
                addDependencyRelationship(commonObjectHolder.globalRelationshipSet, dependency, PrintMojo.RELATION.IMPORT, currentComponent, commonObjectHolder.log)
                when (dependencyNotInMavenProjectList(commonObjectHolder.targetProjectCollectedProjects, dependency)) {
                    true -> {
                        navigateDependency(dependency, commonObjectHolder)
                    }
                    false -> {/*ignore*/
                    }
                }
            })
        }

        fun init(dependencyManagement: DependencyManagement, currentComponent: ComponentModel): Companion {
            this.dependencyManagement = dependencyManagement
            this.currentComponent = currentComponent
            return this
        }

        @Throws(MojoFailureException::class)
        private fun navigateDependency(dependency: Dependency, commonObjectHolder: CommonObjectHolder) {
            val dependencyString = dependencyToGAString(dependency)
            commonObjectHolder.log.debug("navigateDependency $dependencyString")
            val project = getMavenProject(dependency, commonObjectHolder)
            when (project != null) {
                true -> {
                    MavenProjectVisitor.init(project).visit(commonObjectHolder)
                }
                false -> commonObjectHolder.log.warn("Failed to retrieve Maven Project for ${dependencyToGAString(dependency)}")
            }
        }

        @Throws(ProjectBuildingException::class)
        private fun getMavenProject(dependency: Dependency, commonObjectHolder: CommonObjectHolder): MavenProject? {
            val resolvedVersion = resolveVersion(dependency.version, commonObjectHolder.targetProject)
            return when (resolvedVersion != null) {
                true -> {
                    val artifact = commonObjectHolder.repositorySystem.createProjectArtifact(dependency.groupId, dependency.artifactId, resolvedVersion)
                    val build = commonObjectHolder.mavenProjectBuilder.build(artifact, commonObjectHolder.session.projectBuildingRequest)
                    build.project
                }
                false -> null
            }
        }

        private fun resolveVersion(version: String, project: MavenProject): String? {
            return when (version.contains("{")) {
                true -> {
                    val versionName = version.replace("\${", "").replace("}", "")
                    project.model.properties.getProperty(versionName)
                }
                false -> version
            }
        }

        private fun dependencyNotInMavenProjectList(listToCheck: ArrayList<MavenProject>, dependency: Dependency): Boolean {
            return listToCheck.none() { it.groupId == dependency.groupId && it.artifactId == dependency.artifactId }
        }

        private fun addDependencyRelationship(relationshipSet: HashSet<Relationship>, dependency: Dependency, relation: PrintMojo.RELATION, currentComponent: ComponentModel, log: Log) {
            logDependency(dependency, relation, log)
            val relatedComponent = dependencyToComponentModel(dependency)
            relationshipSet.add(Relationship(currentComponent, relatedComponent, relation))
        }
    }

}