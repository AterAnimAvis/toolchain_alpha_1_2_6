package com.github.ateranimavis.toploader.loader.instance;

import com.github.ateranimavis.toploader.modlauncher.api.ModInstance;
import com.github.ateranimavis.toploader.modlauncher.api.scan.ClassData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import net.minecraft.src.BaseMod;

public class BaseModInstance implements ModInstance<BaseMod> {

    private static final Logger LOGGER = LogManager.getLogger();

    private final String id;
    private final BaseMod instance;

    public BaseModInstance(String id, BaseMod instance) {
        this.id = id;
        this.instance = instance;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public BaseMod getInstance() {
        return instance;
    }

    @Nullable
    public static BaseModInstance of(ClassData data, ClassLoader modClassLoader) {
        String className = data.getClazz().getClassName();
        try {
            Class<?> modClass = Class.forName(className, true, modClassLoader);
            LOGGER.trace("Loaded modclass {} with {}", modClass.getName(), modClass.getClassLoader());

            if (modClass.getSuperclass() != BaseMod.class) {
                LOGGER.error("Failed to load mod {} as it's not an instance of BaseMod", className);
                return null;
            }

            return new BaseModInstance(modClass.getSimpleName().replaceFirst("mod_", ""), (BaseMod) modClass.newInstance());
        } catch (Throwable e) {
            LOGGER.error("Failed to load class {}", className, e);
            return null;
        }
    }
}
