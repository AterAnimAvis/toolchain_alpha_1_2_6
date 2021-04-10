package task.srg2source

import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputFile
import task.base.MavenJarExec

import java.io.File
import java.util.HashSet

import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.InputFiles

open class ExtractRangeMap : MavenJarExec() {

    @InputDirectory
    var input: File? = null

    @OutputFile
    var output: File? = null

    @InputFiles
    val dependencies: MutableSet<FileCollection> = mutableSetOf()

    @Input
    var compat: String = "1.8"

    @Input
    var batched = true

    init {
        toolJar = "net.minecraftforge:Srg2Source:5.+:fatjar"
        args = arrayOf("--extract", "--source-compatibility", "{compat}", "--output", "{output}", "{libraries}", "--input", "{input}", "--batch", "{batched}")
    }

    override fun filterArgs(): List<String> {
        val replace = mapOf(
                "{input}" to (input?.absolutePath ?: ""),
                "{compat}" to compat,
                "{output}" to (output?.absolutePath ?: ""),
                "{batched}" to (if (batched) "true" else "false")
        )

        return args
                .map { arg -> replace.getOrDefault(arg, arg) }
                .flatMap {
                    if (it == "{libraries}")
                        dependencies.flatMap { fc ->
                                fc.files.flatMap {
                                    f -> listOf("--lib", f.absolutePath)
                                }
                        }
                    else listOf(it)
                }
    }

}