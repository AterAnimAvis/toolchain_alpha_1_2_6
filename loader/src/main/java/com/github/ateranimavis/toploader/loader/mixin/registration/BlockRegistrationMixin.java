package com.github.ateranimavis.toploader.loader.mixin.registration;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.src.Block;
import net.minecraft.src.ModLoader;
import net.minecraft.src.Session;

@Mixin(Session.class)
public class BlockRegistrationMixin {

    @Shadow
    public static List<Block> registeredBlocksList;

    @SuppressWarnings("UnresolvedMixinReference")
    @Inject(method = "<clinit>", at = @At(value = "INVOKE", target = "Ljava/util/List;size()I"))
    private static void register(CallbackInfo ci) {
        ModLoader.RegisterAllBlocks(registeredBlocksList);
    }
}
