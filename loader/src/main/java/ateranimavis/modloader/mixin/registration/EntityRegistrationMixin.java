package ateranimavis.modloader.mixin.registration;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.src.EntityList;
import net.minecraft.src.ModLoader;

@Mixin(EntityList.class)
public class EntityRegistrationMixin {

    @Inject(method = "<clinit>", at = @At("RETURN"))
    private static void registerEntities(CallbackInfo ci) {
        ModLoader.AddAllEntityIDs();
    }

}
