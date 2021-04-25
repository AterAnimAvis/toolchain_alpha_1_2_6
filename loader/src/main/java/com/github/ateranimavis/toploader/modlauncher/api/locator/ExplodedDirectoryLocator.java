/*
 * Minecraft Forge
 * Copyright (c) 2016-2019.
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.jar.Manifest;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.ateranimavis.toploader.modlauncher.api.ModLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.Strings;

public class ExplodedDirectoryLocator extends AbstractLocator {
    private static final Logger LOGGER = LogManager.getLogger();

    private final List<Path> paths;

    public ExplodedDirectoryLocator(List<Path> paths) {
        this.paths = paths;
    }

    @Override
    public Collection<ModLocation> scan(Path gameDirectory) {
        return paths
            .stream()
            .sorted(Comparator.comparing(this::lowercase))
            .map(this::toLocation)
            .collect(Collectors.toList());
    }

    @Override
    public void scan(ModLocation location, Consumer<Path> consumer) {
        LOGGER.debug("Scan started: {}", location);

        try (Stream<Path> files = Files.find(getFirstPath(location), Integer.MAX_VALUE, this::isClassFile)) {
            files.forEach(consumer);
        } catch (IOException e) {
            e.printStackTrace();
        }

        LOGGER.debug("Scan finished: {}", location);
    }

    protected ModLocation toLocation(Path path) {
        return new CommonModLocation(this, path.getFileName().toString() + "_" + path.hashCode(), Collections.singleton(path));
    }

    @Override
    public Optional<Manifest> findManifest(ModLocation location) {
        return Optional.empty();
    }

    @Override
    public boolean isValid(ModLocation location) {
        return maybeFirstPath(location).map(paths::contains).orElse(false);
    }

    @Override
    public String name() {
        return "{Directory Locator=" + Strings.join(paths.iterator(), ',') + "}";
    }

    @Override
    public Path findPath(ModLocation location, String... path) {
        if (path.length < 1) throw new IllegalArgumentException("Missing path");

        return getFirstPath(location).resolve(Paths.get("", path));
    }
}