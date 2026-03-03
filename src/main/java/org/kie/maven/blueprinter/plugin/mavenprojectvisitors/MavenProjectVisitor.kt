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

import org.apache.maven.plugin.MojoFailureException
import org.apache.maven.project.MavenProject
import org.kie.maven.blueprinter.plugin.AbstractBluePrinterMojo
import org.kie.maven.blueprinter.plugin.PrintMojo
import org.kie.maven.blueprinter.plugin.dataclass.AnalyzerObjectHolder
import org.kie.maven.blueprinter.plugin.dataclass.CommonLoggingHolder
import org.kie.maven.blueprinter.plugin.dataclass.PrintObjectHolder
import org.kie.maven.blueprinter.plugin.dataclass.ComponentModel
import org.kie.maven.blueprinter.plugin.dataclass.Relationship
import org.kie.maven.blueprinter.plugin.mavenprojectvisitors.MavenProjectVisitor.Companion.toNavigate
import org.kie.maven.blueprinter.plugin.utils.dependencyToGAString
import org.kie.maven.blueprinter.plugin.utils.logMavenProject
import org.kie.maven.blueprinter.plugin.utils.logMessage
import org.kie.maven.blueprinter.plugin.utils.mavenProjectToComponentModel
import java.util.function.Consumer

/**
 * [MavenProject] specific [Visitor]
 */
class MavenProjectVisitor {


