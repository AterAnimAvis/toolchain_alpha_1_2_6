package com.github.ateranimavis.toploader.loader;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.stream.Stream;

import com.github.ateranimavis.toploader.loader.instance.AnnotatedModInstance;
import com.github.ateranimavis.toploader.loader.instance.BaseModInstance;
import com.github.ateranimavis.toploader.modlauncher.TopLoader;
import com.github.ateranimavis.toploader.modlauncher.api.ModInstance;
import com.github.ateranimavis.toploader.modlauncher.api.ModLocation;
import com.github.ateranimavis.toploader.modlauncher.api.scan.ClassData;
import com.github.ateranimavis.toploader.modlauncher.api.scan.Scanner;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.Type;
import net.minecraft.src.BaseMod;
import net.minecraft.src.Block;
import net.minecraft.src.Item;
import net.minecraft.src.ItemBlock;
import net.minecraft.src.modloader.BaseModExt;

public class Loader {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final AtomicBoolean isLoaded = new AtomicBoolean(false);

    private static final Map<String, BaseMod> modloaderMods = new HashMap<>();
    private static final Map<String, BaseModExt> modloaderExtMods = new HashMap<>();

    public static void init() {
        if (isLoaded.compareAndSet(false, true)) {
            LogManager.getLogger().info("TopLoader Initializing");

            TopLoader.discoverMods(Loader::discoverMods);

            TopLoader.mods().forEach((mi) -> {
                Object mod = mi.getInstance();
                if (mod instanceof BaseMod) {
                    modloaderMods.put(mi.id(), (BaseMod) mod);
                }

                if (mod instanceof BaseModExt) {
                    modloaderExtMods.put(mi.id(), (BaseModExt) mod);
                }
            });

            // Copy Block Items into the Item List
            for (int j = 0; j < 256; j++) {
                if (Block.blocksList[j] != null && Item.itemsList[j] == null)
                    Item.itemsList[j] = new ItemBlock(j - 256);
            }
        }
    }

    private static final Type MOD_ANNOTATION = Type.getType(Mod.class);

    public static Stream<? extends ModInstance<?>> discoverMods(ModLocation location) {
        Scanner scanner = new Scanner(location).scan();

        Stream<? extends ModInstance<?>> annotation = scanner
            .getAnnotations()
            .stream()
            .filter(ad -> MOD_ANNOTATION.equals(ad.getAnnotationType()))
            .peek(ad -> LOGGER.debug("Found @Mod class {} with id {}", ad.getClassType().getClassName(), ad.getAnnotationData().get("value")))
            .map(ad -> AnnotatedModInstance.of(ad, Thread.currentThread().getContextClassLoader()));

        Stream<? extends ModInstance<?>> baseMod = scanner
            .getClasses()
            .stream()
            .filter(Loader::isModLoaderMod)
            .map(cd -> BaseModInstance.of(cd, Thread.currentThread().getContextClassLoader()))
            .filter(Objects::nonNull)
            .peek(mod -> LOGGER.debug("Found BaseMod with id {}", mod.id()));

        return Stream.concat(annotation, baseMod);
    }

    private static boolean isModLoaderMod(ClassData data) {
        String className = data.getClazz().getClassName();
        return className.startsWith("net.minecraft.src.mod_") || className.startsWith("mod_");
    }

    public static Collection<BaseMod> mods() {
        init();

        return modloaderMods.values();
    }

    public static Collection<BaseModExt> ext() {
        init();

        return modloaderExtMods.values();
    }

    public static void forEach(Consumer<BaseMod> consumer) {
        mods().forEach(consumer);
    }

    public static boolean isLoaded(String mod) {
        init();

        return TopLoader.isLoaded(mod);
    }
}
