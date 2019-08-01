package org.kie.maven.blueprinter.plugin

import org.apache.maven.model.Dependency
import org.apache.maven.plugin.logging.Log
import org.apache.maven.project.MavenProject

/**
 * Functions used to log specific objects
 */

fun logMavenProject(mavenProject: MavenProject, relation: PrintMojo.RELATION, log: Log) = log.debug("$relation: ${mavenProjectString(mavenProject)}:${mavenProject.version}")
fun logDependency(dependency: Dependency, relation: PrintMojo.RELATION, log: Log) = log.debug("$relation: ${dependencyString(dependency)}:${dependency.version}")
