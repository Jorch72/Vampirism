package de.teamlapen.vampirism.api.general;

import de.teamlapen.vampirism.api.VReference;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Store blood conversion factors.
 * Item -> impure blood.
 * Liquids -> blood
 */
public class BloodConversionRegistry {
    private static final Map<Item, Integer> blood_items = new HashMap<>();
    private static final Map<Item, Function<ItemStack, Integer>> blood_item_special = new HashMap<>();
    private static final Map<String, Float> fluids = new HashMap<>();

    /**
     * Get the amount of impure blood the given item is worth.
     * Checks in the following order:
     * Registered Item functions
     * Registered Item values
     *
     * @param item ItemStack
     * @return Impure blood amount in mB or 0
     */
    public static int getImpureBloodValue(@Nonnull ItemStack item) {
        if (blood_item_special.containsKey(item.getItem())) {
            return blood_item_special.get(item.getItem()).apply(item);
        } else if (blood_items.containsKey(item.getItem())) {
            return blood_items.get(item.getItem());
        }
        return 0;
    }

    public static float getFluidBloodConversionFactor(String fluidname) {
        if (fluids.containsKey(fluidname)) {
            return fluids.get(fluidname);
        }
        return 0f;
    }

    public static int getFluidBloodEquivalent(String fluid, int amount) {
        return (int) (getFluidBloodConversionFactor(fluid) * amount);
    }

    @Nullable
    public static FluidStack getFluidBloodEquivalent(FluidStack fluidStack) {
        String name = fluidStack.getFluid().getName();
        int conv = getFluidBloodEquivalent(name, fluidStack.amount);
        if (conv == 0) {
            return null;
        }
        return new FluidStack(FluidRegistry.getFluid(VReference.FLUID_BLOOD_NAME), conv);
    }

    /**
     * Registers the conversion ratio between the given fluid and Vampirism's blood {@link VReference#FLUID_BLOOD_NAME}
     * OldFluid * ration = Blood
     *
     * @param fluidname The registered fluid name
     * @param ratio
     */
    public static void registerFluidConversionRatio(String fluidname, float ratio) {
        fluids.put(fluidname, ratio);
    }

    /**
     * Register the impure blood amount for the given item.
     * This affects any stack of the given item regardless of meta or NBT
     * For different meta values or advanced items (e.g. NBT) check {@link #registerItem(Item, Function)}
     *
     * @param amount Impure blood amount in mB
     */
    public static void registerItem(Item item, int amount) {
        blood_items.put(item, amount);
    }

    /**
     * Register an impure blood amount function for the given item.
     * This affects any stack of the given item.
     *
     * @param function Function that returns the appropriate amount of blood. Can return 0
     */
    public static void registerItem(Item item, Function<ItemStack, Integer> function) {
        blood_item_special.put(item, function);
    }
}
