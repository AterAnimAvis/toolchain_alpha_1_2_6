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
package mixin.tasks

import mixin.MixinGradleException
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.jvm.tasks.Jar
import java.io.File

import mixin.Utils.refMaps
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal

open class AddRefMapToJarTask : DefaultTask() {

    @Input
    lateinit var remappedJar: Jar

    @Input
    var reobfTasks = mutableSetOf<ReobfTask>()

    @Internal
    var jarRefMaps = mutableSetOf<File>()

    @Internal
    /**
     * Annotation Processor error message, which is logged to the console if the
     * environment is misconfigured but is elevated to an error condition if any
     * addRefmapToJar task runs. This stops misconfigured projects from breaking
     * IDE import process of gradle project but should still result in a fatal
     * error when building production artefacts.
     */
    var apErrorMessage : String? = null

    @TaskAction
    fun run() {
        if (this.apErrorMessage != null) throw MixinGradleException(this.apErrorMessage!!)

        // Add the refmap to all reobf'd jars
        this.reobfTasks.forEach { reobfTask ->
            reobfTask.handle.dependsOn.filter { it == remappedJar }.map { it as Jar }.forEach { jar ->
                jarRefMaps.forEach { artefactSpecificRefMap ->
                    project.logger.info("Contributing refmap ({}) to {} in {}", artefactSpecificRefMap, jar.archiveFileName, reobfTask.project)
                    jar.refMaps.from(artefactSpecificRefMap)
                    jar.from(artefactSpecificRefMap)
                }
            }
        }
    }
}