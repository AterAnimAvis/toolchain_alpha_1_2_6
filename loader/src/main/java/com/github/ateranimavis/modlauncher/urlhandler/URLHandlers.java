package com.github.ateranimavis.modlauncher.urlhandler;

import java.net.URLStreamHandler;

public class URLHandlers {

    /**
     * @author https://stackoverflow.com/a/46619972
     */
    public static void registerPackageAsHandler(Class<? extends URLStreamHandler> clazz) {
        String was = System.getProperty("java.protocol.handler.pkgs", "");
        String pkg = clazz.getPackage().getName();
        int ind = pkg.lastIndexOf('.');
        assert ind != -1 : "You can't add url handlers in the base package";
        assert "Handler".equals(clazz.getSimpleName()) : "A URLStreamHandler must be in a class named Handler; not " + clazz.getSimpleName();

        System.setProperty("java.protocol.handler.pkgs", clazz.getPackage().getName().substring(0, ind) + (was.isEmpty() ? "" : "|" + was));
    }
}
