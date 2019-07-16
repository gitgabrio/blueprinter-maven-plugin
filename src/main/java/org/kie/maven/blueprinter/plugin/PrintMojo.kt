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
import org.apache.maven.plugins.annotations.LifecyclePhase
import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.plugins.annotations.Parameter
import org.apache.maven.project.MavenProject
import java.io.File
import java.util.function.Consumer

/**
 * Check and print out the **GWT** inheritance tree.
 */
@Mojo(name = "print", defaultPhase = LifecyclePhase.VALIDATE, threadSafe = true)
open class PrintMojo : AbstractMojo() {


    @Parameter(readonly = true, defaultValue = "\${project}")
    private val project: MavenProject? = null

    fun mavenProjectString(mavenProject: MavenProject): String = "${mavenProject.groupId}:${mavenProject.artifactId}"
    fun artifactString(artifact: Artifact): String = "${artifact.groupId}:${artifact.artifactId}"
    fun dependencyString(dependency: Dependency): String = "${dependency.groupId}:${dependency.artifactId}"

    fun logMavenProject(mavenProject: MavenProject, relation: RELATION) = log.info("$relation: ${mavenProjectString(mavenProject)}:${mavenProject.version}")
    fun logArtifact(artifact: Artifact, relation: RELATION) = log.info("$relation: ${artifactString(artifact)}:${artifact.version}")
    fun logDependency(dependency: Dependency, relation: RELATION) = log.info("$relation: ${dependencyString(dependency)}:${dependency.version}")

    fun addComponentToFile(component: String, fileName: String) = File(fileName).appendText("\r\n[$component]")

    enum class RELATION {
        MODULE,
        PARENT,
        IMPORT,
        CHILD
    }

    @Throws(MojoExecutionException::class, MojoFailureException::class)
    override fun execute() {
        project?.let {
            val fileName = it.name.replace(".", "_").replace(" ", "").replace("-", "") + ".puml"
            File(fileName).writeText("@startuml")
            val currentComponent = mavenProjectString(it)
            addComponentToFile(currentComponent, fileName)
            printMavenProject(project, RELATION.MODULE, fileName, currentComponent)
            it.parent?.let {
                printMavenProject(project.parent, RELATION.PARENT, fileName, currentComponent)
            }
            it.originalModel?.let {
                innerIt -> innerIt.dependencyManagement?.dependencies?.
                    filter { subInnerIt -> subInnerIt.scope == "import" }?.
                    forEach(Consumer { subInnerIt ->
                        printDependency(subInnerIt, RELATION.IMPORT, fileName, currentComponent)
                    })
            }
            it.collectedProjects
                    .filter { innerIt -> innerIt.parent == it }
                    .forEach(Consumer { innerIt -> printMavenProject(innerIt, RELATION.CHILD, fileName, currentComponent) })
            File(fileName).appendText("\r\n@enduml")
        }
    }


    private fun printMavenProject(mavenProject: MavenProject, relation: RELATION, fileName: String, currentComponent: String) {
        logMavenProject(mavenProject, relation)
        val relatedComponent = mavenProjectString(mavenProject)
        addComponentToFile(relatedComponent, fileName)
        when (relation) {
            RELATION.PARENT -> {
                File(fileName).appendText("\r\n[$relatedComponent] <.. [$currentComponent] : extends")
            }
            RELATION.CHILD -> {
                File(fileName).appendText("\r\n[$currentComponent] <.. [$relatedComponent] : extends")
            }
            else -> {
            }
        }
    }

    private fun printArtifact(artifact: Artifact, relation: RELATION, fileName: String, currentComponent: String) {
        logArtifact(artifact, relation)
        val relatedComponent = artifactString(artifact)
        addComponentToFile(relatedComponent, fileName)
        when (relation) {
            RELATION.IMPORT -> {
                File(fileName).appendText("\r\n[$currentComponent] ..> [$relatedComponent] : imports")
            }
            else -> {
            }
        }
    }

    private fun printDependency(dependency: Dependency, relation: RELATION, fileName: String, currentComponent: String) {
        logDependency(dependency, relation)
        val relatedComponent = dependencyString(dependency)
        addComponentToFile(relatedComponent, fileName)
        when (relation) {
            RELATION.IMPORT -> {
                File(fileName).appendText("\r\n[$currentComponent] ..> [$relatedComponent] : imports")
            }
            else -> {
            }
        }
    }

}
