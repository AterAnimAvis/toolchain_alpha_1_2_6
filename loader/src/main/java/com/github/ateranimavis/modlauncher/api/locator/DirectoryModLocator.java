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
package com.github.ateranimavis.modlauncher.api.locator;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Comparator;
import java.util.stream.Collectors;

import com.github.ateranimavis.modlauncher.api.ModLocation;
import cpw.mods.modlauncher.api.LamdbaExceptionUtils;

public class DirectoryModLocator extends AbstractJarLocator {

    private static final String SUFFIX = ".jar";
    private final Path path;

    @SuppressWarnings("unused")
    public DirectoryModLocator() {
        this(Paths.get("mods"));
    }

    public DirectoryModLocator(Path path) {
        this.path = path;
    }

    @Override
    public Collection<ModLocation> scan(Path gameDirectory) {
        return LamdbaExceptionUtils
            .uncheck(() -> Files.list(gameDirectory.resolve(path)))
            .sorted(Comparator.comparing(this::lowercase))
            .filter(path -> lowercase(path).endsWith(SUFFIX))
            .map(this::toLocation)
            .peek(location -> filesystems.compute(location, (mf, fs) -> createFileSystem(mf)))
            .collect(Collectors.toList());
    }

    @Override
    public String name() {
        return "{Directory Locator=" + path + "}";
    }
}
