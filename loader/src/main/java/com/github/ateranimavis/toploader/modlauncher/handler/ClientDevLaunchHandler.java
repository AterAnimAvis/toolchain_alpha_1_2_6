package com.github.ateranimavis.toploader.modlauncher.handler;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;

import com.github.ateranimavis.toploader.modlauncher.TopLoader;
import com.github.ateranimavis.toploader.modlauncher.urlhandler.modlocator.Handler;
import org.jetbrains.annotations.NotNull;
import cpw.mods.gross.Java9ClassLoaderUtil;
import cpw.mods.modlauncher.api.ILaunchHandlerService;
import cpw.mods.modlauncher.api.ITransformingClassLoader;
import cpw.mods.modlauncher.api.ITransformingClassLoaderBuilder;

public class ClientDevLaunchHandler implements ILaunchHandlerService {

    private static final List<String> EXCLUSIONS = Arrays.asList(
        "com.github.ateranimavis.toploader.modlauncher.",
        "org.objectweb.asm.",
        "org.spongepowered.asm.",
        "org.lwjgl.",
        "joptsimple."
    );

    @Override
    public String name() {
        return "client_dev";
    }

    @Override
    public void configureTransformationClassLoader(@NotNull ITransformingClassLoaderBuilder builder) {
        for (final URL url : Java9ClassLoaderUtil.getSystemClassPathURLs()) {
            final String target = url.toString();

            if (target.contains("mixin") && target.endsWith(".jar")) {
                continue;
            }

            try {
                builder.addTransformationPath(Paths.get(url.toURI()));
            } catch (URISyntaxException ex) {
                ex.printStackTrace();
            }
        }

        builder.setResourceEnumeratorLocator(TopLoader::findAllURLsForResource);
        builder.setManifestLocator(connection -> connection instanceof Handler.ModLocationConnection
            ? ((Handler.ModLocationConnection) connection).getManifest()
            : Optional.empty()
        );
    }

    @Override
    public Callable<Void> launchService(String @NotNull [] arguments, ITransformingClassLoader launchClassLoader) {
        launchClassLoader.addTargetPackageFilter(packageLocation -> EXCLUSIONS.stream().noneMatch(packageLocation::startsWith));

        return () -> {
            try {
                Class.forName("net.minecraft.client.Minecraft", true, launchClassLoader.getInstance()).getMethod("main", String[].class).invoke(null, (Object) new String[0]);
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(1);
            }

            return null;
        };
    }
}
