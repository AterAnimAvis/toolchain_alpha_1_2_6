package com.github.ateranimavis.modlauncher;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.function.Function;
import java.util.jar.Manifest;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.ateranimavis.modlauncher.api.ModInstance;
import com.github.ateranimavis.modlauncher.api.ModLocation;
import com.github.ateranimavis.modlauncher.api.ModLocator;
import com.github.ateranimavis.modlauncher.urlhandler.ModLocatorURL;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import cpw.mods.modlauncher.api.LamdbaExceptionUtils;

public final class ModLoader {

    private static final Logger LOGGER = LogManager.getLogger();

    private final Map<String, ModLocator> MOD_LOCATORS = new HashMap<>();
    private static final Map<String, ModLocation> LOADED_LOCATIONS = new HashMap<>();
    private static final Map<String, ModInstance<?>> LOADED_MODS = new HashMap<>();

    public Map<String, ModLocator> getServices() {
        return Collections.unmodifiableMap(MOD_LOCATORS);
    }

    public Collection<Path> getResources() {
        return LOADED_LOCATIONS.values()
            .stream()
            .map(ModLocation::getAdditionalClasspath)
            .flatMap(Collection::stream)
            .map(Path::toAbsolutePath)
            .collect(Collectors.toSet());
    }

    public static void discoverMods(Function<ModLocation, Stream<? extends ModInstance<?>>> converter) {
        LOADED_LOCATIONS
            .values()
            .stream()
            .flatMap(converter)
            .forEach(instance -> LOADED_MODS.put(instance.id(), instance));
    }

    public void discoverServices() {
        final ServiceLoader<ModLocator> serviceLoader = ServiceLoader.load(ModLocator.class, ModLoader.class.getClassLoader());

        for (final Iterator<ModLocator> iter = serviceLoader.iterator(); iter.hasNext(); ) {
            final ModLocator next;

            try {
                next = iter.next();
            } catch (final ServiceConfigurationError e) {
                LOGGER.error("Error encountered initializing classpath provider!", e);
                continue;
            }

            MOD_LOCATORS.put(next.name(), next);
        }
    }

    public void discoverResources(Path gameDirectory) {
        for (final Map.Entry<String, ModLocator> entry : MOD_LOCATORS.entrySet()) {
            final ModLocator provider = entry.getValue();

            final Collection<ModLocation> locations = provider.scan(gameDirectory);
            if (locations.size() > 0) {
                locations.forEach(location -> LOGGER.info("Found Location '{}' via '{}'", location.id(), entry.getKey()));
                locations.forEach(location -> LOADED_LOCATIONS.put(location.id(), location));
            }
        }

        LOGGER.info("Loaded Locations " + LOADED_LOCATIONS.values());
    }

    public static Collection<ModInstance<?>> mods() {
        return LOADED_MODS.values();
    }

    public static boolean isLoaded(String id) {
        return LOADED_MODS.containsKey(id) || LOADED_MODS.containsKey(id.replaceFirst("^mod_", ""));
    }

    public static Enumeration<URL> findAllURLsForResource(final String resName) {
        // strip a leading slash
        final String resourceName = resName.startsWith("/") ? resName.substring(1) : resName;

        return new Enumeration<URL>() {
            private final Iterator<ModLocation> iterator = LOADED_LOCATIONS.values().iterator();
            private URL next;

            @Override
            public boolean hasMoreElements() {
                if (next != null) return true;
                next = findNextURL();
                return next != null;
            }

            @Override
            public URL nextElement() {
                if (next == null) {
                    next = findNextURL();
                    if (next == null) throw new NoSuchElementException();
                }
                URL result = next;
                next = null;
                return result;
            }

            private URL findNextURL() {
                while (iterator.hasNext()) {
                    final ModLocation next = iterator.next();
                    final Path resource = next.findResource(resourceName);
                    if (Files.exists(resource)) {
                        return LamdbaExceptionUtils.uncheck(() -> new URL(ModLocatorURL.PREFIX + next.id() + "/" + resourceName));
                    }
                }

                return null;
            }
        };
    }

    public static ModLocation findById(String id) {
        return LOADED_LOCATIONS.get(id);
    }

    public static Path findResource(String id, String modPath) {
        return findById(id).findResource(modPath);
    }

    public static Optional<Manifest> getManifest(String id) {
        return findById(id).findManifest();
    }
}
