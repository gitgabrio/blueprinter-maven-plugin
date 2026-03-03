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

import org.apache.maven.plugin.MojoExecutionException
import org.apache.maven.plugin.MojoFailureException
import org.apache.maven.plugins.annotations.InstantiationStrategy
import org.apache.maven.plugins.annotations.LifecyclePhase
import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.plugins.annotations.Parameter
import org.apache.maven.project.MavenProject
import org.kie.maven.blueprinter.plugin.dataclass.PrintObjectHolder
import org.kie.maven.blueprinter.plugin.dataclass.Relationship
import org.kie.maven.blueprinter.plugin.mavenprojectvisitors.MavenProjectVisitor
import org.kie.maven.blueprinter.plugin.relationshipwriters.pumlwriter.PUMLWriter
import org.kie.maven.blueprinter.plugin.relationshipwriters.pumlwriter.createHTMLFiles
import org.kie.maven.blueprinter.plugin.utils.logMessage
import java.io.File


/**
 * Check and print out the overall MAVEN relationship
 */
@Mojo(
    name = "print",
    defaultPhase = LifecyclePhase.VALIDATE,
    threadSafe = true,
    instantiationStrategy = InstantiationStrategy.SINGLETON
)
open class PrintMojo : AbstractBluePrinterMojo() {

    /**
     * Generated scheme file name
     */
    @Parameter(required = false, defaultValue = "puml")
    private var outputFormat: String = "puml"

    enum class RELATION {
        PARENT,
        IMPORT,
        CHILD
    }

    private val globalProjectRelationshipSet = HashSet<Relationship>()

    @Throws(MojoExecutionException::class, MojoFailureException::class)
    override fun execute() {
        logMessage("Executing PrintMojo on instance $this", LOG_LEVEL.DEBUG, retrieveLoggingHolder())
        project.let {
            if (!started) {
                init(it)
                started = true
            }
            MavenProjectVisitor.init(it).visitForPrint(
                PrintObjectHolder(
                    globalProjectRelationshipSet,
                    retrieveCommonObjectHolder()
                )
            )
            projectToBuild.remove(it)
            if (projectToBuild.isEmpty()) {
                started = false
                when (outputFormat) {
                    "puml" -> PUMLWriter.writeRelationships(globalProjectRelationshipSet, outputDirectory, retrieveLoggingHolder())
                    else -> throw MojoExecutionException("Unexpected output format $outputFormat")
                }
            }
        }
    }

}
