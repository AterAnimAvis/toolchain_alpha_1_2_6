package net.minecraft.src;

import java.util.List;

import net.minecraft.src.modloader.BaseModExt;
import net.minecraft.src.modloader.SpriteHandler;

import static net.minecraft.src.Block.soundStoneFootstep;

public class mod_Example extends BaseMod implements BaseModExt {

    private static final Block burnt_stone = new Block(100, Material.rock).setHardness(1.5F).getCanBlockGrass(10.0F).setStepSound(soundStoneFootstep);

    static {
        burnt_stone.blockIndexInTexture = ModLoader.getUniqueSpriteIndex(SpriteHandler.TERRAIN);
    }

    @Override
    public int AddSmelting(int id) {
        if (id == Block.stone.blockID) return burnt_stone.blockID;

        return -1;
    }

    @Override
    public void RegisterBlocks(List<Block> registry) {
        registry.add(burnt_stone);
    }

    @Override
    public void RegisterTextureOverrides() {
        ModLoader.addOverride(SpriteHandler.TERRAIN, "/example_mod/burnt_stone.png", burnt_stone.blockIndexInTexture);
    }
}
