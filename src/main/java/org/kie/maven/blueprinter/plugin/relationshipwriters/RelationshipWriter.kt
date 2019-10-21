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
package org.kie.maven.blueprinter.plugin.relationshipwriters

import org.apache.maven.plugin.logging.Log
import org.kie.maven.blueprinter.plugin.dataclass.Relationship

/**
 * Interface to be implemented by all concrete [RelationshipWriter]s
 */
interface RelationshipWriter {

    /**
     * Write all the given [Relationship]s to specific [outputDirectory]
     *
     * [relationshipSet] the [Relationship]s to write
     * [outputDirectory]
     * [log]
     */
    fun writeRelationships(relationshipSet: HashSet<Relationship>, outputDirectory: String, log: Log)
}