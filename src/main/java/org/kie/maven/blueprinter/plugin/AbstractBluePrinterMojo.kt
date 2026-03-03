/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.kie.maven.blueprinter.plugin

import org.apache.maven.execution.MavenSession
import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugin.logging.Log
import org.apache.maven.plugin.logging.SystemStreamLog
import org.apache.maven.plugins.annotations.Component
import org.apache.maven.plugins.annotations.Parameter
import org.apache.maven.project.MavenProject
import org.apache.maven.project.ProjectBuilder
import org.apache.maven.repository.RepositorySystem
import org.kie.maven.blueprinter.plugin.dataclass.CommonLoggingHolder
import org.kie.maven.blueprinter.plugin.dataclass.CommonObjectHolder
import org.kie.maven.blueprinter.plugin.dataclass.PrintObjectHolder
import org.kie.maven.blueprinter.plugin.utils.logMessage

abstract class AbstractBluePrinterMojo : AbstractMojo() {

    enum class LOG_LEVEL {
        INFO,
        WARN,
        ERROR,
        DEBUG
    }

    protected lateinit var commonObjectHolder: CommonObjectHolder

    protected lateinit var commonLoggingHolder: CommonLoggingHolder

    @Parameter(readonly = true, defaultValue = "\${project}")
    protected lateinit var project: MavenProject

    /**
     * Output directory
     */
    @Parameter(required = false, defaultValue = "blueprinter")
    protected var outputDirectory: String = "blueprinter"

    /**
     * Enable debug logging
     */
    @Parameter(required = false, defaultValue = "false")
    protected var debug: Boolean = false

    @Parameter(defaultValue = "\${session}", readonly = true, required = true)
    protected lateinit var session: MavenSession

    @Component
    protected lateinit var repositorySystem: RepositorySystem

    @Component
    protected lateinit var mavenProjectBuilder: ProjectBuilder

    /**
     * Progress indicator; evaluation completed when get to **empty** status
     */
    protected val projectToBuild = ArrayList<MavenProject>()

    /**
     * Maven collected projects of the main one
     */
    protected val targetProjectCollectedProjects = ArrayList<MavenProject>()

    protected var started = false


    private val blueprinterLogger: Log  = SystemStreamLog()

    protected fun retrieveCommonObjectHolder(): CommonObjectHolder {
        if (::commonObjectHolder.isInitialized.not()) {
            commonObjectHolder = CommonObjectHolder(
                repositorySystem,
                mavenProjectBuilder,
                session,
                project,
                targetProjectCollectedProjects,
                outputDirectory,
                retrieveLoggingHolder())
        }
        return commonObjectHolder
    }

    protected fun retrieveLoggingHolder(): CommonLoggingHolder {
        if (::commonLoggingHolder.isInitialized.not()) {
            commonLoggingHolder = CommonLoggingHolder(::getLog, debug)
        }
        return commonLoggingHolder
    }

    /**
     * Initialize the progress indicator and the collected projects containers
     */
    protected fun init(mavenProject: MavenProject) {
        logMessage("Init with ${mavenProject.name}", LOG_LEVEL.DEBUG, retrieveLoggingHolder())
        projectToBuild.clear()
        projectToBuild.addAll(mavenProject.collectedProjects)
        targetProjectCollectedProjects.clear()
        targetProjectCollectedProjects.addAll(mavenProject.collectedProjects)
        targetProjectCollectedProjects.add(mavenProject)
    }

    override fun getLog(): Log {
        return blueprinterLogger
    }
}