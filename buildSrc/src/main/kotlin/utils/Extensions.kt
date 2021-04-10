package utils

import org.apache.commons.io.IOUtils
import java.io.*
import java.nio.file.Files
import java.nio.file.Path
import java.util.stream.Collectors
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

//------------------------------------------------------------------------------------------------------------------
// Common Extensions
//------------------------------------------------------------------------------------------------------------------

fun Writer.writeln(text: String) {
    write(text + "\n")
}

//------------------------------------------------------------------------------------------------------------------

fun <T> Iterable<T>.displayArgs() = this.joinToString(", ") { m -> "\"$m\"" }

//------------------------------------------------------------------------------------------------------------------

fun File.ensureParentDirectoriesExist() : File {
    parentFile.ensureExists()

    return this
}

fun File.ensureExists() : File {
    if (!exists()) mkdirs()

    return this
}

//------------------------------------------------------------------------------------------------------------------

fun File.deleteIfEmpty() {
    if (list()?.size == 0) delete()
}

fun File.deleteEmptyDirectories()
        = toPath().deleteEmptyDirectories()

fun Path.deleteEmptyDirectories() {
    Files.walk(this)
            .filter { Files.isDirectory(it) }     // We only care about directories
            .sorted(Comparator.reverseOrder())    // We reverse the order to do depth first
            .map(Path::toFile)                    // Map to File
            .forEach { it.deleteIfEmpty() }       // Delete Only Empty Directories
}

//------------------------------------------------------------------------------------------------------------------

fun File.collectAll() : MutableSet<File> = Files.walk(toPath())
        .filter { Files.isRegularFile(it) }
        .map(Path::toFile)
        .collect(Collectors.toSet())

//------------------------------------------------------------------------------------------------------------------

fun File.contentsEqual(zip: ZipFile, entry: ZipEntry) = contentsEqual(zip.getInputStream(entry))

fun File.contentsEqual(stream: InputStream) : Boolean {
    FileInputStream(this).use { a -> stream.use { b-> return IOUtils.contentEquals(a, b) } }
}