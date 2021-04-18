package task

import org.gradle.api.tasks.InputFile
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

    init {
        toolJar = "net.md-5:SpecialSource:1.8.3:shaded"
        args = arrayOf("--in-jar", "{input}", "--out-jar", "{output}", "--srg-in", "{mappings}")
    }

    override fun filterArgs(): List<String> {
        output.ensureParentDirectoriesExist()

        val replace = mapOf(
                "{input}" to input.absolutePath,
                "{output}" to output.absolutePath,
                "{mappings}" to mappings.absolutePath
        )

        return args.map { arg -> replace.getOrDefault(arg, arg) }
    }

}