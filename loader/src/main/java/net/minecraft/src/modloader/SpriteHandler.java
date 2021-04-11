package net.minecraft.src.modloader;

import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;

public class SpriteHandler {

    private static final Map<String, BitSet> usedSprites = new HashMap<>();

    public static final String TERRAIN = "/terrain.png";
    public static final String ITEMS = "/gui/items.png";

    static {
        usedSprites.put(ITEMS, BitSet.valueOf(new long[] {-8070556086169509889L, 8727506817335295L, 549764202911L, 844424930131968L}));
        usedSprites.put(TERRAIN, BitSet.valueOf(new long[] {4611615648609468415L, 128638462403219455L, 1L, -4323490823426482176L}));
    }

    public static int getUniqueSpriteIndex(String path) {
        BitSet set = usedSprites.computeIfAbsent(path, (k) -> new BitSet(256));
        int index = set.nextClearBit(0);

        if (index > 255 || index < 0) return -1;

        set.set(index);

        return index;
    }

}
