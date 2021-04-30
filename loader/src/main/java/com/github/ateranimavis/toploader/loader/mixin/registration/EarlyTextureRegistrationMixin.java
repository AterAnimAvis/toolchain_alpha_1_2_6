package com.github.ateranimavis.toploader.loader.mixin.registration;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.src.ModLoader;
import net.minecraft.src.RenderEngine;
import net.minecraft.src.modloader.ModLoaderExt;

@Mixin(Minecraft.class)
public class EarlyTextureRegistrationMixin {

    @Shadow public RenderEngine renderEngine;

    @Inject(method = "startGame", at = @At(value = "RETURN"))
    private void registerRenderers(CallbackInfo ci) {
        if (ModLoaderExt.doEarlyTextureLoad && !ModLoader.texturesOverridden)
            ModLoader.RegisterAllTextureOverrides(renderEngine);
    }
}
