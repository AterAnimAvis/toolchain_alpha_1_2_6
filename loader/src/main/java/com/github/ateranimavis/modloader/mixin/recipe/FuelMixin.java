package com.github.ateranimavis.modloader.mixin.recipe;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.src.ItemStack;
import net.minecraft.src.TileEntityFurnace;
import net.minecraft.src.modloader.ModLoaderExt;

@Mixin(TileEntityFurnace.class)
public class FuelMixin {

    @Inject(method = "getItemBurnTime(Lnet/minecraft/src/ItemStack;)I", at = @At("RETURN"), cancellable = true)
    private void onGetBurnTime(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        // If the Stack isn't Empty
        if (stack == null) return;

        // And it doesn't have a result
        if (cir.getReturnValueI() != 0) return;

        cir.setReturnValue(ModLoaderExt.AddAllFuel(stack));
    }
}
