package ateranimavis.launcher;

import ateranimavis.launcher.repackage.sponge.ArgumentList;
import cpw.mods.modlauncher.Launcher;

public class ClientDevLauncher {

    public static void main(String[] args) {
        ArgumentList list = ArgumentList.from(args);

        list.putLazy("gameDir", ".");
        list.putLazy("launchTarget", "client_dev");
        list.put("mixin.config", "launcher.mixins.json");

        Launcher.main(list.getArguments());
    }

}
