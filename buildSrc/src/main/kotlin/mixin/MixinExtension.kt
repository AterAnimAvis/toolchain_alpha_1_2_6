/*
 * This file is part of MixinGradle, licensed under the MIT License (MIT).
 *
 * Copyright (c) SpongePowered <https://www.spongepowered.org>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package mixin

import mcp.reobf
import mixin.eclipse.MixinEclipse
import org.gradle.api.Project
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.the

import mixin.Utils.asArg
import mixin.Utils.refMap
import mixin.Utils.refMapFile
import mixin.Utils.outNotchSrgFile
import mixin.Utils.findMissingAnnotationProcessors
import mixin.meta.Imports
import mixin.tasks.AddRefMapToJarTask
import mixin.tasks.ReobfTask
import org.gradle.api.InvalidUserDataException
import org.gradle.api.artifacts.Configuration
import org.gradle.api.plugins.ExtraPropertiesExtension
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.extra
import java.io.File
import java.io.PrintWriter
import java.nio.file.Files

open class MixinExtension(project: Project) : MixinExtensionBase(project) {

    /**
     * ctor
     *
     * @param project reference to the containing project
     */
    init {
        if (!this.disableEclipseAddon) {
            @Suppress("LeakingThis")
            MixinEclipse.configureEclipse(this, this.project)
        }

        this.init(this.project)
    }

    /**
     * Set up the project by extending relevant objects and adding the
     * <tt>afterEvaluate</tt> handler
     */
    private fun init(project: Project) {
        this.project.afterEvaluate {
            // Gather reobf jars for processing
            project.reobf.forEach { reobfTaskHandle ->
                reobfTasks.add(ReobfTask(project, reobfTaskHandle))
            }

            // Search for sourceSets with a refmap property and configure them
            project.the<JavaPluginConvention>().sourceSets.forEach { set ->
                if (set.extra.has("refMap")) {
                    configure(set)
                }
            }

            // Search for upstream projects and add our jars to their target set
            project.configurations["compile"].allDependencies.withType(ProjectDependency::class.java) {
                val mixinExt = dependencyProject.extensions.findByName("mixin") as MixinExtensionBase?
                if (mixinExt != null) {
                    project.reobf.forEach { reobfTaskWrapper ->
                        mixinExt.reobfTasks += ReobfTask(project, reobfTaskWrapper)
                    }
                }
            }

            applyDefault()

            if (!disableAnnotationProcessorCheck && (majorGradleVersion > 4 || majorGradleVersion == 0)) {

                val missingAPs = this@MixinExtension.findMissingAnnotationProcessors(project)
                if (missingAPs.isNotEmpty()) {
                    val missingAPNames = missingAPs.map { it.annotationProcessorConfigurationName }
                    val message = (if (majorGradleVersion > 4) "Gradle $majorGradleVersion " else "An unrecognised gradle version ") + "was " +
                            "detected but the mixin dependency was missing from one or more Annotation Processor configurations: $missingAPNames. To " +
                            "enable the Mixin AP please include the mixin processor artefact in each Annotation Processor configuration. For example " +
                            "if you are using mixin dependency 'org.spongepowered:mixin:1.2.3-SNAPSHOT' you should specify the dependency " +
                            "'org.spongepowered:mixin:1.2.3-SNAPSHOT:processor'. If you believe you are seeing this message in error, you can disable " +
                            "this check via the disableAnnotationProcessorCheck directive."

                    // Only promote the error message to an actual error if we're sure there's a gradle version mismatch
                    if (majorGradleVersion > 4) {
                        addRefMapToJarTasks.forEach { it.apErrorMessage = message }
                    }

                    // Always log it to the console though
                    project.logger.error(message)
                }
            }
        }
    }


    /**
     * Handle the default behaviour of adding all sourceSets if no sourceSets
     * were explicitly added
     */
    private fun applyDefault() {
        if (this.applyDefault) {
            this.applyDefault = false
            project.logger.info("No sourceSets added for mixin processing, applying defaults")
            this.disableRefMapWarning = true
            project.the<JavaPluginConvention>().sourceSets.filterNotNull().forEach { set ->
                if (!set.extra.has("refMap")) {
                    set.refMap = "mixin.refmap.json"
                }

                this.configure(set)
            }
        }
    }

    /**
     * Add a sourceSet for mixin processing by name, the sourceSet must exist
     * and define the <tt>refMap</tt> property.
     *
     * @param set SourceSet name
     */
    fun add(set: String) {
        this.add(project.the<JavaPluginConvention>().sourceSets[set])
    }

    /**
     * Add a sourceSet for mixin processing, the sourceSet must define the
     * <tt>refMap</tt> property.
     *
     * @param set SourceSet to add
     */
    fun add(set: SourceSet) {
        try {
            set.refMap
        } catch (e : ExtraPropertiesExtension.UnknownPropertyException) {
            throw InvalidUserDataException("No \'refMap\' or \'ext.refMap\' defined on $set. Call \'add(sourceSet, refMapName)\' instead.")
        }
        this.manuallyAdd(set)
    }

    /**
     * Add a sourceSet by name for mixin processing and specify the refMap name,
     * the SourceSet must exist
     *
     * @param set SourceSet name
     * @param refMapName RefMap name
     */
    fun add(set: String, refMapName: Any) {
        val sourceSet = project.the<JavaPluginConvention>().sourceSets.findByName(set)
            ?: throw InvalidUserDataException("No sourceSet \'$set\' was found")
        sourceSet.refMap = refMapName
        this.manuallyAdd(sourceSet)
    }

    /**
     * Add a sourceSet for mixin processing and specify the refMap name, the
     * SourceSet must exist
     *
     * @param set SourceSet to add
     * @param refMapName RefMap name
     */
    fun add(set: SourceSet, refMapName: Any) {
        set.refMap = refMapName.toString()
        this.manuallyAdd(set)
    }

    /**
     * We need to disable the flag early, because our main after evaluate happens before the one we are adding now.
     */
    private fun manuallyAdd(set: SourceSet) {
        // Don't perform default behaviour, a sourceSet has been added manually
        this.applyDefault = false
        project.afterEvaluate {
            configure(set)
        }
    }

    /**
     * Configure a sourceSet for mixin processing and specify the refMap name,
     * the SourceSet must exist
     *
     * @param set SourceSet to add
     */
    fun configure(set: SourceSet) {
        // Check whether this sourceSet was already added
        if (!this.sourceSets.add(set)) {
            project.logger.info("Not adding {} to mixin processor, sourceSet already added", set)
            return
        }

        project.logger.info("Adding {} to mixin processor", set)

        // Get the sourceSet's compile task
        val compileTask = project.tasks[set.compileJavaTaskName]
        if (compileTask !is JavaCompile) {
            throw InvalidUserDataException("Cannot add non-java $set to mixin processor")
        }

        // Don't perform default behaviour, a sourceSet has been added manually
        this.applyDefault = false

        // For closures below
        val refMaps = this.refMaps

        // Refmap file
        val refMapFile = project.file("${compileTask.temporaryDir}/${compileTask.name}-refmap.json")

        // Generated srg file
        val notchSrgFile = project.file("${compileTask.temporaryDir}/${compileTask.name}-mappings.srg")

        // Add our vars as extension properties to the sourceSet and compile
        // tasks, this will allow them to be used in the build script if needed
        compileTask.outNotchSrgFile = notchSrgFile
        compileTask.refMapFile = refMapFile
        set.refMapFile = refMapFile
        compileTask.refMap = set.refMap.toString()

        // We need the mappingsTask to run in order to generate the mappings
        // consumed by the AP
        compileTask.dependsOn(mappingsTask)

        // Closure to prepare AP environment before compile task runs
        compileTask.doFirst {
            if (!disableRefMapWarning && refMaps[compileTask.refMap] != null) {
                project.logger.warn("Potential refmap conflict. Duplicate refmap name {} specified for sourceSet {}, already defined for sourceSet {}",
                compileTask.refMap, set.name, refMaps[compileTask.refMap])
            } else {
                refMaps[compileTask.refMap] = set.name
            }

            refMapFile.delete()
            notchSrgFile.delete()

            checkTokens()
            applyCompilerArgs(compileTask)
        }

        // Refmap is generated with a generic name, rename to sourceset-specific
        // name ready for inclusion into target jar. We can't use rename in the
        // jar spec because there may be multiple refmaps with the same source
        // name
        val taskSpecificRefMap = File(refMapFile.parentFile, compileTask.refMap)

        // Closure to rename generated refMap to artefact-specific refmap when
        // compile task is completed
        compileTask.doLast {
            // Delete the old one
            taskSpecificRefMap.delete()

            // Copy the new one if it was successfully generated
            if (refMapFile.exists()) {
                Files.copy(refMapFile.toPath(), taskSpecificRefMap.toPath())
            }
        }

        // Create a task to contribute the refmap to the jar. Since we don't
        // know at this point which jars are going to be reobfuscated (the
        // inputs to reobf tasks are lazily evaluated at the dependsOn isn't
        // added until later) we add one such task for every jar and the task
        // can handle the heavy lifting of figuring out what to contribute
        project.tasks.withType(Jar::class.java) {
            addRefMapToJarTasks.add(project.tasks.maybeCreate("addRefMapTo${name.capitalize()}", AddRefMapToJarTask::class.java).apply {
                dependsOn(compileTask)

                remappedJar = this@withType
                reobfTasks = this@MixinExtension.reobfTasks
                jarRefMaps.add(taskSpecificRefMap)

                this@withType.dependsOn(this)
            })
        }

        // Closure to allocate generated AP resources once compile task is completed
        this.reobfTasks.forEach { reobfTask ->
            reobfTask.handle.doFirst {
                val thiz = this as task.ApplySpecialSource
                if (notchSrgFile.exists()) {
                    project.logger.info("Contributing tsrg mappings ({}) to {} in {}", notchSrgFile, reobfTask.name, reobfTask.project)
                    thiz.extraMappings.add(notchSrgFile)
                } else {
                    project.logger.debug("Tsrg file ({}) not found, skipping", notchSrgFile)
                }
            }
        }
    }

    /**
     * Callback from the <tt>compileTask.doFirst</tt> closure, configures the
     * annotation processor arguments based on the settings configured in this
     * extension.
     *
     * @param compileTask Compile task to modify
     */
    internal fun applyCompilerArgs(compileTask: JavaCompile) {
        compileTask.options.compilerArgs.addAll(listOf(
                "-AreobfNotchSrgFile=${this.mappings!!.canonicalPath}",
                "-AoutNotchSrgFile=${compileTask.outNotchSrgFile.canonicalPath}",
                "-AoutRefMapFile=${compileTask.refMapFile.canonicalPath}",
                "-AmappingTypes=srg",
                "-ApluginVersion=${MixinGradlePlugin.VERSION}"
        ))

        if (this.disableTargetValidator) {
            compileTask.options.compilerArgs.add("-AdisableTargetValidator=true")
        }

        if (this.disableTargetExport) {
            compileTask.options.compilerArgs.add("-AdisableTargetExport=true")
        }

        if (this.disableOverwriteChecker) {
            compileTask.options.compilerArgs.add("-AdisableOverwriteChecker=true")
        }

        if (this.overwriteErrorLevel != null) {
            compileTask.options.compilerArgs.add("-AoverwriteErrorLevel=${this.overwriteErrorLevel.toString().trim()}")
        }

        if (this.defaultObfuscationEnv != null) {
            compileTask.options.compilerArgs.add("-AdefaultObfuscationEnv=${this.defaultObfuscationEnv}")
        }

        if (this.mappingTypes.size > 0) {
            compileTask.options.compilerArgs.add(this.mappingTypes.asArg("mappingTypes", ","))
        }

        if (this.tokens.isNotEmpty()) {
            compileTask.options.compilerArgs.add(this.tokens.asArg("tokens"))
        }

        if (this.extraMappings.size > 0) {
            compileTask.options.compilerArgs.add(this.extraMappings.map { file -> this.project.file(file).toString() }.asArg("reobfNotchSrgFiles"))
        }

        val importsFile = this.generateImportsFile(compileTask)
        if (importsFile != null) {
            compileTask.options.compilerArgs.add("-AdependencyTargetsFile=${importsFile.canonicalPath}")
        }
    }

    /**
     * Generates an "imports" file given the currently specified imports. If the
     * import set is empty then null is returned, otherwise generates and
     * returns a {@link File} which contains the generated import mappings.
     *
     * @param compileTask Compile task for context
     * @return generated imports file or null if no imports in scope
     */
    private fun generateImportsFile(compileTask: JavaCompile) : File? {
        val importsFile = File(compileTask.temporaryDir, "mixin.imports.json")
        importsFile.delete()

        val libs = mutableSetOf<File>()

        this.importConfigs.forEach { cfg ->
            val config = if (cfg is Configuration) cfg else project.configurations.findByName(cfg.toString())
            config?.files?.forEach { libs.add(it) }
        }

        this.importLibs.forEach { libs.add(project.file(it)) }

        if (libs.size == 0) {
            return null
        }

        importsFile.outputStream().use { stream ->
            val writer = PrintWriter(stream);
            libs.forEach { Imports[it].appendTo(writer) }
            writer.flush()
        }

        return importsFile
    }

}