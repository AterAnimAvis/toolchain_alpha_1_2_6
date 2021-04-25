package task.patch

import codechicken.diffpatch.cli.PatchOperation
import codechicken.diffpatch.util.LoggingOutputStream
import codechicken.diffpatch.util.PatchMode
import org.gradle.api.DefaultTask
import org.gradle.api.logging.LogLevel
import org.gradle.api.tasks.*
import java.io.File
import java.io.IOException
import utils.ensureParentDirectoriesExist

open class ApplyPatches : DefaultTask() {

    @Internal
    val debug = false

    @InputFile
    lateinit var source: File

    @OutputDirectory
    lateinit var target : File

    @InputDirectory
    lateinit var patches : File

    @TaskAction
    @Throws(IOException::class)
    open fun apply() {
        target.ensureParentDirectoriesExist()

        PatchOperation
            .builder()
            .basePath(source.toPath())
            .outputPath(target.toPath())
            .patchesPath(patches.toPath())
            .logTo(LoggingOutputStream(project.logger, LogLevel.LIFECYCLE))
            .mode(PatchMode.ACCESS)
            .verbose(debug)
            .summary(true)
            .build()
            .operate()
            .throwOnError()
    }
}

