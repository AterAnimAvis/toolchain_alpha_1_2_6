package com.github.ateranimavis.modlauncher.urlhandler;

import java.net.URL;

import com.github.ateranimavis.modlauncher.urlhandler.modlocator.Handler;

public class ModLocatorURL {

    public static final String PROTOCOL = "modlocator";
    public static final String PREFIX = PROTOCOL + "://";

    public static void register() {
        URLHandlers.registerPackageAsHandler(Handler.class);
    }

    /**
     * Why don't we use this? As only a single factory can be registered at any point in time.
     */
    @SuppressWarnings("unused")
    public static void registerAsURLStreamHandlerFactory() {
        URL.setURLStreamHandlerFactory(Handler::maybeGetHandler);
    }
}
