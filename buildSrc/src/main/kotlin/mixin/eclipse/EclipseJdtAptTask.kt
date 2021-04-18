package mixin.eclipse

import mixin.MixinExtensionBase
import mixin.MixinGradlePlugin
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.findByType
import java.io.File
import java.util.*

open class EclipseJdtAptTask : DefaultTask() {
    @InputFile
    var mappingsIn : File? = null

    @InputFile
    var refmapOut = project.file("build/${name}/mixins.refmap.json")

    @InputFile
    var mappingsOut = project.file("build/${name}/mixins.mappings.tsrg")

    @Input
    var processorOptions : Map<String, String> = TreeMap()

    @InputFile
    var genTestDir = project.file("build/.apt_generated_test")

    @InputFile
    var genDir = project.file("build/.apt_generated")

    @OutputFile
    var output : File? = null

    @TaskAction
    fun run() {
        val extension = project.extensions.findByType(MixinExtensionBase::class)!!
        val props = OrderedProperties()
        props["eclipse.preferences.version"] = "1"
        props["org.eclipse.jdt.apt.aptEnabled"] = "true"
        props["org.eclipse.jdt.apt.reconcileEnabled"] = "true"
        props["org.eclipse.jdt.apt.genSrcDir"] = genDir.canonicalPath
        props["org.eclipse.jdt.apt.genSrcTestDir"] = genTestDir.canonicalPath
        props.arg("reobfTsrgFile", mappingsIn!!.canonicalPath)
        props.arg("outTsrgFile", mappingsOut.canonicalPath)
        props.arg("outRefMapFile", refmapOut.canonicalPath)
        props.arg("pluginVersion", MixinGradlePlugin.VERSION)

        if (extension.disableTargetValidator) {
            props.arg("disableTargetValidator", "true")
        }

        if (extension.disableTargetExport) {
            props.arg("disableTargetExport", "true")
        }

        if (extension.disableOverwriteChecker) {
            props.arg("disableOverwriteChecker", "true")
        }

        if (extension.overwriteErrorLevel != null) {
            props.arg("overwriteErrorLevel", extension.overwriteErrorLevel.toString().trim())
        }

        if (extension.defaultObfuscationEnv != null) {
            props.arg("defaultObfuscationEnv", extension.defaultObfuscationEnv!!)
        }

        if (extension.mappingTypes.size > 0) {
            props.arg("mappingTypes", extension.mappingTypes.joinToString(","))
        }

        if (extension.tokens_.isNotEmpty()) {
            props.arg("tokens", extension.tokens_.map { (k, v) -> "$k $v" }.joinToString(";"))
        }

        if (extension.extraMappings.size > 0) {
            props.arg("reobfTsrgFiles", extension.extraMappings.joinToString(";") { file -> project.file(file).toString() })
        }

        /* TODO... Not sure what this is
        File importsFile = extension.generateImportsFile(compileTask)
        if (importsFile != null) {
            compileTask.options.compilerArgs += "-AdependencyTargetsFile=${importsFile.canonicalPath}"
        }
        */

        output!!.writer().use { props.store(it, null) }
    }
}