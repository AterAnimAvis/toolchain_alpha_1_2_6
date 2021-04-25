package com.github.ateranimavis.toploader.modlauncher.api.locator;

import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Locale;
import java.util.Optional;

import com.github.ateranimavis.toploader.modlauncher.api.ModLocation;
import com.github.ateranimavis.toploader.modlauncher.api.ModLocator;

public abstract class AbstractLocator implements ModLocator {

    protected Path getFirstPath(ModLocation location) {
        return maybeFirstPath(location).orElseThrow(IllegalStateException::new);
    }

    protected Optional<Path> maybeFirstPath(ModLocation location) {
        return location.getAdditionalClasspath().stream().findFirst();
    }

    protected String lowercase(Path path) {
        return path.getFileName().toString().toLowerCase(Locale.ROOT);
    }

    protected boolean isClassFile(Path path, BasicFileAttributes attributes) {
        return path.getNameCount() > 0 && path.getFileName().toString().endsWith(".class");
    }
}
