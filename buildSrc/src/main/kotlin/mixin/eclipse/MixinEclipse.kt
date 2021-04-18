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
package mixin.eclipse

import mixin.MixinExtensionBase
import org.gradle.api.Project
import org.gradle.kotlin.dsl.*
import org.gradle.plugins.ide.eclipse.model.EclipseModel

/**
 * Eclipse Specific intergration
 */
object MixinEclipse {

    fun configureEclipse(extension: MixinExtensionBase, project: Project/*, projectType: String, reobf: File, outRef: File, outMapping: File*/) {
        val eclipseModel : EclipseModel? = project.extensions.findByType(EclipseModel::class)

        if (eclipseModel == null) {
            project.logger.lifecycle("[MixinGradle] Skipping eclipse integration, extension not found")
            return
        }

        eclipseModel.jdt.file.withProperties {
            setProperty("org.eclipse.jdt.core.compiler.processAnnotations", "enabled")
        }

        val eclipse = project.tasks.getByName("eclipse")

        val eclipseJdtApt = project.tasks.registering(EclipseJdtAptTask::class) {
            description = "Creates the Eclipse JDT APT settings file"
            output = project.file(".settings/org.eclipse.jdt.apt.core.prefs")
            mappingsIn = extension.mappings
        }
        eclipse.dependsOn(eclipseJdtApt)

        val eclipseFactoryPath = project.tasks.registering(EclipseFactoryPath::class) {
            config = project.configurations.getByName("annotationProcessor")
            output = project.file(".factorypath")
        }
        eclipse.dependsOn(eclipseFactoryPath)
    }

}