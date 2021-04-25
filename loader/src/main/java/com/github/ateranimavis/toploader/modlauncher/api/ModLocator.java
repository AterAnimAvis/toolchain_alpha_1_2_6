package com.github.ateranimavis.toploader.modlauncher.api;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.jar.Manifest;

public interface ModLocator {

    Collection<ModLocation> scan(Path gameDirectory);

    void scan(ModLocation location, Consumer<Path> consumer);

    String name();

    Path findPath(final ModLocation location, final String... path);

    Optional<Manifest> findManifest(ModLocation location);

    boolean isValid(ModLocation location);
}
