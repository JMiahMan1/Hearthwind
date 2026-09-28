package draylar.inmis.compat;

import net.fabricmc.loader.api.FabricLoader;
import eu.pb4.trinkets.api.TrinketAttachment;
import eu.pb4.trinkets.api.TrinketsApi;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import draylar.inmis.Inmis;
import draylar.inmis.item.BackpackItem;

/**
 * Read side of the Inmis Addon's Trinkets support: a backpack worn in a
 * Trinkets slot (chest:back is the one that matters) has to be findable so
 * the open-backpack keybind and the on-back render work with it.
 *
 * <p>Upstream's addon used the {@code dev.emi.trinkets} API, but the fork we
 * ship is {@code eu.pb4.trinkets}, so the lookup is expressed against
 * {@link TrinketAttachment#findFirst}. Every call is guarded by a mod-loaded
 * check so a standalone Inmis install never touches the class.
 */
public final class TrinketsCompat {

    private static final boolean TRINKETS_LOADED = FabricLoader.getInstance().isModLoaded("trinkets");

    private TrinketsCompat() {
    }

    /** True when Trinkets is present and the config opts in. */
    public static boolean isEnabled() {
        return TRINKETS_LOADED && Inmis.CONFIG.enableTrinketCompatibility;
    }

    /**
     * The first backpack equipped in a Trinkets slot, or null. Unlike
     * upstream's {@code getEquipped(...).get(0)} this is null safe.
     */
    @Nullable
    public static ItemStack findEquippedBackpack(LivingEntity entity) {
        if (!isEnabled()) {
            return null;
        }

        return findBackpack(TrinketsApi.getAttachment(entity));
    }

    /**
     * The first backpack equipped in a Trinkets slot, or null. The predicate
     * looks at the stack in the slot rather than at the slot type, so any
     * slot a player can put a backpack into counts.
     */
    @Nullable
    public static ItemStack findBackpack(TrinketAttachment attachment) {
        if (attachment == null) {
            return null;
        }

        return attachment
                .findFirst(stack -> stack.getItem() instanceof BackpackItem)
                .map(slot -> slot.get())
                .filter(stack -> !stack.isEmpty())
                .orElse(null);
    }
}
