package com.github.ateranimavis.toploader.loader.mixin.compat;

import org.spongepowered.asm.mixin.Mixin;
import net.minecraft.src.EntityPlayerSP;
import net.minecraft.src.GuiScreen;
import net.minecraft.src.modloader.EntityPlayerSPExt;
import net.minecraft.src.modloader.ModLoaderExt;

@Mixin(EntityPlayerSP.class)
public class EntityPlayerSPMixin implements EntityPlayerSPExt {

    @Override
    public void openModGUI(Object instance) {
        ModLoaderExt.openModGui((EntityPlayerSP) (Object) this, instance);
    }

    @Override
    public boolean isGUIOpen(Class<? extends GuiScreen> gui) {
        return ModLoaderExt.isGUIOpen(gui);
    }
}
