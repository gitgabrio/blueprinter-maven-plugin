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

import org.apache.maven.artifact.Artifact
import org.apache.maven.model.Dependency
import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugin.MojoExecutionException
import org.apache.maven.plugin.MojoFailureException
import org.apache.maven.plugins.annotations.InstantiationStrategy
import org.apache.maven.plugins.annotations.LifecyclePhase
import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.plugins.annotations.Parameter
import org.apache.maven.project.MavenProject
import java.io.File
import java.util.function.Consumer

/**
 * Check and print out the **GWT** inheritance tree.
 */
@Mojo(name = "print", defaultPhase = LifecyclePhase.VALIDATE, threadSafe = true, instantiationStrategy = InstantiationStrategy.SINGLETON)
open class PrintMojo : AbstractMojo() {


    @Parameter(readonly = true, defaultValue = "\${project}")
    private val project: MavenProject? = null

    /**
     * Additional resource directories.
     */
    @Parameter(required = false, defaultValue = "scheme.puml")
    private var fileName: String = "scheme.puml"


    private fun mavenProjectString(mavenProject: MavenProject): String = "${mavenProject.groupId}:${mavenProject.artifactId}"
    private fun dependencyString(dependency: Dependency): String = "${dependency.groupId}:${dependency.artifactId}"

    private fun logMavenProject(mavenProject: MavenProject, relation: RELATION) = log.debug("$relation: ${mavenProjectString(mavenProject)}:${mavenProject.version}")
    private fun logDependency(dependency: Dependency, relation: RELATION) = log.debug("$relation: ${dependencyString(dependency)}:${dependency.version}")

    private fun addComponentToFile(component: String, fileName: String) = File(fileName).appendText("\r\n[$component]")

    enum class RELATION {
        PARENT,
        IMPORT,
        CHILD
    }

    private val relationshipSet = HashSet<Relationship>()
    private val projectToBuild = ArrayList<MavenProject>()
    private val collectedProjects = ArrayList<MavenProject>()
    private var started = false


    @Throws(MojoExecutionException::class, MojoFailureException::class)
    override fun execute() {
        log.debug("Executing PrintMojo on instance $this")
        project?.let {
            if (!started) {
                init(it)
            }
            val currentComponent = mavenProjectString(it)
            addComponentToFile(currentComponent, fileName)
            it.parent?.let { innerIt ->
                if (!collectedProjects.contains(innerIt)) {
                    addMavenProjectRelationship(innerIt, RELATION.PARENT, currentComponent)
                }
            }
            it.originalModel?.let { innerIt ->
                innerIt.dependencyManagement?.dependencies?.filter { subInnerIt -> subInnerIt.scope == "import" }?.forEach(Consumer { subInnerIt ->
                    addDependencyRelationship(subInnerIt, RELATION.IMPORT, currentComponent)
                })
            }
            it.collectedProjects
                    .filter { innerIt -> innerIt.parent == it }
                    .forEach(Consumer {
                        innerIt -> addMavenProjectRelationship(innerIt, RELATION.CHILD, currentComponent)

                    })
            projectToBuild.remove(it)
            if (projectToBuild.isEmpty()) {
                writeRelationships()
                done()
            }
        }
    }

    private fun init(mavenProject: MavenProject) {
        log.debug("Init with ${mavenProject.name}")
        projectToBuild.clear()
        projectToBuild.addAll(mavenProject.collectedProjects)
        collectedProjects.clear()
        collectedProjects.addAll(mavenProject.collectedProjects)
        collectedProjects.add(mavenProject)
        File(fileName).writeText("@startuml")
        started = true
    }

    private fun writeRelationships() {
        log.debug("Write relationships ...")
        relationshipSet
                .map { it.currentComponent }
                .toHashSet()
                .forEach {
                    log.debug("addComponentToFile $it")
                    addComponentToFile(it, fileName)
                }
        relationshipSet.forEach {
            log.debug("writeRelationship $it")
            writeRelationship(it)
        }
    }

    private fun done() {
        log.debug("... done")
        File(fileName).appendText("\r\n@enduml")
        started = false
    }


    private fun addMavenProjectRelationship(mavenProject: MavenProject, relation: RELATION, currentComponent: String) {
        logMavenProject(mavenProject, relation)
        val relatedComponent = mavenProjectString(mavenProject)
        relationshipSet.add(Relationship(currentComponent, relatedComponent, relation))
    }

    private fun addDependencyRelationship(dependency: Dependency, relation: RELATION, currentComponent: String) {
        logDependency(dependency, relation)
        val relatedComponent = dependencyString(dependency)
        relationshipSet.add(Relationship(currentComponent, relatedComponent, relation))
    }

    private fun writeRelationship(relationship: Relationship) {
        when (relationship.relation) {
            RELATION.PARENT -> {
                File(fileName).appendText("\r\n[${relationship.relatedComponent}] <-- [${relationship.currentComponent}] : extend")
            }
            RELATION.CHILD -> {
                File(fileName).appendText("\r\n[${relationship.currentComponent}] <-- [${relationship.relatedComponent}] : extend")
            }
            RELATION.IMPORT -> {
                File(fileName).appendText("\r\n[${relationship.currentComponent}] ..> [${relationship.relatedComponent}] : import")
            }
        }
    }

    class Relationship(val currentComponent: String, val relatedComponent: String, val relation: RELATION) {

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Relationship

            if (currentComponent != other.currentComponent) return false
            if (relatedComponent != other.relatedComponent) return false
            if (relation != other.relation) return false

            return true
        }

        override fun hashCode(): Int {
            var result = currentComponent.hashCode()
            result = 31 * result + relatedComponent.hashCode()
            result = 31 * result + relation.hashCode()
            return result
        }

        override fun toString(): String {
            return "Relationship(currentComponent='$currentComponent', relatedComponent='$relatedComponent', relation=$relation)"
        }


    }
}
