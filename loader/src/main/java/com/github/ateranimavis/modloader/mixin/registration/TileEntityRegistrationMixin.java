package com.github.ateranimavis.modloader.mixin.registration;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.src.ModLoader;
import net.minecraft.src.TileEntity;

@Mixin(TileEntity.class)
public class TileEntityRegistrationMixin {

    @Inject(method = "<clinit>", at = @At("RETURN"))
    private static void registerTileEntities(CallbackInfo ci) {
        ModLoader.RegisterAllTileEntities();
    }
}
