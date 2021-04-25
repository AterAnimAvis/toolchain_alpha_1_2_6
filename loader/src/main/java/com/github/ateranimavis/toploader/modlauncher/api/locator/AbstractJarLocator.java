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
package com.github.ateranimavis.toploader.modlauncher.api.locator;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.stream.Stream;
import java.util.zip.ZipError;

import com.github.ateranimavis.toploader.modlauncher.api.ModLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractJarLocator extends AbstractLocator {
    private static final Logger LOGGER = LogManager.getLogger();

    protected final Map<ModLocation, FileSystem> filesystems = new HashMap<>();
    protected final Map<ModLocation, Manifest> manifests = new HashMap<>();

    protected FileSystem createFileSystem(ModLocation location) {
        try {
            return FileSystems.newFileSystem(getFirstPath(location), location.getClass().getClassLoader());
        } catch (ZipError | IOException e) {
            LOGGER.debug("Invalid JAR file {} - no filesystem created", location.getAdditionalClasspath());

            return null;
        }
    }

    protected ModLocation toLocation(Path path) {
        return new CommonModLocation(this, path.getFileName().toString(), Collections.singleton(path));
    }

    @Override
    public Path findPath(ModLocation location, String... path) {
        if (path.length < 1) throw new IllegalArgumentException("Missing path");

        return filesystems.get(location).getPath("", path);
    }

    @Override
    public void scan(ModLocation location, Consumer<Path> pathConsumer) {
        LOGGER.debug("Scan started: {}", location);

        filesystems.get(location).getRootDirectories().forEach(path -> {
            try (Stream<Path> files = Files.find(path, Integer.MAX_VALUE, this::isClassFile)) {
                files.forEach(pathConsumer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        LOGGER.debug("Scan finished: {}", location);
    }

    @Override
    public Optional<Manifest> findManifest(ModLocation location) {
        return Optional.ofNullable(manifests.computeIfAbsent(location, this::getManifest));
    }

    @Nullable
    private Manifest getManifest(ModLocation location) {
        try (JarFile jf = new JarFile(getFirstPath(location).toFile())) {
            return jf.getManifest();
        } catch (IOException ignored) {
            return null;
        }
    }

    @Override
    public boolean isValid(ModLocation location) {
        return filesystems.containsKey(location);
    }
}
