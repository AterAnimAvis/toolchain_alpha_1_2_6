package com.github.ateranimavis.modloader.mixin.loader;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.src.modloader.ModLoaderExt;

@Mixin(Minecraft.class)
public class MinecraftInstanceMixin {

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onCreation(CallbackInfo ci) {
        ModLoaderExt.setInstance((Minecraft) (Object) this);
    }
}
