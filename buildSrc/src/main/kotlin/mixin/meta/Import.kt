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
package mixin.meta

import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Type
import org.objectweb.asm.tree.AnnotationNode
import org.objectweb.asm.AnnotationVisitor

import java.io.PrintWriter

import org.objectweb.asm.Opcodes.*

/**
 * Represents an imported library for the annotation processor. Currently only supports scanning for mixins and logging their targets
 */
class Import(val file: File) {


    /**
     * Discovered mixin target lines (preformatted
     */
    var targets = mutableListOf<String>()

    /**
     * True if the import was already scanned
     */
    var generated = false

    /**
     * Scan the import
     *
     * @return fluent interface
     */
    fun read() : Import {
        if (this.generated) {
            return this
        }

        if (file.isFile) {
            this.readFile()
        }

        this.generated = true

        return this
    }

    /**
     * Scan a file import
     */
    protected fun readFile() {
        this.targets.clear()

        ZipInputStream(this.file.inputStream()).use { zin ->

            var entry: ZipEntry?
            while (zin.nextEntry.also { entry = it } != null) {
                if (entry!!.isDirectory || !entry!!.name.endsWith(".class")) continue

                // Read the inner classes from the class file
                val mixin = MixinScannerVisitor()
                ClassReader(zin).accept(mixin, ClassReader.SKIP_CODE or ClassReader.SKIP_DEBUG or ClassReader.SKIP_FRAMES)

                mixin.getTargets().forEach { target ->
                    this.targets.add("${mixin.name}\t$target")
                }
            }
        }
    }

    /**
     * Append the contents of this file to the specified writer
     *
     * @param writer Writer to append to
     * @return fluent interface
     */
    fun appendTo(writer : PrintWriter) : Import {
        this.read()
        this.targets.forEach { target -> writer.println(target) }
        return this
    }

    /**
     * ASM class visitor for scanning the classes for Mixin annotations
     */
    private class MixinScannerVisitor : ClassVisitor(ASM5) {

        /**
         * Discovered mixin annotation
         */
        var mixin : AnnotationNode? = null

        /**
         * Discovered class name
         */
        lateinit var name : String

        override fun visit(version: Int, access: Int, name: String, signature: String, superName: String, vararg interfaces: String) {
            this.name = name
        }


        override fun visitAnnotation(desc: String, visible: Boolean) : AnnotationVisitor? {
            if ("Lorg/spongepowered/asm/mixin/Mixin;" == desc) {
                return AnnotationNode(desc).also { mixin = it }
            }
            return super.visitAnnotation(desc, visible)
        }

        fun getTargets(): List<String> {
            if (this.mixin == null) {
                return emptyList()
            }

            val targets = mutableListOf<String>()
            getAnnotationValue<List<Type>?>("value")?.forEach { type -> targets += type.className.replace(".", "/") }
            getAnnotationValue<List<String>?>("targets")?.forEach { type -> targets += type.replace(".", "/") }

            return targets
        }

        private fun <T> getAnnotationValue(key: String) : T? {
            var getNextValue = false

            this.mixin?.values ?: return null

            // Keys and value are stored in successive pairs, search for the key
            // and if found return the following entry
            this.mixin?.values?.forEach { value ->
                if (getNextValue) return value as T
                if (value == key) getNextValue = true
            }

            return null
        }
    }
}
