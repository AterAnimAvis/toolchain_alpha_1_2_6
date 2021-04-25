package com.github.ateranimavis.toploader.loader.mixin.registration;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.src.CraftingManager;
import net.minecraft.src.ModLoader;
import net.minecraft.src.RecipesArmor;

@Mixin(RecipesArmor.class)
public class RecipeRegistrationMixin {

    @Inject(method = "addRecipes(Lnet/minecraft/src/CraftingManager;)V", at = @At("RETURN"))
    private void hookAfterArmorRecipes(CraftingManager manager, CallbackInfo ci) {
        ModLoader.AddAllRecipes(manager);
    }
}
