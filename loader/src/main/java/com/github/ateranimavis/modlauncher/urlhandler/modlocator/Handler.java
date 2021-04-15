package com.github.ateranimavis.modlauncher.urlhandler.modlocator;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;
import java.util.jar.Manifest;

import com.github.ateranimavis.modlauncher.ModLoader;
import com.github.ateranimavis.modlauncher.urlhandler.ModLocatorURL;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import cpw.mods.modlauncher.api.LamdbaExceptionUtils;

public class Handler extends URLStreamHandler {
    private static final Logger LOGGER = LogManager.getLogger();

    @Nullable
    public static URLStreamHandler maybeGetHandler(String protocol) {
        if (Objects.equals(protocol, ModLocatorURL.PROTOCOL))
            return new Handler();

        return null;
    }

    // modjar://modid/path/to/file
    @Override
    protected URLConnection openConnection(URL url) {
        return new ModLocationConnection(url);
    }

    @Override
    protected int hashCode(final URL u) {
        return Objects.hash(u.getHost(), u.getFile());
    }

    @Override
    protected boolean equals(final URL u1, final URL u2) {
        return Objects.equals(u1.getProtocol(), u2.getProtocol())
            && Objects.equals(u1.getHost(), u2.getHost())
            && Objects.equals(u1.getFile(), u2.getFile());
    }

    public static class ModLocationConnection extends URLConnection {
        private Path resource;
        private String path;
        private String id;

        public ModLocationConnection(final URL url) {
            super(url);
        }

        @Override
        public void connect() {
            if (resource == null) {
                id = url.getHost();

                // trim first char
                path = url.getPath().substring(1);
                resource = ModLoader.findResource(id, path);
            }
        }

        @Override
        public InputStream getInputStream() throws IOException {
            connect();
            LOGGER.trace("Loading modjar URL {} got resource {} {}", url, resource, resource != null ? Files.exists(resource) : "missing");
            return Files.newInputStream(resource);
        }

        @Override
        public long getContentLengthLong() {
            try {
                connect();
                return Files.size(resource);
            } catch (IOException e) {
                return -1L;
            }
        }

        // Used to cache protectiondomains by "top level object" aka the modid
        @Override
        public URL getURL() {
            return LamdbaExceptionUtils.uncheck(() -> new URL(ModLocatorURL.PREFIX + id));
        }

        public Optional<Manifest> getManifest() {
            return ModLoader.getManifest(id);
        }

        @Override public String toString() {
            return "ModJarURLConnection{resource=" + resource + ", path='" + path + '\'' + ", id='" + id + '\'' + '}';
        }
    }
}
