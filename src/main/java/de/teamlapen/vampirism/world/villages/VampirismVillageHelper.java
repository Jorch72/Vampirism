package de.teamlapen.vampirism.world.villages;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.village.Village;
import net.minecraft.world.World;

import javax.annotation.Nullable;


public class VampirismVillageHelper {

    /**
     * Finds the nearest village, but only the given coordinates are withing it's bounding box plus the given the distance.
     */
    public static @Nullable
    VampirismVillageOld getNearestVillage(World w, BlockPos pos, int r) {
        Village v = w.villageCollection.getNearestVillage(pos, r);
        if (v != null) {
            return VampirismVillageOld.get(v);
        }
        return null;
    }

    /**
     * @return The nearest village the entity is in or next to.
     */
    public static @Nullable
    VampirismVillageOld getNearestVillage(Entity e) {
        return getNearestVillage(e.getEntityWorld(), e.getPosition(), 10);
    }

    /**
     * Tick all Vampirism Villages
     */
    public static void tick(World w) {
        for (Village v : w.villageCollection.getVillageList()) {
            VampirismVillageOld.get(v).tick(w.getTotalWorldTime());
        }
    }

}
