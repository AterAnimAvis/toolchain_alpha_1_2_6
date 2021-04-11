package ateranimavis.modloader.mixin.recipe;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.src.ModLoader;
import net.minecraft.src.TileEntityFurnace;

@Mixin(TileEntityFurnace.class)
public class SmeltingMixin {

    @Inject(method = "getSmeltingResultItem(I)I", at = @At("RETURN"), cancellable = true)
    private void onGetBurnTime(int itemID, CallbackInfoReturnable<Integer> cir) {
        // If it doesn't have a result
        if (cir.getReturnValueI() != -1) return;

        cir.setReturnValue(ModLoader.AddAllSmelting(itemID));
    }

}
