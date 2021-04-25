package com.github.ateranimavis.toploader.vanity;

import com.github.ateranimavis.toploader.loader.Loader;
import com.github.ateranimavis.toploader.util.JarVersionLookupHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import net.minecraft.src.GuiMainMenu;

@Mixin(GuiMainMenu.class)
public class MainMenuMixin {

    @ModifyConstant(method = "drawScreen", constant = @Constant(stringValue = "Minecraft Alpha v1.2.6"))
    private String version(String original) {
        return "Minecraft Alpha v1.2.6 - TopLoader v" + JarVersionLookupHandler.getImplementationVersion(Loader.class).orElse("NONE");
    }
}
