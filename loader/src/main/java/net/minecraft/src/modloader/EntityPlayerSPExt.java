package net.minecraft.src.modloader;

import net.minecraft.src.GuiScreen;

public interface EntityPlayerSPExt {

    void openModGUI(Object instance);

    boolean isGUIOpen(Class<? extends GuiScreen> gui);
}
