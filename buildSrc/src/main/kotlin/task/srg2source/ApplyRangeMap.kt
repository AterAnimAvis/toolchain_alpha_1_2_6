package task.srg2source

import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import task.base.MavenJarExec
import utils.ensureParentDirectoriesExist

import java.io.File

open class ApplyRangeMap : MavenJarExec() {

    @InputDirectory
    lateinit var input: File

    @InputFile
    lateinit var range: File

    @OutputFile
    lateinit var output: File

    @InputFile
    lateinit var mappings: File

//    @InputFile
//    lateinit var exc: File

    @Input
    var keepImports = true

    init {
        toolJar = "net.minecraftforge:Srg2Source:5.+:fatjar"
        args = mutableListOf( "--apply", "--input", "{input}", "--range", "{range}", "--srg", "{mappings}", /* "--exc", "{exc}", */ "--output", "{output}", "--keepImports", "{keepImports}")
    }

    override fun filterArgs(): List<String> {
        output.ensureParentDirectoriesExist()

        val replace = mapOf(
                "{input}" to input.absolutePath,
                "{range}" to range.absolutePath,
                "{mappings}" to mappings.absolutePath,
//                "{exc}" to exc.absolutePath,
                "{output}" to output.absolutePath,
                "{keepImports}" to (if (keepImports) "true" else "false")
        )

        return args.map { arg -> replace.getOrDefault(arg, arg) }
    }

}