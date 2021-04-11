package net.minecraft.src.modloader;

import net.minecraft.src.ItemStack;

public interface BaseModExt {

    /**
     * Used for adding new sources of fuel to the furnace.
     * @param stack the item stack to use as fuel
     * @return Duration of fuel provided.
     * @see ModLoaderExt#AddAllFuel(ItemStack)
     */
    default int AddFuel(ItemStack stack) {
        return 0;
    }

}
