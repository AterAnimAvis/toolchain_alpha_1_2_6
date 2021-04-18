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

import org.gradle.api.InvalidUserDataException
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.bundling.AbstractArchiveTask
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.kotlin.dsl.extra
import org.gradle.kotlin.dsl.get
import java.io.File

object Utils {

    @JvmStatic
    @JvmOverloads
    fun <A : Any, B : Any> Map<A, B>.asArg(argName: String, separator: String = ";") : String {
        return if (isEmpty()) "" else "-A${argName}=" + map { (key, value) -> "$key=$value" }.joinToString(separator)
    }

    @JvmStatic
    @JvmOverloads
    fun <A> List<A>.asArg(argName: String, separator: String = ";") : String {
        return if (isEmpty()) "" else "-A${argName}=${joinToString(separator)}"
    }

    private val MAJOR_VERSION = "^(?<major>[0-9]+)\\.".toRegex()

    @JvmStatic
    fun detectGradleVersion(project : Project) : Int {
        return MAJOR_VERSION.find(project.gradle.gradleVersion)
            ?.groups
            ?.get("major")
            ?.value
            ?.toIntOrNull() ?: 0
    }

    fun MixinExtensionBase.findMissingAnnotationProcessors(project: Project) : Set<SourceSet> {
        val missingAPs = mutableSetOf<SourceSet>()
        val depFinder = { it: Dependency -> (it.group?.contains("spongepowered") ?: false) && (it.name.contains("mixin")) }
        val find = { name: String -> project.configurations[name].dependencies.find(depFinder) }

        sourceSets.filter {
            (find(it.compileConfigurationName) ?: find(it.implementationConfigurationName) != null)
                    && find(it.annotationProcessorConfigurationName) == null
        }.forEach {
            missingAPs.add(it)
        }

        return missingAPs
    }

    /**
     * Sanity check current tokens before passing them to the AP
     */
    fun MixinExtensionBase.checkTokens() {
        this.tokens_.filter { it.value.contains(';') }.forEach { (key, value) ->
            throw InvalidUserDataException("Invalid token value '$value' for token '$key'")
        }
    }

    @JvmStatic
    var JavaCompile.outNotchSrgFile
        get() : File = extra["outNotchSrgFile"] as File
        set(value) {
            extra["outNotchSrgFile"] = value
        }

    @JvmStatic
    var JavaCompile.refMap
        get() : String = extra["refMap"] as String
        set(value) {
            extra["refMap"] = value
        }

    @JvmStatic
    var JavaCompile.refMapFile
        get() : File = extra["refMapFile"] as File
        set(value) {
            extra["refMapFile"] = value
        }

    @JvmStatic
    var SourceSet.refMap
        get() = extra["refMap"]
        set(value) {
            extra["refMap"] = value
        }

    @JvmStatic
    var SourceSet.refMapFile
        get() = extra["refMapFile"]
        set(value) {
            extra["refMapFile"] = value
        }

    @JvmStatic
    var AbstractArchiveTask.refMaps
        set(fileCollection) {
            extra["refMaps"] = fileCollection
        }
        get() : ConfigurableFileCollection {
            if (!this.extra.has("refMaps")) {
                extra["refMaps"] = project.layout.configurableFiles()
            }

            return extra["refMaps"] as ConfigurableFileCollection
        }

}