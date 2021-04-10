package task

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.*
import utils.Utilities
import java.io.File
import java.io.IOException
import java.util.zip.ZipEntry

open class ExtractZip : DefaultTask() {

    @InputFile
    var input: File? = null

    @OutputDirectory
    var output: File? = null

    @Optional
    @Input
    var filter: (ZipEntry)->Boolean = { _ -> true }

    fun whitelist(filter: (ZipEntry) -> Boolean) {
        this.filter = filter
    }

    fun blacklist(filter: (ZipEntry) -> Boolean) {
        this.filter = { e -> !filter(e) }
    }

    init {
        outputs.upToDateWhen { false } // Gradle considers this up to date if the output exists at all...
    }

    @TaskAction
    @Throws(IOException::class)
    open fun run() {
        Utilities.extractZip(input!!, output!!, filter)
    }

}