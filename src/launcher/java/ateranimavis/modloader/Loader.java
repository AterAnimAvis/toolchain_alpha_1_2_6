package ateranimavis.modloader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.logging.log4j.LogManager;
import ateranimavis.launcher.ClientDevLauncher;
import net.minecraft.src.BaseMod;
import net.minecraft.src.modloader.BaseModExt;
import net.minecraft.src.Block;
import net.minecraft.src.Item;
import net.minecraft.src.ItemBlock;

public class Loader {

    private static AtomicBoolean isLoaded = new AtomicBoolean(false);

    private static final Map<String, BaseMod> loadedMods = new HashMap<>();
    private static final Map<String, BaseModExt> loadedExtMods = new HashMap<>();

    public static void init() {
        if (isLoaded.compareAndSet(false, true)) {
            LogManager.getLogger().info("ModLoader Initializing");

            try {
                // Dirty hack - move to IModLocator System?
                readFromClassPath(new File(ClientDevLauncher.class.getProtectionDomain().getCodeSource().getLocation().toURI()));
            } catch (Exception e) {
                throw new RuntimeException("ModLoader has failed to initialize.", e);
            }

            // Copy Block Items into the Item List
            for (int j = 0; j < 256; j++) {
                if (Block.blocksList[j] != null && Item.itemsList[j] == null)
                    Item.itemsList[j] = new ItemBlock(j - 256);
            }
        }
    }

    private static void readFromClassPath(File source) throws IOException {
        ClassLoader loader = Loader.class.getClassLoader();
        if (source.isFile() && (source.getName().endsWith(".jar") || source.getName().endsWith(".zip"))) {
            ZipInputStream zip = new ZipInputStream(new FileInputStream(source));
            ZipEntry entry;
            while (true) {
                entry = zip.getNextEntry();

                if (entry == null) break;

                String name = entry.getName();
                if (!entry.isDirectory() && name.startsWith("mod_") && name.endsWith(".class")) {
                    String modName = name.substring(0, name.length() - 6);
                    addMod(loader, modName, modName);
                }
            }
        } else if (source.isDirectory()) {
            File[] files = source.listFiles();
            if (files != null)
                for (File file : files) {
                    String name = file.getName();
                    if (file.isFile() && name.startsWith("mod_") && name.endsWith(".class")) {
                        String modName = name.substring(0, name.length() - 6);
                        addMod(loader, modName, modName);
                    }
                }

            files = new File(source, "net/minecraft/src").listFiles();
            if (files != null)
                for (File file : files) {
                    String name = file.getName();
                    if (file.isFile() && name.startsWith("mod_") && name.endsWith(".class")) {
                        String modName = name.substring(0, name.length() - 6);
                        addMod(loader, modName, "net.minecraft.src." + modName);
                    }
                }
        }
    }

    private static void addMod(ClassLoader loader, String modName, String clazz) {
        try {
            Class<?> instclass = loader.loadClass(clazz);

            if (instclass.getSuperclass() != BaseMod.class) return;

            loadedMods.putIfAbsent(modName, (BaseMod)instclass.newInstance());

            LogManager.getLogger().info("Mod Loaded: " + modName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Collection<BaseMod> mods() {
        init();

        return loadedMods.values();
    }

    public static Collection<BaseModExt> ext() {
        init();

        return loadedExtMods.values();
    }

    public static void forEach(Consumer<BaseMod> consumer) {
        mods().forEach(consumer);
    }

    public static boolean isLoaded(String mod) {
        init();

        return loadedMods.containsKey(mod);
    }
}
