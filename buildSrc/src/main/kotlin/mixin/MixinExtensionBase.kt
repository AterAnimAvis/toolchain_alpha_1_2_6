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

import mixin.tasks.AddRefMapToJarTask
import mixin.tasks.ReobfTask
import org.gradle.api.InvalidUserDataException
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.SourceSet
import java.io.File

open class MixinExtensionBase(project: Project) {

    /**
     * Cached reference to containing project
     */
    internal val project : Project

    /**
     * mcp-plugin has one major type of project currently
     * 'mcp-plugin' is used by modders
     */
    internal val projectType: String

    /**
     * Detected gradle major version, used in compatibility checks
     */
    internal val majorGradleVersion : Int

    init {
        this.project = project
        this.majorGradleVersion = Utils.detectGradleVersion(project)

        when {
            project.plugins.findPlugin("mcp-plugin") != null -> {
                this.projectType = "mcp-plugin"
            }
            else -> {
                throw InvalidUserDataException("Could not find plugin 'mcp-plugin' on $project, ensure mcp-plugin is applied.")
            }
        }
    }

    /**
     * Until we add some sourcesets, we will assume that the user hasn't
     * configured the plugin in any way (hasn't added a refMap setting on the
     * sourceSet or added any sourceSets in the mixin block). If this is the
     * case we will attach to all sourceSets in <tt>project.afterEvaluate</tt>
     * as our default behaviour. This flag is set to false as soon as
     * {@link MixinExtension#add} is called from any source.
     */
    internal var applyDefault = true

    /**
     * Avoid adding duplicates by adding sourceSets to this collection
     */
    internal val sourceSets = mutableSetOf<SourceSet>()

    /**
     * Mapping of refMap names to sourceSet names, used to avoid refMap
     * conflicts (or at least notify the user that a conflict has occurred)
     */
    internal val refMaps = mutableMapOf<String, String>()

    /**
     * AP tokens, if this map is empty then the tokens argument will be omitted,
     * otherwise it will be compiled to a semicolon-separated list and passed
     * into the
     */
    internal val tokens_ = mutableMapOf<String, String>()

    /**
     * Return current tokens as an unmodifyable map
     */
    val tokens : Map<String, String>
        get() = tokens_

    /**
     * Reobf tasks we will target
     */
    internal val reobfTasks = mutableSetOf<ReobfTask>()

    /**
     * Handles for tasks which add the refmaps to each jar, one per jar
     */
    internal val addRefMapToJarTasks = mutableSetOf<AddRefMapToJarTask>()

    /**
     * If a refMap overlap is detected a warning will be output, however there
     * are situations where a refMap overlap may be desired (for example if
     * different sourceSets are going into different jars) and thus the warning
     * can be ignored. Setting this value to true will suppress the warning.
     */
    var disableRefMapWarning = false

    /**
     * Disables the target validator in the mixin annotation processor.
     */
    var disableTargetValidator = false

    /**
     * Disables the target export in the mixin annotation processor, only
     * useful if multiple compile tasks need to be separated from the point of
     * view of the mixin AP.
     */
    var disableTargetExport = false

    /**
     * Disables the part of this plugin that tries to add IDE
     * integration for the AP to eclipse. Useful if something goes wrong, or
     * you want to manage that part yourself.
     */
    var disableEclipseAddon = false

    /**
     * Disables the overwrite checking functionality which raises warnings when
     * overwrite methods are not appropriately decorated
     */
    var disableOverwriteChecker = false

    /**
     * Disables the check for the annotation processor dependency on gradle 5
     * and above. Potential reasons for doing so being that the AP is present
     * but in a different dep, or the detection has just failed for some reason
     * and the user wants to override
     */
    var disableAnnotationProcessorCheck = false

    /**
     * Sets the overwrite checker error level, the default is to raise WARNING
     * however this can be set to ERROR in order to cause missing decorations
     * to be treated as errors
     */
    var overwriteErrorLevel : Any? = null

