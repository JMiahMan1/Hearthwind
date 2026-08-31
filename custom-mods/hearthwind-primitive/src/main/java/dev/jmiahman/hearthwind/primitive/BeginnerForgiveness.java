package dev.jmiahman.hearthwind.primitive;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.mojang.serialization.Codec;

import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

/**
 * Beginner-death forgiveness: new players can die up to
 * {@code beginnerDeathCount} times (default 3) without losing items.
 *
 * On each death within the count:
 *  - Full inventory (armor, hotbar, offhand, main) is saved before death drops
 *  - Inventory is cleanly restored into identical slots upon respawn
 *
 * The forgiveness count resets when the player:
 *  - Eats any food (completes a meal)
 *  - Sleeps in a bed
 */
public final class BeginnerForgiveness {

    public static final AttachmentType<Integer> DEATH_COUNT =
            AttachmentRegistry.<Integer>builder()
                    .persistent(Codec.INT)
                    .buildAndRegister(
                            Identifier.fromNamespaceAndPath("earlystage", "beginner_deaths"));

    /** Saved inventories awaiting return after forgiveness death. */
    private static final Map<UUID, ItemStack[]> SAVED_INVENTORIES = new ConcurrentHashMap<>();

    private BeginnerForgiveness() {}

    public static void register() {
        // Intercept lethal damage BEFORE vanilla dropAllDeathLoot() spills inventory
        ServerLivingEntityEvents.ALLOW_DEATH.register((entity, damageSource, damageAmount) -> {
            if (entity instanceof ServerPlayer player && shouldForgive(player)) {
                incrementDeathCount(player);
                Inventory inv = player.getInventory();
                UUID id = player.getUUID();
                ItemStack[] saved = new ItemStack[inv.getContainerSize()];
                for (int i = 0; i < inv.getContainerSize(); i++) {
                    saved[i] = inv.getItem(i).copy();
                }
                SAVED_INVENTORIES.put(id, saved);
                // Clear inventory so nothing spills onto the ground or burns in lava/void
                inv.clearContent();

                HearthwindPrimitiveConfig cfg = HearthwindPrimitiveConfig.get();
                int count = getDeathCount(player);
                player.sendSystemMessage(Component.literal(
                        "Beginner forgiveness: death " + count + "/" + cfg.beginnerDeathCount
                                + " — your items will be restored on respawn."));
            }
            return true;
        });

        // Restore items upon respawn
        ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) -> {
            if (!alive) {
                restoreSavedInventory(oldPlayer.getUUID(), newPlayer);
            }
        });

        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            restoreSavedInventory(oldPlayer.getUUID(), newPlayer);
        });
    }

    /**
     * Restore saved items to player inventory.
     */
    public static void restoreSavedInventory(UUID oldPlayerId, ServerPlayer newPlayer) {
        ItemStack[] saved = SAVED_INVENTORIES.remove(oldPlayerId);
        if (saved == null) {
            saved = SAVED_INVENTORIES.remove(newPlayer.getUUID());
        }
        if (saved != null) {
            Inventory newInv = newPlayer.getInventory();
            for (int i = 0; i < saved.length && i < newInv.getContainerSize(); i++) {
                if (saved[i] != null && !saved[i].isEmpty()) {
                    newInv.setItem(i, saved[i]);
                }
            }
            newPlayer.sendSystemMessage(Component.literal(
                    "Your items have been restored from beginner forgiveness."));
        }
    }

    /**
     * Get the current beginner death count for a player.
     */
    public static int getDeathCount(Entity entity) {
        return entity.getAttachedOrElse(DEATH_COUNT, 0);
    }

    /**
     * Increment the beginner death count.
     */
    public static void incrementDeathCount(Entity entity) {
        int count = getDeathCount(entity) + 1;
        entity.setAttached(DEATH_COUNT, count);
    }

    /**
     * Check if a player death should be forgiven.
     */
    public static boolean shouldForgive(ServerPlayer player) {
        HearthwindPrimitiveConfig cfg = HearthwindPrimitiveConfig.get();
        int count = getDeathCount(player);
        return count < cfg.beginnerDeathCount;
    }

    /**
     * Reset forgiveness count when player eats or sleeps.
     */
    public static void onMeal(ServerPlayer player) {
        resetCount(player);
    }

    public static void onSleep(ServerPlayer player) {
        resetCount(player);
    }

    private static void resetCount(ServerPlayer player) {
        player.setAttached(DEATH_COUNT, 0);
        SAVED_INVENTORIES.remove(player.getUUID());
    }
}
