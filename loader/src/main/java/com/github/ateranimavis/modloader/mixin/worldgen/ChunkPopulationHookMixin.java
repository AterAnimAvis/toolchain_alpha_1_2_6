package com.github.ateranimavis.modloader.mixin.worldgen;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.src.ChunkProviderLoadOrGenerate;
import net.minecraft.src.IChunkProvider;
import net.minecraft.src.ModLoader;
import net.minecraft.src.World;

@Mixin(ChunkProviderLoadOrGenerate.class)
public class ChunkPopulationHookMixin {

    @Shadow
    private IChunkProvider chunkProvider;

    @Shadow
    private World worldObj;

    @Inject(method = "populate(Lnet/minecraft/src/IChunkProvider;II)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/src/Chunk;setChunkModified()V"))
    private void onPopulateChunk(IChunkProvider provider, int xPosition, int zPosition, CallbackInfo ci) {
        ModLoader.PopulateChunk(this.chunkProvider, xPosition * 16, zPosition * 16, this.worldObj);
    }
}
