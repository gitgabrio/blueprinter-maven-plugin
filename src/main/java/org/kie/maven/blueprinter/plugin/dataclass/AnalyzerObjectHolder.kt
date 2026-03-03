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
package org.kie.maven.blueprinter.plugin.dataclass

import org.apache.maven.project.MavenProject

/**
 * Utility class to store objects used throughout execution for [org.kie.maven.blueprinter.plugin.AnalyzerMojo] purpose
 * [globalDependencyVersionsMap] all the mapped [VersionsTuple]s identified by recursively read during overall execution
 * [commonObjectHolder] the [CommonObjectHolder]
 *
 */
data class AnalyzerObjectHolder(
    val globalDependencyVersionsMap: MutableMap<String, MutableSet<VersionsTuple>>,
    val commonObjectHolder: CommonObjectHolder
) {
    fun commonLoggingHolder(): CommonLoggingHolder {
        return commonObjectHolder.commonLoggingHolder
    }

    fun targetProjectCollectedProjects(): ArrayList<MavenProject> {
        return commonObjectHolder.targetProjectCollectedProjects
    }
}