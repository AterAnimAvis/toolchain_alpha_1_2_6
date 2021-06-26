package net.minecraft.src;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import javax.imageio.ImageIO;

import com.github.ateranimavis.toploader.loader.Loader;
import com.github.ateranimavis.toploader.util.Pair;
import com.github.ateranimavis.toploader.util.ReflectionHelper;
import org.apache.logging.log4j.LogManager;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import net.minecraft.client.Minecraft;
import net.minecraft.src.modloader.ModLoaderExt;
import net.minecraft.src.modloader.SpriteHandler;

public abstract class ModLoader {

    /**
     * Whether RegisterAllTextureOverrides has already run.
     */
    public static boolean texturesOverridden;

    private static int highestEntityId = 3000;

    private static final List<TextureFX> animations = new ArrayList<>();
    private static final Map<String, List<Pair<String, Integer>>> overrides = new HashMap<>();

    /**
     * Place holder for calling the function that adds entity IDs, which are used for SMP, MobSpawner, and saving.
     *
     * @see BaseMod#AddEntityID()
     */
    public static void AddAllEntityIDs() {
        Loader.forEach(BaseMod::AddEntityID);
    }

    /**
     * Used for adding new sources of fuel to the furnace.
     *
     * @param id ItemID for the item to use as fuel.
     * @return Duration of fuel provided
     * @see BaseMod#AddFuel(int)
     */
    public static int AddAllFuel(int id) {
        return Loader.mods().stream().map(mod -> mod.AddFuel(id)).filter(burnTime -> burnTime != 0).findFirst().orElse(0);
    }

    /**
     * Used to add recipes from all the mods.
     *
     * @param recipes Recipe instance to add to.
     * @see BaseMod#AddRecipes(CraftingManager)
     */
    public static void AddAllRecipes(CraftingManager recipes) {
        Loader.mods().forEach(mod -> mod.AddRecipes(recipes));
    }

    /**
     * Used to add all mod entity renderers.
     *
     * @param renderers HashMap of the renderers. key is an entity class, value is the renderer.
     * @see BaseMod#AddRenderer(Map)
     */
    public static void AddAllRenderers(Map<Class<? extends Entity>, Render<?>> renderers) {
        Loader.mods().forEach(mod -> mod.AddRenderer(renderers));
    }

    /**
     * Used for adding new options to the furnace for item creation.
     *
     * @param id ItemID of the input item.
     * @return ItemID of the output item.
     * @see BaseMod#AddSmelting(int)
     */
    public static int AddAllSmelting(int id) {
        return Loader.mods().stream().map(mod -> mod.AddSmelting(id)).filter(itemId -> itemId != -1).findFirst().orElse(-1);
    }

    /**
     * Registers one texture override to be done.
     *
     * @param path        Path to the texture file to modify.
     * @param overlayPath Path to the texture file which is to be overlaid.
     * @param index       Sprite index into the texture to be modified.
     */
    public static void addOverride(String path, String overlayPath, int index) {
        overrides.putIfAbsent(path, new ArrayList<>());
        overrides.get(path).add(Pair.of(overlayPath, index));
    }

    /**
     * Register one animation instance.
     *
     * @param anim instance of animation handler
     */
    public static void addAnimation(TextureFX anim) {
        animations.add(anim);
    }

