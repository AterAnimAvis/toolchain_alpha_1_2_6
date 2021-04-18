package task.srg

import net.minecraftforge.srgutils.IMappingFile
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.*
import utils.ensureParentDirectoriesExist
import java.io.*

open class TransformSrg : DefaultTask() {

    @InputFile
    lateinit var input: File

    @OutputFile
    lateinit var output: File

    @Optional
    @Input
    var format: IMappingFile.Format = IMappingFile.Format.TSRG

    @Optional
    @Input
    var transformer: (IMappingFile) -> IMappingFile = { a -> a}

    @TaskAction
    @Throws(IOException::class)
    fun apply() {
        output.ensureParentDirectoriesExist()

        transformer
                .invoke(IMappingFile.load(input))
                .write(output.toPath(), format, false)
    }
}

