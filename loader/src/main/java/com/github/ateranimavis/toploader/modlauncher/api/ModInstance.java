package com.github.ateranimavis.toploader.modlauncher.api;

public interface ModInstance<T> {

    String id();

    T getInstance();
}
