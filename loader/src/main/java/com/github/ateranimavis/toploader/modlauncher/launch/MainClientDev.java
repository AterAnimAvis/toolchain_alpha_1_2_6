package com.github.ateranimavis.toploader.modlauncher.launch;

import java.io.IOException;

import com.github.ateranimavis.toploader.util.ArgumentList;

public class MainClientDev extends Main {

    public static void main(final String[] args) throws IOException {
        new MainClientDev().launch(args);
    }

    @Override
    public void addArguments(ArgumentList args) {
        super.addArguments(args);

        args.putLazy("launchTarget", "client_dev");

        args.add("mixin.config", "top-loader.mixins.json");
        args.add("mixin.config", "top-loader.vanity.mixins.json");

        //FIXME: Probably via a INameMappingService or via
        // property 'mixin.env.remapRefMap', 'true'
        // property 'mixin.env.refMapRemappingFile', "${buildDir}/createSrgToMcp/output.srg"
        System.setProperty("mixin.env.disableRefMap", "true");
    }
}
