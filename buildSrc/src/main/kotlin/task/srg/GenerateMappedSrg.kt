package task.srg

import org.gradle.api.DefaultTask
import java.io.*
import net.minecraftforge.srgutils.IMappingFile
import net.minecraftforge.srgutils.IMappingFile.IField
import net.minecraftforge.srgutils.IMappingFile.IMethod
import net.minecraftforge.srgutils.IRenamer
import org.gradle.api.tasks.*
import utils.*

open class GenerateMappedSrg : DefaultTask() {

    @InputFile
    var input: File? = null

    @InputFiles
    var fields: MutableSet<File> = mutableSetOf()

    @InputFiles
    var methods: MutableSet<File> = mutableSetOf()

    @Optional
    @Input
    var format: IMappingFile.Format = IMappingFile.Format.TSRG

    @OutputFile
    var output: File? = null

    @TaskAction
    @Throws(IOException::class)
    fun apply() {
        output?.ensureParentDirectoriesExist()

        val input = IMappingFile.load(input)

        val srg2mcp = mutableMapOf<String, String>()

        fields.forEach {
            it.readLines().dropWhile { line ->
                !line.contains("Class,Field,Name,Class,Field,Name,Name,Notes")
            }.drop(1).forEach loop@{ line ->
                val parts = line.split(",")
                if (parts.size < 7) return@loop
                if (parts[2] != "*") srg2mcp[parts[2]] = parts[6]
                if (parts[5] != "*") srg2mcp[parts[5]] = parts[6]
            }
        }

        methods.forEach {
            it.readLines().dropWhile { line ->
                !line.contains("class (for reference only),Reference,class (for reference only),Reference,Name,")
            }.drop(1).forEach loop@{ line ->
                val parts = line.split(",")
                if (parts.size < 5) return@loop
                if (parts[1] != "*") srg2mcp[parts[1]] = parts[4]
                if (parts[3] != "*") srg2mcp[parts[3]] = parts[4]
            }
        }

        val ret = input.rename(object : IRenamer {
            override fun rename(value: IField): String {
                return srg2mcp.rename(value.mapped)
            }

            override fun rename(value: IMethod): String {
                return srg2mcp.rename(value.mapped)
            }
        })

        ret.write(output!!.toPath(), format, false)
    }

    private fun <K> Map<K, K>.rename(mapped: K): K = getOrDefault(mapped, mapped)
}

