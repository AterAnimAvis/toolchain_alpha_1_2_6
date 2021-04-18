package task

import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import task.base.MavenJarExec
import utils.ensureParentDirectoriesExist
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

open class ApplyMCInjector : MavenJarExec() {

    @InputFile
    lateinit var input: File

    @InputFile
    lateinit var exceptions: File

    @InputFile
    lateinit var access: File

    @InputFile
    lateinit var constructors: File

    @OutputFile
    lateinit var output: File

    @Input
    var generate: Boolean = false

    init {
        toolJar = "de.oceanlabs.mcp:mcinjector:3.8.0:fatjar"
        args = mutableListOf(
                "--in", "{input}",
                "--out", "{output}",
                "--log", "{log}",
                "--level=FINE", // INFO
                "--lvt=LVT",
                "--exc", "{exceptions}",
                "--acc", "{access}",
                "--ctr", "{constructors}"
        )
        jvmArgs = mutableListOf("-Xmx4G")
    }

    override fun filterArgs(): List<String> {
        val arguments = args.toMutableList()

        val logFile = File(project.file("build/$name"), "log-injector.txt")
        val exceptionsOut = exceptions.absolutePath + ".out.txt"
        val accessOut = access.absolutePath + ".out.txt"
        val constructorsOut = constructors.absolutePath + ".out.txt"

        if (generate) {
            arguments.addAll("--excOut", "{exceptions-out}", "--accOut", "{access-out}", "--ctrOut", "{constructors-out}")

            exceptionsOut.deleteIfExists()
            accessOut.deleteIfExists()
            constructorsOut.deleteIfExists()
        }

        output.ensureParentDirectoriesExist()

        val replace = mapOf(
                "{input}" to input.absolutePath,
                "{output}" to output.absolutePath,
                "{exceptions}" to exceptions.absolutePath,
                "{access}" to access.absolutePath,
                "{constructors}" to constructors.absolutePath,
                "{exceptions-out}" to exceptionsOut,
                "{access-out}" to accessOut,
                "{constructors-out}" to constructorsOut,
                "{log}" to logFile.absolutePath
        )

        return arguments.map { arg -> replace.getOrDefault(arg, arg) }
    }

    private fun String.deleteIfExists() = Files.deleteIfExists(Paths.get(this))
    private fun <E> MutableList<E>.addAll(vararg arguments: E) = this.addAll(arguments)

}
