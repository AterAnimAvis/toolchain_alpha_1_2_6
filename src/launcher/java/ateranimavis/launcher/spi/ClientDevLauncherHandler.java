package ateranimavis.launcher.spi;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

import cpw.mods.gross.Java9ClassLoaderUtil;
import cpw.mods.modlauncher.api.ILaunchHandlerService;
import cpw.mods.modlauncher.api.ITransformingClassLoader;
import cpw.mods.modlauncher.api.ITransformingClassLoaderBuilder;

public class ClientDevLauncherHandler implements ILaunchHandlerService {

    private static final List<String> EXCLUSIONS = Arrays.asList(
        "ateranimavis.launcher",
        "org.objectweb.asm.",
        "org.spongepowered.asm.",
        "joptsimple."
    );

    @Override
    public String name() {
        return "client_dev";
    }

    @Override
    public void configureTransformationClassLoader(ITransformingClassLoaderBuilder builder) {
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

        //TODO: setManifestLocator / setResourceEnumeratorLocator
    }

    @Override
    public Callable<Void> launchService(String[] arguments, ITransformingClassLoader launchClassLoader) {
        launchClassLoader.addTargetPackageFilter(packageLocation -> EXCLUSIONS.stream().noneMatch(packageLocation::startsWith));

        return () ->  {
            Class.forName("net.minecraft.client.Minecraft", true, launchClassLoader.getInstance()).getMethod("main", String[].class).invoke(null, (Object) new String[0]);

            return null;
        };
    }
}
