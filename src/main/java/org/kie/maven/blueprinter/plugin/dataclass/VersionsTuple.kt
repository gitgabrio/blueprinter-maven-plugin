package org.kie.maven.blueprinter.plugin.dataclass

import org.apache.maven.model.InputLocation

/**
 * Class representing the declaration of a given [org.apache.maven.model.Dependency], at both **Dependency** and **DependencyManagement level.
 * It represents the definition for a specific module.
 * 
 * [dependencyLocation] the **pom.xml** where the *Dependency** level is declared
 * [versionLocation] the **pom.xml** where the **version** variable (used by **Dependency** or **DependencyManagement**) is defined
 */
class VersionsTuple(val version: String, val dependencyLocation: InputLocation, val versionLocation: InputLocation) {
    
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as VersionsTuple

        if (version != other.version) return false
        if (dependencyLocation.toString() != other.dependencyLocation.toString()) return false
        if (versionLocation.toString() != other.versionLocation.toString()) return false

        return true
    }

    override fun hashCode(): Int {
        var result = version.hashCode()
        result = 31 * result + dependencyLocation.toString().hashCode()
        result = 31 * result + versionLocation.toString().hashCode()
        return result
    }

    override fun toString(): String {
        return "VersionsTuple(version='$version', dependencyLocation='$dependencyLocation', versionLocation=$versionLocation)"
    }


}