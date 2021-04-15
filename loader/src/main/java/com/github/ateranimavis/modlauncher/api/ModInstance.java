package com.github.ateranimavis.modlauncher.api;

public interface ModInstance<T> {

    String id();

    T getInstance();
}
