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
 * Class representing a relationship between <code>Component</code>s.
 * <b>Note</b>: [currentComponent] must be different then [relatedComponent]
 * Two <code>Relationship</code>s are considered equals if the two components are the same, but <b>regardless</b> of their position (left/right); moreover, the relation type is ignored:
 * e.g. Relationship(a, b, IMPORT) would be equals to Relationship(b, a, CHILD)
 *
 * [currentComponent] the <b>left-side</b> of the current <code>Relationship</code>
 * [relatedComponent] the <b>right-side</b> of the current <code>Relationship</code>
 * [relation] the relationship <b>type</b> of the current <code>Relationship</code>
 */
class Relationship(val currentComponent: ComponentModel, val relatedComponent: ComponentModel, val relation: PrintMojo.RELATION) {

    init {
        require(currentComponent != relatedComponent) {
            "$currentComponent should be different then $relatedComponent"
        }
    }



//    /**
//     * This implementation ignores [relation] and compares only if the [currentComponent] and [relatedComponent] in the provided <code>Relationship</code> to check are
//     * present in the current instance, <bi>regardless</bi> of position; i.e. Relationship(a, b, IMPORT) would be equal to Relationship(b, a, CHILD)
//     */
//    override fun equals(other: Any?): Boolean {
//        if (this === other) return true
//        if (javaClass != other?.javaClass) return false
//
//        other as Relationship
//
//        if (currentComponent != other.currentComponent && currentComponent != other.relatedComponent) return false
//        if (relatedComponent != other.relatedComponent && relatedComponent != other.currentComponent) return false
//
//        return true
//    }
//
//    override fun hashCode(): Int {
//        var result = currentComponent.hashCode()
//        result = 31 * result + relatedComponent.hashCode()
//        return result
//    }

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