    companion object : Visitor {

        private lateinit var toNavigate: MavenProject


        /**
         * Functions used to read **MAVEN** projects: it populates [PrintObjectHolder.globalRelationshipSet] with all the [Relationship] retrieved with **recursive** navigation the given [MavenProject]
         *
         * [toNavigate] the [MavenProject] currently navigated
         * [printObjectHolder]
         *
         */
        @Throws(MojoFailureException::class)
        override fun visitForPrint(printObjectHolder: PrintObjectHolder) {
            logMessage("visitForPrint ${printObjectHolder}", AbstractBluePrinterMojo.LOG_LEVEL.DEBUG, printObjectHolder.commonLoggingHolder())
            toNavigate.let {
                val currentComponent = mavenProjectToComponentModel(it)
                it.parent?.let { parentProject ->
                    conditionallyNavigateParentProjectForPrint(parentProject, currentComponent, printObjectHolder)
                }
                it.originalModel?.dependencyManagement?.let { dependencyManagement ->
                    DependencyManagementVisitor.init(dependencyManagement, currentComponent)
                        .visitForPrint(printObjectHolder)
                }
                it.dependencyManagement?.let { dependencyManagement ->
                    DependencyManagementVisitor.init(dependencyManagement, currentComponent)
                        .visitForPrint(printObjectHolder)
                }
                it.collectedProjects
                    ?.filter { collectedProject -> collectedProject.parent == it }
                    ?.forEach(Consumer { collectedProject ->
                        addMavenProjectRelationship(
                            printObjectHolder.globalRelationshipSet,
                            collectedProject,
                            PrintMojo.RELATION.CHILD,
                            currentComponent,
                            printObjectHolder.commonLoggingHolder()
                        )
                    })
            }
        }

        override fun visitForAnalysis(analyzerObjectHolder: AnalyzerObjectHolder) {
            logMessage("visitForAnalysis ${analyzerObjectHolder}", AbstractBluePrinterMojo.LOG_LEVEL.DEBUG, analyzerObjectHolder.commonLoggingHolder())
            toNavigate.let {
                val currentComponent = mavenProjectToComponentModel(it)
                logMessage("analyzing $it - $currentComponent", AbstractBluePrinterMojo.LOG_LEVEL.DEBUG, analyzerObjectHolder.commonLoggingHolder())
                it.originalModel?.dependencies?.let { dependencies ->
                    DependencyManagementVisitor.init(dependencies, currentComponent)
                        .visitForAnalysis(analyzerObjectHolder)
                }
                it.dependencies?.let { dependencies ->
                    DependencyManagementVisitor.init(dependencies, currentComponent)
                        .visitForAnalysis(analyzerObjectHolder)
                }
                it.dependencyManagement?.let { dependencyManagement ->
                    DependencyManagementVisitor.init(dependencyManagement, currentComponent)
                        .visitForAnalysis(analyzerObjectHolder)
                }

                it.parent?.let { parentProject ->
                    conditionallyNavigateParentProjectForAnalysis(parentProject, currentComponent, analyzerObjectHolder)
                }
                it.collectedProjects
                    ?.filter { collectedProject -> collectedProject.parent == it }
                    ?.forEach(Consumer { collectedProject ->
                        logMessage(
                            "visitForAnalysis: here we should add entries in Map<String, Set<VersionsTuple>> for ${collectedProject.name} and then navigate it",
                            AbstractBluePrinterMojo.LOG_LEVEL.DEBUG,
                            analyzerObjectHolder.commonLoggingHolder()
                        )
                        conditionallyNavigateParentProjectForAnalysis(collectedProject, currentComponent, analyzerObjectHolder)
                    })
            }
        }

        fun init(toNavigate: MavenProject): Companion {
            this.toNavigate = toNavigate
            return this
        }

        /**
         * Navigate a given [MavenProject] only if is not contained in the collected projects of the principal one, adding a [PrintMojo.RELATION.PARENT] between it and the current [ComponentModel].
         */
        private fun conditionallyNavigateParentProjectForPrint(
            parentProject: MavenProject,
            currentComponent: ComponentModel,
            printObjectHolder: PrintObjectHolder
        ) {
            if (!printObjectHolder.targetProjectCollectedProjects().contains(parentProject)) {
                addMavenProjectRelationship(
                    printObjectHolder.globalRelationshipSet,
                    parentProject,
                    PrintMojo.RELATION.PARENT,
                    currentComponent,
                    printObjectHolder.commonLoggingHolder()
                )
                init(parentProject).visitForPrint(printObjectHolder)
            }
        }

        /**
         * Navigate a given [MavenProject] only if is not contained in the collected projects of the principal one, adding a [PrintMojo.RELATION.PARENT] between it and the current [ComponentModel].
         */
        private fun conditionallyNavigateParentProjectForAnalysis(
            parentProject: MavenProject,
            currentComponent: ComponentModel,
            analyzerObjectHolder: AnalyzerObjectHolder
        ) {
            logMessage(
                "analyzerObjectHolder.targetProjectCollectedProjects(): ${analyzerObjectHolder.targetProjectCollectedProjects()}",
                AbstractBluePrinterMojo.LOG_LEVEL.DEBUG,
                analyzerObjectHolder.commonLoggingHolder()
            )
            if (!analyzerObjectHolder.targetProjectCollectedProjects().contains(parentProject)) {
                logMessage(
                    "conditionallyNavigateParentProjectForAnalysis: here we should add entries in Map<String, Set<VersionsTuple>> for $parentProject and then navigate it",
                    AbstractBluePrinterMojo.LOG_LEVEL.DEBUG,
                    analyzerObjectHolder.commonLoggingHolder()
                )
                init(parentProject).visitForAnalysis(analyzerObjectHolder)
                analyzerObjectHolder.targetProjectCollectedProjects().add(parentProject)
            }
        }

        private fun addMavenProjectRelationship(
            relationshipSet: HashSet<Relationship>,
            mavenProject: MavenProject,
            relation: PrintMojo.RELATION,
            currentComponent: ComponentModel,
            commonLoggingHolder: CommonLoggingHolder
        ) {
            logMavenProject(mavenProject, relation, commonLoggingHolder)
            val relatedComponent = mavenProjectToComponentModel(mavenProject)
            relationshipSet.add(Relationship(currentComponent, relatedComponent, relation))
        }
    }

}
