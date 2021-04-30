package net.minecraft.src.modloader;

import com.github.ateranimavis.toploader.loader.Loader;
import com.github.ateranimavis.toploader.loader.mixin.compat.EntityPlayerSPMixin;
import net.minecraft.client.Minecraft;
import net.minecraft.src.EntityPlayerSP;
import net.minecraft.src.GuiScreen;
import net.minecraft.src.ItemStack;
import net.minecraft.src.ModLoader;

/**
 * Extended API for ModLoader <br> Note: Relying on this Class will make your mod dependent on this custom loader
 */
public abstract class ModLoaderExt {

    private static Minecraft minecraft;

    public static boolean doEarlyTextureLoad = true;

    /**
     * @param instance - context object
     * @see EntityPlayerSPMixin#openModGUI(Object)
     */
    public static void openModGui(EntityPlayerSP player, Object instance) {
        GuiScreen gui = ModLoader.OpenModGUI(player, instance);
        if (gui != null)
            minecraft.displayGuiScreen(gui);
    }

    /**
     * @param gui - The class the current open gui should extends or null
     * @see EntityPlayerSPMixin#isGUIOpen(Class)
     */
    public static boolean isGUIOpen(Class<? extends GuiScreen> gui) {
        if (gui == null) return minecraft.currentScreen == null;

        if (minecraft.currentScreen == null) return false;

        return gui.isInstance(minecraft.currentScreen);
    }

    /**
     * Used for adding new sources of fuel to the furnace.
     *
     * @param stack the item stack to use as fuel.
     * @return Duration of fuel provided
     * @see ModLoader#AddAllFuel(int)
     */
    public static int AddAllFuel(ItemStack stack) {
        return Loader
            .ext()
            .stream()
            .map(mod -> mod.AddFuel(stack))
            .filter(burnTime -> burnTime != 0)
            .findFirst().orElseGet(() -> ModLoader.AddAllFuel(stack.itemID));
    }

    public static Minecraft getInstance() {
        return minecraft;
    }

    public static void setInstance(Minecraft minecraft) {
        ModLoaderExt.minecraft = minecraft;
    }
}
