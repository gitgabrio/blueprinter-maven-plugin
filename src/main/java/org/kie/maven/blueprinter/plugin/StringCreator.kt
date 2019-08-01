package org.kie.maven.blueprinter.plugin

import org.apache.maven.artifact.Artifact
import org.apache.maven.model.Dependency
import org.apache.maven.project.MavenProject

/**
 * Functions used to transform specific object to common sting representation
 */

fun mavenProjectString(mavenProject: MavenProject): String = "${mavenProject.groupId}:${mavenProject.artifactId}"
fun dependencyString(dependency: Dependency): String = "${dependency.groupId}:${dependency.artifactId}"
fun artifactString(artifact: Artifact): String = "${artifact.groupId}:${artifact.artifactId}"
