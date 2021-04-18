package task

import utils.ensureParentDirectoriesExist
import java.nio.file.Files
import java.nio.file.StandardCopyOption

open class ApplySpecialSourceInPlace : ApplySpecialSource() {

    init {
        args += "--live"
        outputs.upToDateWhen { false }
    }

    override fun apply() {
        val temp = output
        try {
            output = project.file("build/$name/output.jar")

            super.apply()

            output.ensureParentDirectoriesExist()
            Files.copy(temp.toPath(), output.toPath(), StandardCopyOption.REPLACE_EXISTING)
        } finally {
            output = temp
        }
    }
}