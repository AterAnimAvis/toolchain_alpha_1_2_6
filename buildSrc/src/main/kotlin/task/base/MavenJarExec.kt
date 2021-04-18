package task.base

import org.gradle.api.tasks.Input
import java.io.File
import utils.Utilities

open class MavenJarExec : AbstractJarExec() {

    @Input
    lateinit var toolJar : String

    override fun getJar(): File {
        return Utilities.resolve(project, toolJar, isChanging=false)!!
    }
}