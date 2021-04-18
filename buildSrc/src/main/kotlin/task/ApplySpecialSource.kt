package task

import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import task.base.MavenJarExec
import utils.ensureParentDirectoriesExist

import java.io.File

open class ApplySpecialSource : MavenJarExec() {

    @InputFile
    lateinit var input: File

    @OutputFile
    lateinit var output: File

    @InputFile
    lateinit var mappings: File

    @InputFiles
    var extraMappings: MutableList<File> = mutableListOf()

    init {
        toolJar = "net.md-5:SpecialSource:1.8.3:shaded"
        args = mutableListOf("--in-jar", "{input}", "--out-jar", "{output}", "{mappings}")
    }

    override fun filterArgs(): List<String> {
        output.ensureParentDirectoriesExist()

        val replace = mapOf(
                "{input}" to input.absolutePath,
                "{output}" to output.absolutePath
        )

        return args.map { arg -> replace.getOrDefault(arg, arg) }.flatMap {
            if (it == "{mappings}")
                (extraMappings + mappings).flatMap { f -> listOf("--srg-in", f.absolutePath) }
            else listOf(it)
        }
    }

}