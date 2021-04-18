package task.srg

import net.minecraftforge.srgutils.IMappingFile
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.*
import utils.ensureParentDirectoriesExist
import java.io.*
import java.nio.file.Files
import java.nio.file.StandardOpenOption

open class DualTransformSrg : DefaultTask() {

    @InputFile
    lateinit var inputA: File

    @InputFile
    lateinit var inputB: File

    @OutputFile
    lateinit var output: File

    @Input
    var format: IMappingFile.Format = IMappingFile.Format.TSRG

    @Input
    var transformer: (IMappingFile, IMappingFile) -> IMappingFile = { a, _ -> a}

    @TaskAction
    @Throws(IOException::class)
    open fun apply() {
        output.ensureParentDirectoriesExist()

        transformer
                .invoke(IMappingFile.load(inputA), IMappingFile.load(inputB))
                .write(output.toPath(), format, false)
    }
}

