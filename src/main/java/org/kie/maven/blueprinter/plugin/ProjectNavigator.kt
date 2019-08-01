package org.kie.maven.blueprinter.plugin

import org.apache.maven.model.Dependency
import org.apache.maven.model.DependencyManagement
import org.apache.maven.plugin.MojoFailureException
import org.apache.maven.plugin.logging.Log
import org.apache.maven.project.MavenProject
import org.apache.maven.project.ProjectBuildingException
import java.util.function.Consumer

/**
 * Functions used to read **MAVEN** projects
 */


fun navigateProject(relationshipSet: HashSet<PrintMojo.Relationship>, toNavigate: MavenProject, localFileName: String, commonObjectHolder: PrintMojo.CommonObjectHolder) {
    toNavigate.let {
        val currentComponent = mavenProjectString(it)
        addComponentToFile(currentComponent, localFileName)
        it.parent?.let { parentProject ->
            if (!commonObjectHolder.targetProjectCollectedProjects.contains(parentProject)) {
                addMavenProjectRelationship(relationshipSet, parentProject, PrintMojo.RELATION.PARENT, currentComponent, commonObjectHolder.log)
                navigateProject(relationshipSet, parentProject, localFileName, commonObjectHolder)
            }
        }
        it.originalModel?.dependencyManagement?.let { dependencyManagement ->
            navigateDependencyManagement(relationshipSet, dependencyManagement, currentComponent, commonObjectHolder)
        }
        it.dependencyManagement?.let { dependencyManagement ->
            navigateDependencyManagement(relationshipSet, dependencyManagement, currentComponent, commonObjectHolder)
        }
        it.collectedProjects
                ?.filter { collectedProject -> collectedProject.parent == it }
                ?.forEach(Consumer { collectedProject ->
                    addMavenProjectRelationship(relationshipSet, collectedProject, PrintMojo.RELATION.CHILD, currentComponent, commonObjectHolder.log)

                })
    }
}

fun writeRelationships(relationshipSet: HashSet<PrintMojo.Relationship>, localFileName: String, log: Log) {
    log.debug("Write relationships")
    relationshipSet
            .map { it.currentComponent }
            .toHashSet()
            .forEach {
                log.debug("addComponentToFile $it -> $localFileName")
                addComponentToFile(it, localFileName)
            }
    relationshipSet.forEach {
        log.debug("writeRelationship $it")
        writeRelationship(it, localFileName, log)
    }
}

private fun navigateDependencyManagement(relationshipSet: HashSet<PrintMojo.Relationship>, dependencyManagement: DependencyManagement, currentComponent: String, commonObjectHolder: PrintMojo.CommonObjectHolder) {
    dependencyManagement.dependencies?.filter { dependency -> dependency.scope == "import" }?.forEach(Consumer { dependency ->
        addDependencyRelationship(relationshipSet, dependency, PrintMojo.RELATION.IMPORT, currentComponent, commonObjectHolder.log)
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
private fun navigateDependency(dependency: Dependency, commonObjectHolder: PrintMojo.CommonObjectHolder) {
    val dependencyString = dependencyString(dependency)
    commonObjectHolder.log.debug("navigateDependency $dependencyString")
    val project = getMavenProject(dependency, commonObjectHolder)
    when (project != null) {
        true -> {
            val fileName = dependencyString(dependency).replace(".", "_").replace(":", "_")
            val relationship = HashSet<PrintMojo.Relationship>();
            initFile(fileName, commonObjectHolder.log)
            navigateProject(relationship, project, fileName, commonObjectHolder)
            writeRelationships(relationship, fileName, commonObjectHolder.log)
            done(fileName, commonObjectHolder.log)
        }
        false -> commonObjectHolder.log.warn("Failed to retrieve Maven Project for ${dependencyString(dependency)}")
    }
}

private fun addMavenProjectRelationship(relationshipSet: HashSet<PrintMojo.Relationship>, mavenProject: MavenProject, relation: PrintMojo.RELATION, currentComponent: String, log: Log) {
    logMavenProject(mavenProject, relation, log)
    val relatedComponent = mavenProjectString(mavenProject)
    relationshipSet.add(PrintMojo.Relationship(currentComponent, relatedComponent, relation))
}

private fun addDependencyRelationship(relationshipSet: HashSet<PrintMojo.Relationship>, dependency: Dependency, relation: PrintMojo.RELATION, currentComponent: String, log: Log) {
    logDependency(dependency, relation, log)
    val relatedComponent = dependencyString(dependency)
    relationshipSet.add(PrintMojo.Relationship(currentComponent, relatedComponent, relation))
}

@Throws(ProjectBuildingException::class)
private fun getMavenProject(dependency: Dependency, commonObjectHolder: PrintMojo.CommonObjectHolder): MavenProject? {
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