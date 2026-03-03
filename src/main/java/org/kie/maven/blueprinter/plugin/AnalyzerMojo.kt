package org.kie.maven.blueprinter.plugin

import org.apache.maven.plugin.MojoExecutionException
import org.apache.maven.plugin.MojoFailureException
import org.apache.maven.plugins.annotations.InstantiationStrategy
import org.apache.maven.plugins.annotations.LifecyclePhase
import org.apache.maven.plugins.annotations.Mojo
import org.kie.maven.blueprinter.plugin.dataclass.AnalyzerObjectHolder
import org.kie.maven.blueprinter.plugin.dataclass.VersionsTuple
import org.kie.maven.blueprinter.plugin.mavenprojectvisitors.MavenProjectVisitor
import org.kie.maven.blueprinter.plugin.utils.logMessage

/**
 * Analyze and print out the duplicated dependencies and versions
 */
@Mojo(
    name = "analyze",
    defaultPhase = LifecyclePhase.VALIDATE,
    threadSafe = true,
    instantiationStrategy = InstantiationStrategy.SINGLETON
)
open class AnalyzerMojo : AbstractBluePrinterMojo() {

    private val globalDependencyVersionsMap = mutableMapOf<String, MutableSet<VersionsTuple>>()

    @Throws(MojoExecutionException::class, MojoFailureException::class)
    override fun execute() {
        logMessage("Executing AnalyzerMojo on instance $this", LOG_LEVEL.DEBUG, retrieveLoggingHolder())
        project.let {
            if (!started) {
                init(it)
                started = true
            }
            val analyzerMojoHolder = AnalyzerObjectHolder(
                globalDependencyVersionsMap,
                retrieveCommonObjectHolder()
            )
            MavenProjectVisitor.init(it).visitForAnalysis(
                analyzerMojoHolder
            )
            projectToBuild.remove(it)
            if (projectToBuild.isEmpty()) {
                started = false
                logMessage("Now we should create the XLS...", LOG_LEVEL.DEBUG, retrieveLoggingHolder())
                analyzerMojoHolder.globalDependencyVersionsMap.forEach { (key, value) ->
                    logMessage("Library -> $key", LOG_LEVEL.INFO, retrieveLoggingHolder())
                    value.forEach {
                        logMessage("$it", LOG_LEVEL.INFO, retrieveLoggingHolder())
                    }
                }
            }
        }
    }
}