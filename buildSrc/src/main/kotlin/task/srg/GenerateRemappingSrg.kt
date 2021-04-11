package task.srg

import net.minecraftforge.srgutils.IMappingFile
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.*
import utils.ensureParentDirectoriesExist
import java.io.*
import java.nio.file.Files
import java.nio.file.StandardOpenOption

open class GenerateRemappingSrg : DualTransformSrg() {

    init {
        format = IMappingFile.Format.SRG
    }

    override fun apply() {
        super.apply()

        Files.write(output!!.toPath(), listOf("PK: net/minecraft/src .\n"), StandardOpenOption.APPEND)
    }
}

