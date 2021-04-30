package com.github.ateranimavis.toploader.loader.mixin.registration;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.FontRenderer;
import net.minecraft.src.GameSettings;
import net.minecraft.src.ModLoader;
import net.minecraft.src.RenderEngine;
import net.minecraft.src.RenderManager;
import net.minecraft.src.World;

@Mixin(RenderManager.class)
public class TextureRegistrationMixin {

    @Inject(method = "setRenderingContext", at = @At(value = "RETURN"))
    private void registerRenderers(World world, RenderEngine engine, FontRenderer font, EntityPlayer player, GameSettings settings, float partialTicks, CallbackInfo ci) {
        if (!ModLoader.texturesOverridden)
            ModLoader.RegisterAllTextureOverrides(engine);
    }
}
