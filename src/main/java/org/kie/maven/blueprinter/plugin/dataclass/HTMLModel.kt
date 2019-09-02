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

/**
 * Class used as model for final html
 */
data class HTMLModel(val representedComponent: ComponentModel, val relationships: Set<Relationship>, val outputDirectory: String) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as HTMLModel

        if (representedComponent != other.representedComponent) return false
        if (relationships != other.relationships) return false
        if (outputDirectory != other.outputDirectory) return false

        return true
    }

    override fun hashCode(): Int {
        var result = representedComponent.hashCode()
        result = 31 * result + relationships.hashCode()
        result = 31 * result + outputDirectory.hashCode()
        return result
    }

    override fun toString(): String {
        return "HTMLModel(representedComponent=$representedComponent, relationships=$relationships, outputDirectory='$outputDirectory')"
    }


}