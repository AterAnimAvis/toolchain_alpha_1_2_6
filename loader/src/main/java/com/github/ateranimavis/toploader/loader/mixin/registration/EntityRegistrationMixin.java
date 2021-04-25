package com.github.ateranimavis.toploader.loader.mixin.registration;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.src.EntityList;
import net.minecraft.src.ModLoader;

@Mixin(EntityList.class)
public class EntityRegistrationMixin {

    @SuppressWarnings("UnresolvedMixinReference")
    @Inject(method = "<clinit>", at = @At("RETURN"))
    private static void registerEntities(CallbackInfo ci) {
        ModLoader.AddAllEntityIDs();
    }
}