    /**
     * The default obfuscation environment to use when generating refMaps. This
     * is the obfuscation set which will end up in the <tt>mappings</tt> node
     * in the generated refMap.
     */
    var defaultObfuscationEnv : String? = "notch"

    /**
     * Mapping types list to pass to the AP. Mapping services may parse the
     * supplied options to determine whether they should activate.
     */
    var mappingTypes = mutableListOf("tsrg")

    /**
     * By default we will attempt to find the SRG file to feed to the AP by
     * querying the <tt>genSrgs</tt> task, however the user can override the
     * file by setting this argument
     */
    var reobfSrgFile : Any? = null

    /**
     * Additional TSRG mapping files to supply to the annotation processor. LTP.
     */
    internal var extraMappings = mutableSetOf<Any>()

    /**
     * Configurations to scan for dependencies when running AP
     */
    protected var importConfigs = mutableSetOf<Any>()

    /**
     * Additional libraries to scan when running AP
     */
    protected var importLibs = mutableSetOf<Any>()

    val mappingsTask: Task?
        get() = when {
            projectType == "mcp-plugin" -> project.findProject(":_mappings")?.tasks?.getByName("generateMcp2Obf")
            else -> null
        }

    /**
     * Getter for reobfSrgFile, fetch from the <tt>genSrgs</tt> task if not configured
     */
    val mappings : File?
        get() = when {
            reobfSrgFile != null -> project.file(reobfSrgFile!!)
            projectType == "mcp-plugin" -> mappingsTask?.outputs?.files?.first()
            else -> null
        }

    //------------------------------------------------------------------------------------------------------------------

    /**
     * Sanity check current tokens before passing them to the AP
     */
    internal fun checkTokens() {
        this.tokens.filter { it.value.contains(';') }.forEach { (key, value) ->
            throw InvalidUserDataException("Invalid token value '$value' for token '$key'")
        }
    }

    fun importConfig(config: Any) {
        this.importConfigs.add(config)
    }

    fun importLibrary(lib: Any) {
        this.importLibs.add(lib)
    }

    /**
     * Adds an additional TSRG file for the AP to consume.
     *
     * @param file Object which resolves to a file
     */
    fun extraMappings(file : Any) {
        this.extraMappings.add(file)
    }

    /**
     * Adds a boolean token with the value true
     *
     * @param name boolean token name
     */
    fun token(name: Any) {
        token(name, "true")
    }

    /**
     * Adds a token with the specified value
     *
     * @param name Token name
     * @param value Token value
     */
    fun token(name: Any, value: Any) {
        this.tokens_[name.toString().trim()] = value.toString().trim()
    }

    /**
     * Add multiple tokens in one go by providing a map
     *
     * @param map map of tokens to add
     * @return fluent interface
     */
    fun tokens(map: Map<String, Any>) : MixinExtensionBase {
        map.forEach { (key, value) -> this.tokens_[key.trim()] = value.toString().trim() }

        return this
    }

    /**
     * Directive version of {@link #disableRefMapWarning}
     */
    fun disableRefMapWarning() {
        this.disableRefMapWarning = true
    }

    /**
     * Directive version of {@link #disableTargetValidator}
     */
    fun disableTargetValidator() {
        this.disableTargetValidator = true
    }

    /**
     * Directive version of {@link #disableTargetExport}
     */
    fun disableTargetExport() {
        this.disableTargetExport = true
    }

    /**
     * Directive version of {@link #disableEclipseAddon}
     */
    fun disableEclipseAddon() {
        this.disableEclipseAddon = true
    }

    /**
     * Directive version of {@link #disableOverwriteChecker}
     */
    fun disableOverwriteChecker() {
        this.disableOverwriteChecker = true
    }

    /**
     * Directive version of {@link #disableAnnotationProcessorCheck}
     */
    fun disableAnnotationProcessorCheck() {
        this.disableAnnotationProcessorCheck = true
    }

    /**
     * Directive version of {@link #overwriteErrorLevel}
     */
    fun overwriteErrorLevel(errorLevel: Any?) {
        this.overwriteErrorLevel = errorLevel
    }

}