package task.srg

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.io.*
import java.nio.file.Files
import utils.*

open class GenerateRetroGuardTSrg : DefaultTask() {

    @InputFile
    var input: File? = null

    @OutputFile
    var output: File? = null

    @TaskAction
    @Throws(IOException::class)
    fun apply() {
        output?.ensureParentDirectoriesExist()

        var currentClass : String? = null
        OutputStreamWriter(FileOutputStream(output!!)).use { out ->
            Files.readAllLines(input!!.toPath()).forEach {
                if (it.startsWith(".class_map")) {
                    val parts = it.split(" ")
                    val a = parts[1]
                    val b = if (a.contains("/")) a else "net/minecraft/src/${parts[2]}"

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

