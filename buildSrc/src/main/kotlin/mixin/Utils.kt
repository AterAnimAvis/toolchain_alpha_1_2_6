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
import org.gradle.api.artifacts.ResolvedArtifact
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.bundling.AbstractArchiveTask
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.kotlin.dsl.extra
import org.gradle.kotlin.dsl.get
import org.gradle.util.VersionNumber
import java.io.File

object Utils {

    sealed class Either<out A, out B> {
        class Left<A>(val value: A): Either<A, Nothing>()
        class Right<B>(val value: B): Either<Nothing, B>()
    }

    private fun <T, R> T?.mapNonNull(func: (T) -> R) : R? {
        if (this == null) return null
        return func(this)
    }

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
    fun detectGradleMajorVersion(project : Project) : Int {
        return MAJOR_VERSION.find(project.gradle.gradleVersion)
            ?.groups
            ?.get("major")
            ?.value
            ?.toIntOrNull() ?: 0
    }

    /**
     * Searches for the annotation processor dependency in all sourceset ap
     * configurations which have a refmap. Returns true if the AP is found and
     * false if it is not found in any added configuration.
     */
    fun MixinExtensionBase.findMissingAnnotationProcessors() : Set<SourceSet> {
        val missingAPs = mutableSetOf<SourceSet>()
        sourceSets.filter { sourceSet ->
            sourceSet.mixinDependency =
                findMixinDependency(sourceSet.compileConfigurationName) ?:
                findMixinDependency(sourceSet.implementationConfigurationName)

            if (sourceSet.mixinDependency != null) {
                sourceSet.apDependency = findMixinDependency(sourceSet.annotationProcessorConfigurationName)
                if (sourceSet.apDependency != null) {
                    val mainVersion = getDependencyVersion(sourceSet.mixinDependency!!)
                    val apVersion = getDependencyVersion(sourceSet.apDependency!!)
                    if (mainVersion > apVersion) {
                        project.logger.warn("Mixin AP version ($apVersion) in configuration '${sourceSet.annotationProcessorConfigurationName}' is older than compile version ($mainVersion)")
                    }
                } else {
                    return@filter true
                }
            }

            return@filter false
        }.forEach {
            missingAPs.add(it)
        }

        return missingAPs
    }

    /**
     * Detected dependencies might be a resolved artefact or a declared
     * dependency (depending on where we are in the build lifecycle). This
     * method abstracts parsing a dependency version from either supported
     * object type.
     */
    private fun getDependencyVersion(dependency: Either<ResolvedArtifact, Dependency>) : VersionNumber {
        if (dependency is Either.Left) {
            return VersionNumber.parse(dependency.value.moduleVersion.id.version)
        } else if (dependency is Either.Right) {
            return VersionNumber.parse(dependency.value.version!!)
        }

        return VersionNumber.UNKNOWN
    }

    /**
     * Checks whether a configuration contains any mixin dependency in its
     * explicit or resolved dependency artefacts
     *
     * @param configurationName Configuration name to check
     * @return true if the configuration contains a mixin dependency
     */
    fun MixinExtensionBase.findMixinDependency(configurationName: String) : Either<ResolvedArtifact, Dependency>? {
        val configuration = project.configurations[configurationName]

        if (configuration.isCanBeResolved) {
            val resolved = configuration.resolvedConfiguration.resolvedArtifacts.find { it.id.toString().contains(":mixin:") }
            return resolved.mapNonNull { Either.Left(it) }
        }

        val dependency = configuration.allDependencies.find { it.group?.contains("spongepowered") ?: false && it.name.contains("mixin") }
        return dependency.mapNonNull { Either.Right(it) }
    }

    /**
     * Searches the compile configuration of each SourceSet that has been
     * registered via add() looking for the mixin dependency. If the mixin
     * dependency is found and the current gradle version is 5 or higher then
     * check that the Annotation Processor artefact has been added to the
     * corresponding annotationProcessor configuration for the SourceSet and
     * raise an error if the AP was not found.
     *
     * <p>This is necessary because in previous versions of gradle, APs in
     * compile dependencies were automatically added, however since gradle 5
     * they must be explicitly specified but knowing this assumes that all end
     * users read the gradle upgrade notes when changing versions, which is not
     * realistically the case. So thanks gradle.</p>
     */
    fun MixinExtensionBase.checkForAnnotationProcessors() {
        if (this.disableAnnotationProcessorCheck || (this.majorGradleVersion in 1..4)) return

        val missingAPs = this.findMissingAnnotationProcessors()
        if (missingAPs.isNotEmpty()) {
            val missingAPNames = missingAPs.map { it.annotationProcessorConfigurationName }
            val message = (if (this.majorGradleVersion > 4) "Gradle ${this.majorGradleVersion} " else "An unrecognised gradle version ") + "was " +
            "detected but the mixin dependency was missing from one or more Annotation Processor configurations: $missingAPNames. To " +
                    "enable the Mixin AP please include the mixin processor artefact in each Annotation Processor configuration. For example " +
                    "if you are using mixin dependency 'org.spongepowered:mixin:0.1.2-SNAPSHOT' you should specify the dependency " +
                    "'org.spongepowered:mixin:0.1.2-SNAPSHOT:processor'. If you believe you are seeing this message in error, you can disable " +
                    "this check via the disableAnnotationProcessorCheck() directive."

            // Only promote the error message to an actual error if we're sure there's a gradle version mismatch
            if (this.majorGradleVersion >= 5) {
                throw MixinGradleException(message)
            } else {
                project.logger.error(message)
            }
        }
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
    var SourceSet.mixinDependency
        get() = extra["mixinDependency"] as Either<ResolvedArtifact, Dependency>?
        set(value) {
            extra["mixinDependency"] = value
        }

    @JvmStatic
    var SourceSet.apDependency
        get() = extra["apDependency"] as Either<ResolvedArtifact, Dependency>?
        set(value) {
            extra["apDependency"] = value
        }

    @JvmStatic
    var AbstractArchiveTask.refMaps
        set(fileCollection) {
            extra["refMaps"] = fileCollection
        }
        get() : ConfigurableFileCollection {
            if (!this.extra.has("refMaps")) {
                extra["refMaps"] = project.files()
            }

            return extra["refMaps"] as ConfigurableFileCollection
        }

}