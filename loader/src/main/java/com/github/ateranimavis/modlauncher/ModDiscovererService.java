package com.github.ateranimavis.modlauncher;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.github.ateranimavis.modlauncher.urlhandler.ModLocatorURL;
import com.github.ateranimavis.util.JarVersionLookupHandler;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import cpw.mods.modlauncher.api.IEnvironment;
import cpw.mods.modlauncher.api.ITransformationService;
import cpw.mods.modlauncher.api.ITransformer;

public final class ModDiscovererService implements ITransformationService {

    private static final String NAME = "mod_discoverer";
    private static final Logger LOGGER = LogManager.getLogger();

    private final ModLoader modLoader = new ModLoader();

    @NotNull
    @Override
    public String name() {
        return ModDiscovererService.NAME;
    }

    @Override
    public void initialize(final @NotNull IEnvironment environment) {
        ModLocatorURL.register();
    }

    @Override
    public void beginScanning(final @NotNull IEnvironment environment) {
        //NOOP
    }

    @Override
    public List<Map.Entry<String, Path>> runScan(final IEnvironment environment) {
        modLoader.discoverResources(environment.getProperty(IEnvironment.Keys.GAMEDIR.get()).orElseThrow(IllegalStateException::new));

        final List<Map.Entry<String, Path>> launchResources = new ArrayList<>();

        for (final Path resource : modLoader.getResources()) {
            launchResources.add(Maps.immutableEntry(resource.getFileName().toString() + "_" + resource.hashCode(), resource));
        }

        launchResources.forEach((entry) -> LOGGER.info("RunScan: '{}'='{}' .", entry.getKey(), entry.getValue()));

        return launchResources;
    }

    @Override
    public void onLoad(final @NotNull IEnvironment env, final @NotNull Set<String> otherServices) {
        LOGGER.info("ModLoader Version {} Loading", JarVersionLookupHandler.getImplementationVersion(ModDiscovererService.class).orElse("NONE"));
        modLoader.discoverServices();
        modLoader.getServices().forEach((k, v) -> LOGGER.info("ModLocator '{}' found.", k));
    }

    @NotNull
    @Override
    public List<ITransformer> transformers() {
        return ImmutableList.of();
    }
}
