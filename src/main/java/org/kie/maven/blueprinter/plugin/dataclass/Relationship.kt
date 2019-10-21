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

import org.kie.maven.blueprinter.plugin.PrintMojo

/**
 * Class representing a relationship between [ComponentModel]s.
 * **Note**: [currentComponent] must be different then [relatedComponent]
 * Two [Relationship]s are considered equals if the two components are the same, but **regardless** of their position (left/right); moreover, the relation type is ignored:
 * e.g. Relationship(a, b, IMPORT) would be equals to Relationship(b, a, CHILD)
 *
 * [currentComponent] the **left-side** of the current [Relationship]
 * [relatedComponent] the **right-side** of the current [Relationship]
 * [relation] the relationship **type** of the current [Relationship]
 */
class Relationship(val currentComponent: ComponentModel, val relatedComponent: ComponentModel, val relation: PrintMojo.RELATION) {

    init {
        require(currentComponent != relatedComponent) {
            "$currentComponent should be different then $relatedComponent"
        }
    }
    
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