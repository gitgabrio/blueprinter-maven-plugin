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
 * Class used as model for single maven artifact
 */
data class ComponentModel(val gaIdentifier: String, val alias: String, val linkedFile: String) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ComponentModel

        if (gaIdentifier != other.gaIdentifier) return false
        if (alias != other.alias) return false
        if (linkedFile != other.linkedFile) return false

        return true
    }

    override fun hashCode(): Int {
        var result = gaIdentifier.hashCode()
        result = 31 * result + alias.hashCode()
        result = 31 * result + linkedFile.hashCode()
        return result
    }

    override fun toString(): String {
        return "ComponentModel(gaIdentifier='$gaIdentifier', alias='$alias', linkedFile='$linkedFile')"
    }


}