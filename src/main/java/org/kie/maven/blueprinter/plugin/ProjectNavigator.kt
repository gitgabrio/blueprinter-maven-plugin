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

import org.apache.maven.model.Dependency
import org.apache.maven.model.DependencyManagement
import org.apache.maven.plugin.MojoFailureException
import org.apache.maven.plugin.logging.Log
import org.apache.maven.project.MavenProject
import org.apache.maven.project.ProjectBuildingException
import org.kie.maven.blueprinter.plugin.dataclass.CommonObjectHolder
import org.kie.maven.blueprinter.plugin.dataclass.ComponentModel
import org.kie.maven.blueprinter.plugin.dataclass.Relationship
import java.util.function.Consumer

/**
 * Functions used to read **MAVEN** projects: it populates [CommonObjectHolder.globalRelationshipSet] with all the <code>Relationship</code> retrieved with <b>recursive</b> navigation the given <code>MavenProject</code>
 *
 * [toNavigate] the <code>MavenProject</code> currently navigated
 * [commonObjectHolder]
 *
 */
fun navigateProject(toNavigate: MavenProject, commonObjectHolder: CommonObjectHolder) {
    toNavigate.let {
        val currentComponent = mavenProjectToComponentModel(it)
        it.parent?.let { parentProject ->
            conditionallyNavigateParentProject(parentProject, currentComponent, commonObjectHolder)
        }
        it.originalModel?.dependencyManagement?.let { dependencyManagement ->
            navigateDependencyManagement(dependencyManagement, currentComponent, commonObjectHolder)
        }
        it.dependencyManagement?.let { dependencyManagement ->
            navigateDependencyManagement(dependencyManagement, currentComponent, commonObjectHolder)
        }
        it.collectedProjects
                ?.filter { collectedProject -> collectedProject.parent == it }
                ?.forEach(Consumer { collectedProject ->
                    addMavenProjectRelationship(commonObjectHolder.globalRelationshipSet, collectedProject, PrintMojo.RELATION.CHILD, currentComponent, commonObjectHolder.log)
                })
    }
}

/**
 * Navigate a given <code>MavenProject</code> only if is not contained in the collected projects of the principal one, adding a <code>PrintMojo.RELATION.PARENT</code> between it and the current <code>ComponentModel</code>.
 */
private fun conditionallyNavigateParentProject(parentProject: MavenProject, currentComponent: ComponentModel, commonObjectHolder: CommonObjectHolder) {
    if (!commonObjectHolder.targetProjectCollectedProjects.contains(parentProject)) {
        addMavenProjectRelationship(commonObjectHolder.globalRelationshipSet, parentProject, PrintMojo.RELATION.PARENT, currentComponent, commonObjectHolder.log)
        navigateProject(parentProject, commonObjectHolder)
    }
}

private fun navigateDependencyManagement(dependencyManagement: DependencyManagement, currentComponent: ComponentModel, commonObjectHolder: CommonObjectHolder) {
    dependencyManagement.dependencies?.filter { dependency -> dependency.scope == "import" }?.forEach(Consumer { dependency ->
        addDependencyRelationship(commonObjectHolder.globalRelationshipSet, dependency, PrintMojo.RELATION.IMPORT, currentComponent, commonObjectHolder.log)
        when (dependencyNotInMavenProjectList(commonObjectHolder.targetProjectCollectedProjects, dependency)) {
            true -> {
                navigateDependency(dependency, commonObjectHolder)
            }
            false -> {/*ignore*/
            }
        }
    })
}

@Throws(MojoFailureException::class)
private fun navigateDependency(dependency: Dependency, commonObjectHolder: CommonObjectHolder) {
    val dependencyString = dependencyToGAString(dependency)
    commonObjectHolder.log.debug("navigateDependency $dependencyString")
    val project = getMavenProject(dependency, commonObjectHolder)
    when (project != null) {
        true -> {
            navigateProject(project, commonObjectHolder)
        }
        false -> commonObjectHolder.log.warn("Failed to retrieve Maven Project for ${dependencyToGAString(dependency)}")
    }
}

private fun addMavenProjectRelationship(relationshipSet: HashSet<Relationship>, mavenProject: MavenProject, relation: PrintMojo.RELATION, currentComponent: ComponentModel, log: Log) {
    logMavenProject(mavenProject, relation, log)
    val relatedComponent = mavenProjectToComponentModel(mavenProject)
    relationshipSet.add(Relationship(currentComponent, relatedComponent, relation))
}

private fun addDependencyRelationship(relationshipSet: HashSet<Relationship>, dependency: Dependency, relation: PrintMojo.RELATION, currentComponent: ComponentModel, log: Log) {
    logDependency(dependency, relation, log)
    val relatedComponent = dependencyToComponentModel(dependency)
    relationshipSet.add(Relationship(currentComponent, relatedComponent, relation))
}

@Throws(ProjectBuildingException::class)
private fun getMavenProject(dependency: Dependency, commonObjectHolder: CommonObjectHolder): MavenProject? {
    val resolvedVersion = resolveVersion(dependency.version, commonObjectHolder.targetProject)
    return when (resolvedVersion != null) {
        true -> {
            val artifact = commonObjectHolder.repositorySystem.createProjectArtifact(dependency.groupId, dependency.artifactId, resolvedVersion)
            val build = commonObjectHolder.mavenProjectBuilder.build(artifact, commonObjectHolder.session.projectBuildingRequest)
            build.project
        }
        false -> null
    }
}

private fun resolveVersion(version: String, project: MavenProject): String? {
    return when (version.contains("{")) {
        true -> {
            val versionName = version.replace("\${", "").replace("}", "")
            project.model.properties.getProperty(versionName)
        }
        false -> version
    }
}

private fun dependencyNotInMavenProjectList(listToCheck: ArrayList<MavenProject>, dependency: Dependency): Boolean {
    return listToCheck.none() { it.groupId == dependency.groupId && it.artifactId == dependency.artifactId }
}
