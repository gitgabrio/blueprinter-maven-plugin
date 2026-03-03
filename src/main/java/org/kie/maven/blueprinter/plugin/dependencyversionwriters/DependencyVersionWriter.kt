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
package org.kie.maven.blueprinter.plugin.dependencyversionwriters

import org.kie.maven.blueprinter.plugin.dataclass.CommonLoggingHolder
import org.kie.maven.blueprinter.plugin.dataclass.VersionsTuple

/**
 * Interface to be implemented by all concrete [DependencyVersionWriter]s
 */
interface DependencyVersionWriter {

    /**
     * Write all the entries in the given [Map<String, Set<VersionsTuple>>] to XLS in a specific  [outputDirectory]
     *
     * [dependencyVersions] the [Map<String, Set<VersionsTuple>>]s to write
     * [outputDirectory] the destination of created XLS file
     * [commonLoggingHolder] the [CommonLoggingHolder] used for logging
     */
    fun writeDependencyVersions(
        dependencyVersionsMap: Map<String, Set<VersionsTuple>>,
        outputDirectory: String,
        commonLoggingHolder: CommonLoggingHolder
    )
}