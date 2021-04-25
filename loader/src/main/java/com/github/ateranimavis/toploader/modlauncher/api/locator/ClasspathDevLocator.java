package com.github.ateranimavis.toploader.modlauncher.api.locator;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ClasspathDevLocator extends ExplodedDirectoryLocator {

    public ClasspathDevLocator() {
        super(findClasspathPaths());
    }

    private static List<Path> findClasspathPaths() {
        String classpath = System.getProperty("java.class.path");
        String[] classpathEntries = classpath.split(File.pathSeparator);

        return Arrays.stream(classpathEntries)
            .filter(it -> it.replace("\\", "/").contains("/build/classes/") || it.replace("\\", "/").contains("/build/resources/"))
            .map(Paths::get)
            .collect(Collectors.toList());
    }
}