    /**
     * Used for getting value of private fields.
     *
     * @param instance   Object to get private field from.
     * @param fieldindex Name of the field.
     * @param <T>        Return type.
     * @return Value of private field.
     * @throws SecurityException    if the thread is not allowed to access field.
     * @throws NoSuchFieldException if field does not exist.
     */
    public static <T> T getPrivateValue(Object instance, int fieldindex) throws SecurityException, NoSuchFieldException {
        try {
            //noinspection unchecked
            return (T) ReflectionHelper.getFieldByIndex(instance.getClass(), fieldindex).get(instance);
        } catch (IllegalAccessException | IllegalArgumentException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Used for getting value of private fields.
     *
     * @param instance Object to get private field from.
     * @param field    Name of the field.
     * @param <T>      Return type.
     * @return Value of private field.
     * @throws SecurityException    if the thread is not allowed to access field.
     * @throws NoSuchFieldException if field does not exist.
     */
    public static <T> T getPrivateValue(Object instance, String field) throws SecurityException, NoSuchFieldException {
        try {
            //noinspection unchecked
            return (T) ReflectionHelper.getField(instance.getClass(), field).get(instance);
        } catch (IllegalAccessException | IllegalArgumentException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Used for setting value of private fields.
     *
     * @param instance   Object to get private field from.
     * @param fieldindex Name of the field.
     * @param value      Value to set.
     * @param <T>        Return type.
     * @throws SecurityException    if the thread is not allowed to access field.
     * @throws NoSuchFieldException if field does not exist.
     */
    public static <T> void setPrivateValue(Object instance, int fieldindex, T value) throws SecurityException, NoSuchFieldException {
        try {
            Field f = ReflectionHelper.getFieldByIndex(instance.getClass(), fieldindex);
            f.set(instance, value);
        } catch (IllegalAccessException | IllegalArgumentException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Used for setting value of private fields.
     *
     * @param instance Object to get private field from.
     * @param field    Name of the field.
     * @param value    Value to set.
     * @param <T>      Return type.
     * @throws SecurityException    if the thread is not allowed to access field.
     * @throws NoSuchFieldException if field does not exist.
     */
    public static <T> void setPrivateValue(Object instance, String field, T value) throws SecurityException, NoSuchFieldException {
        try {
            Field f = ReflectionHelper.getField(instance.getClass(), field);
            f.set(instance, value);
        } catch (IllegalAccessException | IllegalArgumentException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Gets the next Entity ID to use.
     *
     * @return Assigned ID.
     */
    public static int getUniqueEntityId() {
        return highestEntityId++;
    }

    /**
     * Gets next available index for this sprite map.
     *
     * @param path Sprite map to get available index from.
     * @return Assigned sprite index to use.
     */
    public static int getUniqueSpriteIndex(String path) {
        int result = SpriteHandler.getUniqueSpriteIndex(path);

        if (result < 0)
            throw new RuntimeException("No more empty sprites indices left for " + path + "!");

        return result;
    }

    /**
     * Checks if a mod is loaded.
     *
     * @param mod - Name of the mod to check for.
     * @return Returns true if a mod with supplied name exists in the mod list.
     */
    public static boolean isModLoaded(String mod) {
        return Loader.isLoaded(mod);
    }

    /**
     * Opens GUI for use with mods.
     *
     * @param player   Player instance to open GUI for.
     * @param instance Used for identifying which mod this call is for. Allows for passing extra data to GUI.
     * @return GUI that mod created.
     * @see BaseMod#OpenModGUI(EntityPlayerSP, Object)
     */
    public static GuiScreen OpenModGUI(EntityPlayerSP player, Object instance) {
        return Loader.mods()
            .stream()
            .map(mod -> mod.OpenModGUI(player, instance))
            .filter(Objects::nonNull)
            .findFirst().orElse(null);
    }

    /**
     * Used for generating new blocks in the world.
     *
     * @param generator Generator to pair with.
     * @param chunkX    X coordinate of chunk.
     * @param chunkZ    Z coordinate of chunk.
     * @param world     World to generate blocks in.
     * @see BaseMod#GenerateSurface(World, Random, int, int)
     * @see BaseMod#GenerateNether(World, Random, int, int)
     */
    public static void PopulateChunk(IChunkProvider generator, int chunkX, int chunkZ, World world) {
        if (generator instanceof ChunkProviderGenerate) {
            Loader.forEach(mod -> mod.GenerateSurface(world, world.rand, chunkX, chunkZ));
        } else if (generator instanceof ChunkProviderHell) {
            Loader.forEach(mod -> mod.GenerateNether(world, world.rand, chunkX, chunkZ));
        }
    }

    /**
     * Used to register all mod blocks that the client can use.
     *
     * @param registry List of blocks to add to.
     * @see BaseMod#RegisterBlocks(List)
     */
    public static void RegisterAllBlocks(List<Block> registry) {
        Loader.forEach(mod -> mod.RegisterBlocks(registry));
    }

    /**
     * Processes all registered texture overrides, modifies internal buffers.
     *
     * @param texCache Reference to texture cache.
     * @see BaseMod#RegisterTextureOverrides()
     */
    public static void RegisterAllTextureOverrides(RenderEngine texCache) {
        if (texturesOverridden) return;

        texturesOverridden = true;

        Loader.forEach(BaseMod::RegisterTextureOverrides);

        Minecraft minecraft = ModLoaderExt.getInstance();
        Loader.forEach(mod -> mod.RegisterAnimation(minecraft));

        animations.forEach(texCache::registerTextureFX);

        overrides.forEach((path, overlays) -> {
            int id = texCache.getTexture(path);

            IntBuffer sizebuffer = BufferUtils.createIntBuffer(4);

            GL11.glBindTexture(GL11.GL_TEXTURE_2D, id);
            GL11.glGetTexLevelParameter(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_HEIGHT, sizebuffer);

            int texHeight = sizebuffer.get(0);
            int texSize = texHeight / 16;
            for (Pair<String, Integer> overlayEntry : overlays) {
                String overlayPath = overlayEntry.getKey();
                int index = overlayEntry.getValue();

                LogManager.getLogger().info("Overriding " + path + " with " + overlayPath + " @ " + index);
                BufferedImage image = loadImage(overlayPath);
                ByteBuffer buffer = ByteBuffer.allocateDirect(4 * texSize * texSize).order(ByteOrder.nativeOrder());

                int h = image.getHeight();
                int scale = texSize / h;
                if (scale == 0) scale = texSize;

                int[] data = new int[h * h];
                image.getRGB(0, 0, h, h, data, 0, h);

                int y = texSize * (index / 16);
                int x = texSize * (index % 16);

                for (int dy = 0; dy < texSize; dy++) {
                    for (int dx = 0; dx < texSize; dx++) {
                        int argb = data[h * dy / scale + dx / scale];
                        buffer.putInt(argb);
                    }
                }

                buffer.position(0);
                GL11.glTexSubImage2D(GL11.GL_TEXTURE_2D, 0, x, y, texSize, texSize, GL12.GL_BGRA, GL11.GL_UNSIGNED_BYTE, buffer);
            }
        });
    }

    private static BufferedImage loadImage(String path) {
        try {
            TexturePackList pack = ModLoaderExt.getInstance().texturePackList;
            InputStream input = pack.selectedTexturePack.getResourceAsStream(path);

            if (input != null) return ImageIO.read(input);

            throw new RuntimeException("Image not found: " + path);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Place holder for calling the function that adds TileEntities.
     *
     * @see BaseMod#RegisterTileEntity()
     */
    public static void RegisterAllTileEntities() {
        Loader.forEach(BaseMod::RegisterTileEntity);
    }

    /**
     * Runs all registered OSD hooks, run by GUIInGame.
     *
     * @param game instance of the game class.
     * @see BaseMod#OSDHook(Minecraft)
     */
    public static void RunOSDHooks(Minecraft game) {
        Loader.forEach(mod -> mod.OSDHook(game));
    }
}
