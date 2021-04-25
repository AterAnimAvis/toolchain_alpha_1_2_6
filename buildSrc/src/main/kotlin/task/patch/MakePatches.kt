package task.patch

import codechicken.diffpatch.cli.DiffOperation
import codechicken.diffpatch.util.LoggingOutputStream
import org.gradle.api.DefaultTask
import org.gradle.api.logging.LogLevel
import org.gradle.api.tasks.*
import java.io.File
import java.io.IOException
import utils.ensureParentDirectoriesExist

open class MakePatches : DefaultTask() {

    @Internal
    val debug = false

    @InputFile
    lateinit var source: File

    @InputDirectory
    lateinit var target : File

    @OutputDirectory
    lateinit var patches : File

    @TaskAction
    @Throws(IOException::class)
    open fun apply() {
        patches.ensureParentDirectoriesExist()

        DiffOperation
            .builder()
            .aPath(source.toPath())
            .bPath(target.toPath())
            .outputPath(patches.toPath())
            .autoHeader(true)
            .logTo(LoggingOutputStream(project.logger, LogLevel.LIFECYCLE))
            .verbose(debug)
            .summary(true)
            .build()
            .operate()
    }
}

