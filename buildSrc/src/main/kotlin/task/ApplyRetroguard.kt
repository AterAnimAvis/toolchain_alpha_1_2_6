package task

import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import task.base.MavenJarExec
import utils.ensureParentDirectoriesExist

import java.io.File

open class ApplyRetroguard : MavenJarExec() {

    @InputFile
    var input: File? = null

    @InputFile
    var script: File? = null

    @OutputFile
    var output: File? = null

    @Input
    var reindex: Int = 20000

    init {
        toolJar = "de.oceanlabs.mcp:RetroGuard:3.6.6:fatjar"
        mainClass = "RetroGuard"
        args = arrayOf("{input}", "{output}", "{script}", "{log}", "{reindex}")
    }

    override fun filterArgs(): List<String> {
        val logFile = File(project.file("build/$name"), "log-rg.txt")

        output?.ensureParentDirectoriesExist()

        val replace = mapOf(
                "{input}" to (input?.absolutePath ?: ""),
                "{output}" to (output?.absolutePath ?: ""),
                "{script}" to (script?.absolutePath ?: ""),
                "{reindex}" to reindex.toString(),
                "{log}" to logFile.absolutePath
        )

        return args.map { arg -> replace.getOrDefault(arg, arg) }
    }

}