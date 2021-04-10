package task

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.*
import org.gradle.api.tasks.Optional
import utils.Utilities
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

open class FilterZip : DefaultTask() {

    @InputFile
    var input: File? = null

    @OutputFile
    var output: File? = null

    @Optional
    @Input
    var filter: (ZipEntry)->Boolean = { _ -> false }

    init {
        outputs.upToDateWhen { false } // Gradle considers this up to date if the output exists at all...
    }

    @TaskAction
    @Throws(IOException::class)
    open fun run() {
        ZipOutputStream(FileOutputStream(output!!)).use { zout ->
            ZipInputStream(FileInputStream(input!!)).use { zin ->
                Utilities.copyZipEntries(zout, zin, filter)
            }
        }
    }

}