package com.github.ateranimavis.toploader.modlauncher.api;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.jar.Manifest;

public interface ModLocation {

    String id();

    Collection<Path> getAdditionalClasspath();

    ModLocator getLocator();

    // TODO: Set<AnnotationData>?

    default Path findResource(String resource) {
        return getLocator().findPath(this, resource);
    }

    default Optional<Manifest> findManifest() {
        return getLocator().findManifest(this);
    }

    default void scan(Consumer<Path> consumer) {
        getLocator().scan(this, consumer);
    }
}
