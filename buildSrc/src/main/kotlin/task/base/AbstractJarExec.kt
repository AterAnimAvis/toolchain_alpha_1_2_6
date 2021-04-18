package task.base

import org.gradle.api.DefaultTask
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.*
import org.gradle.api.tasks.Optional
import java.io.*
import java.util.*
import java.util.jar.Attributes
import java.util.jar.JarFile
import utils.*

abstract class AbstractJarExec : DefaultTask() {

    @Optional
    @Input
    var mainClass : String? = null

    @Input
    var hasLog = true

    @Input
    var args: MutableList<String> = mutableListOf()

    @Input
    var jvmArgs: MutableList<String> = mutableListOf()

    @Optional
    @InputFiles
    var classpath: FileCollection? = null

    @TaskAction
    @Throws(IOException::class)
    open fun apply() {
        val jar = getJar()
        val mainClass = mainClass ?: jar.mainClass
        val workDir = project.file("build/$name").ensureExists()
        val logFile = File(workDir, "log.txt")
        val log = if (hasLog) BufferedOutputStream(FileOutputStream(logFile)) else NullOutputStream

        val printer = PrintWriter(log, true)
        project.javaexec {
            args = this@AbstractJarExec.filterArgs()
            jvmArgs = this@AbstractJarExec.jvmArgs
            classpath = if (this@AbstractJarExec.classpath == null) project.files(jar) else project.files(jar, this@AbstractJarExec.classpath)
            workingDir = workDir
            main = mainClass

            printer.println("Args: " + args!!.displayArgs())
            printer.println("JVM Args: " + jvmArgs!!.displayArgs())
            classpath?.forEach { f -> printer.println("Classpath: " + f.absolutePath) }
            printer.println("WorkDir: $workDir")
            printer.println("Main: $mainClass")
            printer.println("====================================")

            standardOutput = UnclosingOutputStream(log)
        }.rethrowFailure().assertNormalExitValue()

        workDir.deleteIfEmpty()
    }

    protected open fun filterArgs(): List<String> {
        return args
    }

    @InputFile
    abstract fun getJar() : File

    private val File.mainClass : String get() = JarFile(this).use {
        return it.manifest.mainAttributes.getValue(Attributes.Name.MAIN_CLASS)
    }

}