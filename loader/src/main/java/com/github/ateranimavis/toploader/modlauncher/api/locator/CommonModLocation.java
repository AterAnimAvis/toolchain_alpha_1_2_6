package com.github.ateranimavis.toploader.modlauncher.api.locator;

import java.nio.file.Path;
import java.util.Collection;

import com.github.ateranimavis.toploader.modlauncher.api.ModLocation;
import com.github.ateranimavis.toploader.modlauncher.api.ModLocator;

public class CommonModLocation implements ModLocation {

    private final String id;
    private final Collection<Path> classpath;
    private final ModLocator locator;

    public CommonModLocation(ModLocator locator, String id, Collection<Path> classpath) {
        this.id = id;
        this.classpath = classpath;
        this.locator = locator;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public Collection<Path> getAdditionalClasspath() {
        return classpath;
    }

    @Override
    public ModLocator getLocator() {
        return locator;
    }
}
