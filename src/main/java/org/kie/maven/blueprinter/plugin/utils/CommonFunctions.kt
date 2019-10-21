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
package org.kie.maven.blueprinter.plugin.utils

import org.apache.maven.model.Dependency
import org.apache.maven.project.MavenProject
import org.kie.maven.blueprinter.plugin.dataclass.ComponentModel

/**
 * Functions used to transform specific object to common sting representation
 */

/**
 * Returns a String representing the **groupId** and **artifactId** of the given [MavenProject]
 * e.g. org.test.project:foo-bar
 *
 * [mavenProject]
 */
fun mavenProjectToGAString(mavenProject: MavenProject): String = "${mavenProject.groupId}:${mavenProject.artifactId}"

/**
 * Returns a String containing only the **capitalized** substring of **groupId** and **artifactId** (split by **".", "-", "_"**) of the given [MavenProject]
 * e.g. org.test.project:foo-bar -> OrgTestProjectFooBar
 *
 * [mavenProject]
 */
fun mavenProjectToAlias(mavenProject: MavenProject): String = gaToAlias(mavenProject.groupId, mavenProject.artifactId)

/**
 * Returns a String representing the  **groupId** and **artifactId** of the given [MavenProject] with "." and ":" replaced by "_"
 * e.g. org.test.project:foo-bar -> org_test_project_foo-bar
 *
 * [mavenProject]
 */
fun mavenProjectToFileName(mavenProject: MavenProject): String = gaToFileName(mavenProject.groupId, mavenProject.artifactId)

/**
 * Returns a [ComponentModel] representation of the given [MavenProject]
 *
 * [mavenProject]
 */
fun mavenProjectToComponentModel(mavenProject: MavenProject): ComponentModel {
    return ComponentModel(mavenProjectToGAString(mavenProject), mavenProjectToAlias(mavenProject), mavenProjectToFileName(mavenProject))
}

/**
 * Returns a String representing the  **groupId** and **artifactId** of the given [Dependency]
 * e.g. org.test.project:foo-bar
 *
 * [dependency]
 */
fun dependencyToGAString(dependency: Dependency): String = "${dependency.groupId}:${dependency.artifactId}"

/**
 * Returns a String containing only the **capitalized** substring of  **groupId** and **artifactId** (split by **".", "-", "_"**) of the given [Dependency]
 * e.g. org.test.project:foo-bar -> OrgTestProjectFooBar
 *
 * [dependency]
 */
fun dependencyToAlias(dependency: Dependency): String = gaToAlias(dependency.groupId, dependency.artifactId)


/**
 * Returns a String representing the  **groupId** and **artifactId** of the given [Dependency] with "." and ":" replaced by "_"
 * e.g. org.test.project:foo-bar -> org_test_project_foo-bar
 *
 * [dependency]
 */
fun dependencyToFileName(dependency: Dependency): String = gaToFileName(dependency.groupId, dependency.artifactId)

/**
 * Returns a [ComponentModel] representation of the given [Dependency]
 *
 * [dependency]
 */
fun dependencyToComponentModel(dependency: Dependency): ComponentModel {
    return ComponentModel(dependencyToGAString(dependency), dependencyToAlias(dependency), dependencyToFileName(dependency))
}

/**
 *  Returns a String representing the given [groupId] and [artifactId] with "." and ":" replaced by "_"
 * e.g. org.test.project:foo-bar -> org_test_project_foo-bar
 *
 * [groupId]
 * [artifactId]
 */
private fun gaToAlias(groupId: String, artifactId: String): String {
    val groupParts = groupId.split(".")
    val artifactParts = artifactId.split("-", "_")
    val stringBuilder: StringBuilder = java.lang.StringBuilder()
    groupParts.forEach { stringBuilder.append(it.capitalize()) }
    artifactParts.forEach { stringBuilder.append(it.capitalize()) }
    return stringBuilder.toString()
}

/**
 *  Returns a String representing the given [groupId] and [artifactId] with "." and ":" replaced by "_"
 * e.g. org.test.project:foo-bar -> org_test_project_foo-bar
 *
 * [groupId]
 * [artifactId]
 */
private fun gaToFileName(groupId: String, artifactId: String): String = "${groupId}_$artifactId".replace(".", "_")