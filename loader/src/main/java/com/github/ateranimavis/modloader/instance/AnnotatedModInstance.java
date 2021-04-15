package com.github.ateranimavis.modloader.instance;

import com.github.ateranimavis.modlauncher.api.ModInstance;
import com.github.ateranimavis.modlauncher.api.scan.AnnotationData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AnnotatedModInstance implements ModInstance<Object> {

    private static final Logger LOGGER = LogManager.getLogger();

    private final String id;
    private final Object instance;

    public AnnotatedModInstance(String id, Object instance) {
        this.id = id;
        this.instance = instance;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public Object getInstance() {
        return instance;
    }

    public static AnnotatedModInstance of(AnnotationData data, ClassLoader modClassLoader) {
        String className = data.getClassType().getClassName();
        try {
            Class<?> modClass = Class.forName(className, true, modClassLoader);
            LOGGER.trace("Loaded modclass {} with {}", modClass.getName(), modClass.getClassLoader());

            return new AnnotatedModInstance(data.getAnnotationData().get("value").toString(), modClass.newInstance());
        } catch (Throwable e) {
            LOGGER.error("Failed to load class {}", className, e);
            throw new IllegalStateException(e);
        }
    }
}
