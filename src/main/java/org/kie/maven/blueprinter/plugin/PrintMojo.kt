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

import org.apache.maven.execution.MavenSession
import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugin.MojoExecutionException
import org.apache.maven.plugin.MojoFailureException
import org.apache.maven.plugin.logging.Log
import org.apache.maven.plugins.annotations.*
import org.apache.maven.project.MavenProject
import org.apache.maven.project.ProjectBuilder
import org.apache.maven.repository.RepositorySystem


/**
 * Check and print out the overall MAVEN relationship
 */
@Mojo(name = "print", defaultPhase = LifecyclePhase.VALIDATE, threadSafe = true, instantiationStrategy = InstantiationStrategy.SINGLETON)
open class PrintMojo : AbstractMojo() {


    @Parameter(readonly = true, defaultValue = "\${project}")
    private lateinit var project: MavenProject

    /**
     * Generated scheme file name
     */
    @Parameter(required = false, defaultValue = "scheme")
    private var fileName: String = "scheme"

    @Parameter(defaultValue = "\${session}", readonly = true, required = true)
    private lateinit var session: MavenSession

    @Component
    private lateinit var repositorySystem: RepositorySystem

    @Component
    private lateinit var mavenProjectBuilder: ProjectBuilder

    enum class RELATION {
        PARENT,
        IMPORT,
        CHILD
    }

    private val targetProjectRelationshipSet = HashSet<Relationship>()
    private val projectToBuild = ArrayList<MavenProject>()
    private val targetProjectCollectedProjects = ArrayList<MavenProject>()
    private var started = false


    @Throws(MojoExecutionException::class, MojoFailureException::class)
    override fun execute() {
        log.debug("Executing PrintMojo on instance $this")
        project.let {
            if (!started) {
                init(it)
                initFile(fileName, log)
                started = true
            }
            navigateProject(targetProjectRelationshipSet, it, fileName, CommonObjectHolder(repositorySystem, mavenProjectBuilder, session, project, targetProjectCollectedProjects, log))
            projectToBuild.remove(it)
            if (projectToBuild.isEmpty()) {
                writeRelationships(targetProjectRelationshipSet, fileName, log)
                done(fileName, log)
                started = false
            }
        }
    }

    private fun init(mavenProject: MavenProject) {
        log.debug("Init with ${mavenProject.name}")
        projectToBuild.clear()
        projectToBuild.addAll(mavenProject.collectedProjects)
        targetProjectCollectedProjects.clear()
        targetProjectCollectedProjects.addAll(mavenProject.collectedProjects)
        targetProjectCollectedProjects.add(mavenProject)
//        initFile(fileName, log)
    }


    class CommonObjectHolder(val repositorySystem: RepositorySystem, val mavenProjectBuilder: ProjectBuilder, val session: MavenSession, val targetProject: MavenProject, val targetProjectCollectedProjects: ArrayList<MavenProject>, val log: Log)

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
