package mixin.eclipse

import groovy.xml.MarkupBuilder
import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.Configuration
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.withGroovyBuilder
import java.io.File

open class EclipseFactoryPath : DefaultTask() {
    @InputFiles
    lateinit var config : Configuration

    @OutputFile
    lateinit var output : File

    @TaskAction
    fun run() {
        output.outputStream().writer().use {
            MarkupBuilder(it).withGroovyBuilder {
                "factorypath" {
                    config.resolvedConfiguration.resolvedArtifacts.forEach { dep ->
                        "factorypathentry"(
                            "kind" to "EXTJAR",
                            "id" to dep.file.absolutePath,
                            "enabled" to true,
                            "runInBatchMode" to false
                        )
                    }
                }
            }
        }
    }
}