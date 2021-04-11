package task

import utils.ensureParentDirectoriesExist
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption

open class ApplySpecialSourceInPlace : ApplySpecialSource() {

    private var temp: File? = null

    init {
        args = arrayOf("--in-jar", "{input}", "--out-jar", "{output}", "--srg-in", "{mappings}", "--live")
        outputs.upToDateWhen { false }
    }

    override fun filterArgs(): List<String> {
        temp?.ensureParentDirectoriesExist()

        val replace = mapOf(
                "{input}" to (input?.absolutePath ?: ""),
                "{output}" to (temp?.absolutePath ?: ""),
                "{mappings}" to (mappings?.absolutePath ?: "")
        )

        return args.map { arg -> replace.getOrDefault(arg, arg) }
    }

    override fun apply() {
        temp = project.file("build/$name/output.jar")

        super.apply()

        Files.copy(temp!!.toPath(), output!!.toPath(), StandardCopyOption.REPLACE_EXISTING)
    }
}