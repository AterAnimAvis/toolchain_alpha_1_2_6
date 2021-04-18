package task.srg

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.io.*
import java.nio.file.Files
import utils.*

open class GenerateRetroGuardTSrg : DefaultTask() {

    @InputFile
    lateinit var input: File

    @Optional
    @InputFile
    var renames: File? = null

    @OutputFile
    lateinit var output: File

    @TaskAction
    @Throws(IOException::class)
    fun apply() {
        output.ensureParentDirectoriesExist()

        val replacements = mutableMapOf<String, String>()
        val renames = renames
        if (renames != null) {
            Files
                .readAllLines(renames.toPath())
                .forEach {
                    if (it.startsWith("#")) return@forEach

                    val parts = it.split(",")
                    if (parts.size >= 2)
                        replacements[parts[0]] = parts[1]
                }
        }

        var currentClass : String? = null
        OutputStreamWriter(FileOutputStream(output)).use { out ->
            Files.readAllLines(input.toPath()).forEach {
                if (it.startsWith(".class_map")) {
                    val parts = it.split(" ")
                    val a = parts[1]
                    var b = if (a.contains("/")) a else "net/minecraft/src/${parts[2]}"

                    replacements.forEach { (k, v) -> if (b.contains(k)) b = b.replace(k, v) }

                    currentClass = a

                    out.writeln("$a $b")
                }

                if (it.startsWith(".method_map")) {
                    val parts = it.split(" ")
                    val clazz = parts[1].substringBeforeLast("/")
                    val a     = parts[1].substringAfterLast("/")
                    val desc  = parts[2]
                    val b     = if (a.length >= 3) a else parts[3]

                    if (currentClass != clazz) {
                        val bClazz = if (clazz.contains("/")) clazz else "net/minecraft/src/${clazz}"
                        out.writeln("$clazz $bClazz")

                        currentClass = clazz
                    }

                    out.writeln("\t$a $desc $b")
                }

                if (it.startsWith(".field_map")) {
                    val parts = it.split(" ")
                    val clazz = parts[1].substringBeforeLast("/")
                    val a     = parts[1].substringAfterLast("/")
                    val b     = if (a.length >= 3) a else parts[2]

                    if (currentClass != clazz) {
                        val bClazz = if (clazz.contains("/")) clazz else "net/minecraft/src/${clazz}"
                        out.writeln("$clazz $bClazz")

                        currentClass = clazz
                    }

                    out.writeln("\t$a $b")
                }
            }
        }
    }

}

