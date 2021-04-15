/*
 * Minecraft Forge
 * Copyright (c) 2016-2021.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation version 2.1
 * of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package com.github.ateranimavis.modlauncher.api.scan;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

import com.github.ateranimavis.modlauncher.api.ModLocation;
import com.github.ateranimavis.modlauncher.api.scan.visitor.ModClassVisitor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.ClassReader;

public class Scanner {

    private static final Logger LOGGER = LogManager.getLogger();

    private final ModLocation location;
    private final Set<ClassData> classes = new HashSet<>();
    private final Set<AnnotationData> annotations = new HashSet<>();

    public Scanner(ModLocation location) {
        this.location = location;
    }

    public Scanner scan() {
        location.scan(this::visit);
        return this;
    }

    public void visit(final Path path) {
        LOGGER.debug("Scanning {} path {}", location.id(), path);
        try (InputStream in = Files.newInputStream(path)) {
            ModClassVisitor mcv = new ModClassVisitor();
            ClassReader cr = new ClassReader(in);
            cr.accept(mcv, 0);
            mcv.buildData(classes, annotations);
        } catch (IOException | IllegalArgumentException e) {
            // mark path bad
        }
    }

    public Set<ClassData> getClasses() {
        return classes;
    }

    public Set<AnnotationData> getAnnotations() {
        return annotations;
    }
}
