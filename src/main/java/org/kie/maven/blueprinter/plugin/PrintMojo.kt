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
import org.apache.maven.plugins.annotations.*
import org.apache.maven.project.MavenProject
import org.apache.maven.project.ProjectBuilder
import org.apache.maven.repository.RepositorySystem
import org.kie.maven.blueprinter.plugin.dataclass.CommonObjectHolder
import org.kie.maven.blueprinter.plugin.dataclass.Relationship
import org.kie.maven.blueprinter.plugin.mavenprojectvisitors.MavenProjectVisitor
import org.kie.maven.blueprinter.plugin.relationshipwriters.pumlwriter.PUMLWriter


/**
 * Check and print out the overall MAVEN relationship
 */
@Mojo(name = "print", defaultPhase = LifecyclePhase.VALIDATE, threadSafe = true, instantiationStrategy = InstantiationStrategy.SINGLETON)
open class PrintMojo : AbstractMojo() {

    @Parameter(readonly = true, defaultValue = "\${project}")
    private lateinit var project: MavenProject

//    /**
//     * Generated scheme file name
//     */
//    @Parameter(required = false, defaultValue = "scheme")
//    private var fileName: String = "scheme"

    /**
     * Generated scheme file name
     */
    @Parameter(required = false, defaultValue = "puml")
    private var outputFormat: String = "puml"

    /**
     * Output directory
     */
    @Parameter(required = false, defaultValue = "blueprinter")
    private var outputDirectory: String = "blueprinter"

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

    private val globalProjectRelationshipSet = HashSet<Relationship>()
    /**
     * Progress indicator; evaluation completed when get to **empty** status
     */
    private val projectToBuild = ArrayList<MavenProject>()
    /**
     * Maven collected projects of the main one
     */
    private val targetProjectCollectedProjects = ArrayList<MavenProject>()

    private var started = false


    @Throws(MojoExecutionException::class, MojoFailureException::class)
    override fun execute() {
        log.debug("Executing PrintMojo on instance $this")
        project.let {
            if (!started) {
                init(it)
                started = true
            }
            MavenProjectVisitor.init(it).visit(CommonObjectHolder(repositorySystem, mavenProjectBuilder, session, project, targetProjectCollectedProjects, globalProjectRelationshipSet, outputDirectory, log))
            projectToBuild.remove(it)
            if (projectToBuild.isEmpty()) {
                started = false
                when(outputFormat)  {
                    "puml" -> PUMLWriter.writeRelationships(globalProjectRelationshipSet, outputDirectory, log)
                    else -> throw MojoExecutionException("Unexpected output format $outputFormat")
                }
            }
        }
    }

    /**
     * Initialize the progress indicator and the collected projects containers
     */
    private fun init(mavenProject: MavenProject) {
        log.debug("Init with ${mavenProject.name}")
        projectToBuild.clear()
        projectToBuild.addAll(mavenProject.collectedProjects)
        targetProjectCollectedProjects.clear()
        targetProjectCollectedProjects.addAll(mavenProject.collectedProjects)
        targetProjectCollectedProjects.add(mavenProject)
    }

}
