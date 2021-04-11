package net.minecraft.src;

import java.util.List;
import java.util.Map;
import java.util.Random;

import net.minecraft.client.Minecraft;

public abstract class BaseMod {

    /**
     * Place holder for calling the function that adds entity IDs, which are used for SMP, MobSpawner, and saving.
     * @see ModLoader#AddAllEntityIDs()
     */
    public void AddEntityID() {}

    /**
     * Used for adding new sources of fuel to the furnace.
     * @param id ItemID for the item to use as fuel.
     * @return Duration of fuel provided.
     * @see ModLoader#AddAllFuel(int)
     */
    public int AddFuel(int id) {
        return 0;
    }

    /**
     * Used to add recipes.
     * @param recipes - Recipe instance to add to.
     * @see ModLoader#AddAllRecipes(CraftingManager)
     */
    public void AddRecipes(CraftingManager recipes) {}

    /**
     * Used to add entity renderers.
     * @param renderers HashMap of the renderers. key is an entity class, value is the renderer.
     * @see ModLoader#AddAllRenderers(Map)
     */
    public void AddRenderer(Map<Class<? extends Entity>, Render<?>> renderers) {}

    /**
     * Used for adding new options to the furnace for item creation.
     * @param id ItemID of the input item.
     * @return ItemID of the output item.
     * @see ModLoader#AddAllSmelting(int)
     */
    public int AddSmelting(int id) {
        return -1;
    }

    /**
     * Used for generating new blocks (veins) in Nether.
     * @param world Reference to world.
     * @param random Instance of random to use
     * @param chunkX X coordinate of chunk.
     * @param chunkZ Z coordinate of chunk.
     * @see ModLoader#PopulateChunk(IChunkProvider, int, int, World)
     */
    public void GenerateNether(World world, Random random, int chunkX, int chunkZ) {}

    /**
     * Used for generating new blocks (veins) on the surface world.
     * @param world Reference to world.
     * @param random Instance of random to use
     * @param chunkX X coordinate of chunk.
     * @param chunkZ Z coordinate of chunk.
     * @see ModLoader#PopulateChunk(IChunkProvider, int, int, World)
     */
    public void GenerateSurface(World world, Random random, int chunkX, int chunkZ) {}

    /**
     * Opens GUI for use with mods.
     * @param player Player instance to open GUI for.
     * @param instance Used for identifying which mod this call is for. Allows for passing extra data to GUI.
     * @return GUI that mod created.
     * @see ModLoader#OpenModGUI(EntityPlayerSP, Object)
     */
    public GuiScreen OpenModGUI(EntityPlayerSP player, Object instance) {
        return null;
    }

    /**
     * Used for displaying OSDs, called each frame
     * @param game Instance of the game class
     * @see ModLoader#RunOSDHooks(Minecraft)
     */
    public void OSDHook(Minecraft game) {}

    /**
     * Used to register blocks that the client can use.
     * @param registry List of blocks to add to.
     * @see ModLoader#RegisterAllBlocks(List)
     */
    public void RegisterBlocks(List<Block> registry) {}

    /**
     * Used for registering textures to be overlaid over internal tex buffers
     * @see ModLoader#RegisterAllTextureOverrides(RenderEngine)
     * @see ModLoader#addOverride(String, String, int)
     */
    public void RegisterTextureOverrides() {}

    /**
     * Used for registering animations for items and blocks.
     * @see ModLoader#RegisterAllTextureOverrides(RenderEngine)
     * @see ModLoader#addAnimation(TextureFX)
     */
    public void RegisterAnimation(Minecraft game) {}

    /**
     * Place holder for calling the function that adds TileEntities.
     * @see ModLoader#RegisterAllTileEntities()
     */
    public void RegisterTileEntity() {}

}
