package com.github.ateranimavis.toploader.loader.mixin.registration;

import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.src.Entity;
import net.minecraft.src.ModLoader;
import net.minecraft.src.Render;
import net.minecraft.src.RenderManager;

@Mixin(RenderManager.class)
public class RendererRegistrationMixin {

    @Shadow
    private Map<Class<? extends Entity>, Render<?>> entityRenderMap;

    /**
     * Do to Mixin limitations about constructors we just inject at RETURN and redo the setRenderManager loop
     */
    @Inject(method = "<init>()V", at = @At("RETURN"))
    private void registerRenderers(CallbackInfo ci) {
        ModLoader.AddAllRenderers(this.entityRenderMap);

        entityRenderMap.values().forEach(render -> render.setRenderManager((RenderManager) (Object) this));
    }
}
