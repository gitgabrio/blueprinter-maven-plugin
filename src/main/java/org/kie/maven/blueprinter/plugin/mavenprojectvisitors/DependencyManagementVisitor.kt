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
import org.apache.maven.model.InputLocation
import org.apache.maven.plugin.MojoFailureException
import org.apache.maven.project.MavenProject
import org.apache.maven.project.ProjectBuildingException
import org.kie.maven.blueprinter.plugin.AbstractBluePrinterMojo
import org.kie.maven.blueprinter.plugin.PrintMojo
import org.kie.maven.blueprinter.plugin.dataclass.*
import org.kie.maven.blueprinter.plugin.utils.dependencyToComponentModel
import org.kie.maven.blueprinter.plugin.utils.dependencyToGAString
import org.kie.maven.blueprinter.plugin.utils.logDependency
import org.kie.maven.blueprinter.plugin.utils.logMessage
import java.util.function.Consumer

/**
 * [DependencyManagement] specific [Visitor]
 */
class DependencyManagementVisitor {

    companion object : Visitor {
        private var dependencyManagement: DependencyManagement? = null
        private var dependencies: List<Dependency>? = null
        private lateinit var currentComponent: ComponentModel

        @Throws(MojoFailureException::class)
        override fun visitForPrint(printObjectHolder: PrintObjectHolder) {
            dependencyManagement?.dependencies?.filter { dependency -> dependency.scope == "import" }
                ?.forEach(Consumer { dependency ->
                    addDependencyRelationship(
                        printObjectHolder.globalRelationshipSet,
                        dependency,
                        PrintMojo.RELATION.IMPORT,
                        currentComponent,
                        printObjectHolder.commonLoggingHolder()
                    )
                    when (dependencyNotInMavenProjectList(
                        printObjectHolder.targetProjectCollectedProjects(),
                        dependency
                    )) {
                        true -> {
                            navigateDependency(dependency, printObjectHolder)
                        }

                        false -> {/*ignore*/
                        }
                    }
                })
        }

        @Throws(MojoFailureException::class)
        override fun visitForAnalysis(analyzerObjectHolder: AnalyzerObjectHolder) {
            dependencies?.filterNot { dependency -> isDroolsDomain(dependency) }
                ?.forEach { dependency ->
                    logMessage(
                        "visitForAnalysis ${dependencyToGAString(dependency)}",
                        AbstractBluePrinterMojo.LOG_LEVEL.DEBUG,
                        analyzerObjectHolder.commonLoggingHolder()
                    )
                    addDependencyVersions(
                        analyzerObjectHolder.globalDependencyVersionsMap,
                        dependency,
                        analyzerObjectHolder.commonLoggingHolder()
                    )
                }

            dependencyManagement?.dependencies?.forEach { dependency ->
                logMessage(
                    "visitForAnalysis ${dependencyToGAString(dependency)} with scope ${dependency.scope}",
                    AbstractBluePrinterMojo.LOG_LEVEL.DEBUG,
                    analyzerObjectHolder.commonLoggingHolder()
                )
                when (dependency.scope) {
                    "import" -> manageImportedDependency(dependency, analyzerObjectHolder)
                    else -> addDependencyVersions(
                        analyzerObjectHolder.globalDependencyVersionsMap,
                        dependency,
                        analyzerObjectHolder.commonLoggingHolder()
                    )
                }

            }
        }

        private fun manageImportedDependency(dependency: Dependency, analyzerObjectHolder: AnalyzerObjectHolder) {
            when (dependencyNotInMavenProjectList(
                analyzerObjectHolder.targetProjectCollectedProjects(),
                dependency
            )) {
                true -> {
                    navigateDependency(dependency, analyzerObjectHolder)
                }

                false -> {/*ignore*/
                }
            }
        }

        fun init(dependencyManagement: DependencyManagement, currentComponent: ComponentModel): Companion {
            this.dependencyManagement = dependencyManagement
            this.currentComponent = currentComponent
            return this
        }

        fun init(dependencies: List<Dependency>, currentComponent: ComponentModel): Companion {
            this.dependencies = dependencies
            this.currentComponent = currentComponent
            return this
        }

        @Throws(MojoFailureException::class)
        private fun navigateDependency(dependency: Dependency, printObjectHolder: PrintObjectHolder) {
            val dependencyString = dependencyToGAString(dependency)
            logMessage(
                "navigateDependency $dependencyString",
                AbstractBluePrinterMojo.LOG_LEVEL.DEBUG, printObjectHolder.commonLoggingHolder()
            )
            val project = getMavenProject(dependency, printObjectHolder)
            when (project != null) {
                true -> {
                    MavenProjectVisitor.init(project!!).visitForPrint(printObjectHolder)
                }

                false -> logMessage(
                    "Failed to retrieve Maven Project for ${dependencyToGAString(dependency)}",
                    AbstractBluePrinterMojo.LOG_LEVEL.WARN, printObjectHolder.commonLoggingHolder()
                )
            }
        }

        @Throws(MojoFailureException::class)
        private fun navigateDependency(dependency: Dependency, analyzerObjectHolder: AnalyzerObjectHolder) {
            val dependencyString = dependencyToGAString(dependency)
            logMessage(
                "navigateDependency $dependencyString",
                AbstractBluePrinterMojo.LOG_LEVEL.DEBUG, analyzerObjectHolder.commonLoggingHolder()
            )
            val project = getMavenProject(dependency, analyzerObjectHolder)
            when (project != null) {
                true -> {
                    MavenProjectVisitor.init(project!!).visitForAnalysis(analyzerObjectHolder)
                }

                false -> logMessage(
                    "Failed to retrieve Maven Project for ${dependencyToGAString(dependency)}",
                    AbstractBluePrinterMojo.LOG_LEVEL.WARN, analyzerObjectHolder.commonLoggingHolder()
                )
            }
        }

        @Throws(ProjectBuildingException::class)
        private fun getMavenProject(dependency: Dependency, printObjectHolder: PrintObjectHolder): MavenProject? {
            return getMavenProject(dependency, printObjectHolder.commonObjectHolder)
        }

        @Throws(ProjectBuildingException::class)
        private fun getMavenProject(dependency: Dependency, analyzerObjectHolder: AnalyzerObjectHolder): MavenProject? {
            return getMavenProject(dependency, analyzerObjectHolder.commonObjectHolder)
        }

        @Throws(ProjectBuildingException::class)
        private fun getMavenProject(dependency: Dependency, commonObjectHolder: CommonObjectHolder): MavenProject? {
            val resolvedVersion = resolveVersion(dependency.version, commonObjectHolder.targetProject)
            return when (resolvedVersion != null) {
                true -> {
                    val artifact = commonObjectHolder.repositorySystem.createProjectArtifact(
                        dependency.groupId,
                        dependency.artifactId,
                        resolvedVersion
                    )
                    val build = commonObjectHolder.mavenProjectBuilder.build(
                        artifact,
                        commonObjectHolder.session.projectBuildingRequest
                    )
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

        private fun dependencyNotInMavenProjectList(
            listToCheck: ArrayList<MavenProject>,
            dependency: Dependency
        ): Boolean {
            return listToCheck.none() { it.groupId == dependency.groupId && it.artifactId == dependency.artifactId }
        }

        private fun addDependencyRelationship(
            relationshipSet: HashSet<Relationship>,
            dependency: Dependency,
            relation: PrintMojo.RELATION,
            currentComponent: ComponentModel,
            commonLoggingHolder: CommonLoggingHolder
        ) {
            logDependency(dependency, relation, commonLoggingHolder)
            val relatedComponent = dependencyToComponentModel(dependency)
            relationshipSet.add(Relationship(currentComponent, relatedComponent, relation))
        }

        private fun addDependencyVersions(
            globalDependencyVersionsMap: MutableMap<String, MutableSet<VersionsTuple>>,
            dependency: Dependency,
            commonLoggingHolder: CommonLoggingHolder
        ) {
            val versionLocation = dependency.getLocation("version")
            when (isDroolsDomain(versionLocation) && !isDroolsDomain(dependency)) {
                true -> {
                    logMessage(
                        "Add dependency version for $dependency because does not belong to 'drools domain' but it is defined in a drools-domain pom $versionLocation",
                        AbstractBluePrinterMojo.LOG_LEVEL.DEBUG,
                        commonLoggingHolder
                    )
                    addDependencyVersion(globalDependencyVersionsMap, dependency, versionLocation, commonLoggingHolder)
                }
                else -> {
                    logMessage(
                        "Skipping version logging for ${dependencyToGAString(dependency)} with versionInput $versionLocation as it is in not the Drools domain",
                        AbstractBluePrinterMojo.LOG_LEVEL.DEBUG,
                        commonLoggingHolder
                    )
                }
            }
        }

        private fun addDependencyVersion(
            globalDependencyVersionsMap: MutableMap<String, MutableSet<VersionsTuple>>,
            dependency: Dependency,
            versionLocation: InputLocation,
            commonLoggingHolder: CommonLoggingHolder
        ) {
            logDependency(dependency, versionLocation, commonLoggingHolder)
            val dependencyLocation = dependency.getLocation("")
            val versionsTuple = VersionsTuple(dependency.version, dependencyLocation, versionLocation)
            val gaString = dependencyToGAString(dependency)
            if (globalDependencyVersionsMap.contains(gaString)) {
                globalDependencyVersionsMap[gaString]?.add(versionsTuple)
                logMessage(
                    "add $versionsTuple to $gaString",
                    AbstractBluePrinterMojo.LOG_LEVEL.DEBUG,
                    commonLoggingHolder
                )
            } else {
                globalDependencyVersionsMap[gaString] = mutableSetOf(versionsTuple)
            }
        }

        private fun isDroolsDomain(inputLocation: InputLocation?): Boolean {
            return inputLocation?.source?.modelId?.let { isDroolsDomain(it) } ?: false
        }

        private fun isDroolsDomain(dependency: Dependency): Boolean {
            return isDroolsDomain(dependency.groupId)
        }

        private fun isDroolsDomain(valueToCheck: String): Boolean {
            return valueToCheck.startsWith("org.kie")
                    || valueToCheck.startsWith(
                "org.drools")
                    || valueToCheck.startsWith("org.jbpm")
                    || valueToCheck.startsWith("org.apache.kie")
        }
    }

}