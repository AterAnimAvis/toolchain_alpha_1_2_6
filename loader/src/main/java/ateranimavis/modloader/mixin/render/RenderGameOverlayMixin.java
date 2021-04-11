package ateranimavis.modloader.mixin.render;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.src.GuiIngame;
import net.minecraft.src.ModLoader;

@Mixin(GuiIngame.class)
public class RenderGameOverlayMixin {

    @Shadow
    private Minecraft mc;

    @Inject(method = "renderGameOverlay", at = @At(value = "INVOKE", target = "Lorg/lwjgl/input/Keyboard;isKeyDown(I)Z", remap = false))
    private void onRender(CallbackInfo ci) {
        ModLoader.RunOSDHooks(this.mc);
    }

}
