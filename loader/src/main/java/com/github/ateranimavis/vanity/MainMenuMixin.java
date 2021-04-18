package com.github.ateranimavis.vanity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import net.minecraft.src.GuiMainMenu;

@Mixin(GuiMainMenu.class)
public class MainMenuMixin {

    @ModifyConstant(method = "drawScreen", constant = @Constant(stringValue = "Minecraft Alpha v1.2.6"))
    private String version(String original) {
        return "Modded Minecraft Alpha v1.2.6";
    }
}
