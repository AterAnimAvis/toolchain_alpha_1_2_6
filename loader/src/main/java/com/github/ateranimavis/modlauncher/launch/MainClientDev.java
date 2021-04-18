package com.github.ateranimavis.modlauncher.launch;

import java.io.IOException;

import com.github.ateranimavis.util.ArgumentList;

public class MainClientDev extends Main {

    public static void main(final String[] args) throws IOException {
        new MainClientDev().launch(args);
    }

    @Override
    public void addArguments(ArgumentList args) {
        super.addArguments(args);

        args.putLazy("launchTarget", "client");

        args.add("mixin.config", "launcher.mixins.json");
        args.add("mixin.config", "launcher.vanity.mixins.json");
    }
}
