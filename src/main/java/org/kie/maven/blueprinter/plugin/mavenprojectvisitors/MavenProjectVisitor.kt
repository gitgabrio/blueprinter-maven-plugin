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
import org.apache.maven.plugin.logging.Log
import org.apache.maven.project.MavenProject
import org.kie.maven.blueprinter.plugin.PrintMojo
import org.kie.maven.blueprinter.plugin.dataclass.CommonObjectHolder
import org.kie.maven.blueprinter.plugin.dataclass.ComponentModel
import org.kie.maven.blueprinter.plugin.dataclass.Relationship
import org.kie.maven.blueprinter.plugin.utils.logMavenProject
import org.kie.maven.blueprinter.plugin.utils.mavenProjectToComponentModel
import java.util.function.Consumer

/**
 * [MavenProject] specific [Visitor]
 */
class MavenProjectVisitor {


    companion object : Visitor {

        private lateinit var toNavigate: MavenProject


        /**
         * Functions used to read **MAVEN** projects: it populates [CommonObjectHolder.globalRelationshipSet] with all the [Relationship] retrieved with **recursive** navigation the given [MavenProject]
         *
         * [toNavigate] the [MavenProject] currently navigated
         * [commonObjectHolder]
         *
         */
        @Throws(MojoFailureException::class)
        override fun visit(commonObjectHolder: CommonObjectHolder) {
            toNavigate.let {
                val currentComponent = mavenProjectToComponentModel(it)
                it.parent?.let { parentProject ->
                    conditionallyNavigateParentProject(parentProject, currentComponent, commonObjectHolder)
                }
                it.originalModel?.dependencyManagement?.let { dependencyManagement ->
                    DependencyManagementVisitor.init(dependencyManagement, currentComponent).visit(commonObjectHolder)
                }
                it.dependencyManagement?.let { dependencyManagement ->
                    DependencyManagementVisitor.init(dependencyManagement, currentComponent).visit(commonObjectHolder)
                }
                it.collectedProjects
                        ?.filter { collectedProject -> collectedProject.parent == it }
                        ?.forEach(Consumer { collectedProject ->
                            addMavenProjectRelationship(commonObjectHolder.globalRelationshipSet, collectedProject, PrintMojo.RELATION.CHILD, currentComponent, commonObjectHolder.log)
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
        private fun conditionallyNavigateParentProject(parentProject: MavenProject, currentComponent: ComponentModel, commonObjectHolder: CommonObjectHolder) {
            if (!commonObjectHolder.targetProjectCollectedProjects.contains(parentProject)) {
                addMavenProjectRelationship(commonObjectHolder.globalRelationshipSet, parentProject, PrintMojo.RELATION.PARENT, currentComponent, commonObjectHolder.log)
                init(parentProject).visit(commonObjectHolder)
            }
        }

        private fun addMavenProjectRelationship(relationshipSet: HashSet<Relationship>, mavenProject: MavenProject, relation: PrintMojo.RELATION, currentComponent: ComponentModel, log: Log) {
            logMavenProject(mavenProject, relation, log)
            val relatedComponent = mavenProjectToComponentModel(mavenProject)
            relationshipSet.add(Relationship(currentComponent, relatedComponent, relation))
        }
    }

}
