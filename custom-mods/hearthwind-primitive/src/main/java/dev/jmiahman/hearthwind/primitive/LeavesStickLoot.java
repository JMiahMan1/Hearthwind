package dev.jmiahman.hearthwind.primitive;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

/**
 * Earlystage / Primitive Parity:
 * 1. Breaking leaves drops sticks reliably (50% chance).
 * 2. Right-clicking or hitting leaves with empty hand forages sticks directly from the foliage.
 */
public final class LeavesStickLoot {
    private static final Map<UUID, Long> lastForageTime = new ConcurrentHashMap<>();

    private LeavesStickLoot() {}

    public static void register() {
        // 1. Loot Table Injection
        LootTableEvents.MODIFY.register((key, table, source, wrapper) -> {
            if (key == null || key.identifier() == null
                    || !key.identifier().getPath().contains("leaves")) {
                return;
            }
            float chance = HearthwindPrimitiveConfig.get().extraStickDropChance;
            if (chance <= 0.0001f) {
                return;
            }
            table.withPool(LootPool.lootPool()
                    .setRolls(ConstantValue.exactly(1))
                    .when(LootItemRandomChanceCondition.randomChance(chance))
                    .add(LootItem.lootTableItem(Items.STICK)));
        });

        // 2. Direct Block Break Stick Drop
        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, be) -> {
            if (!world.isClientSide() && state.is(BlockTags.LEAVES) && world instanceof ServerLevel serverLevel) {
                float chance = HearthwindPrimitiveConfig.get().extraStickDropChance;
                if (serverLevel.getRandom().nextFloat() < chance) {
                    ItemEntity entity = new ItemEntity(serverLevel, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                            new ItemStack(Items.STICK, 1));
                    serverLevel.addFreshEntity(entity);
                }
            }
        });

        // 3. Right-Click Leaf Foraging (Empty Hand on Leaves)
        UseBlockCallback.EVENT.register((player, level, hand, hitResult) -> {
            if (hand != InteractionHand.MAIN_HAND || !player.getItemInHand(hand).isEmpty()) {
                return InteractionResult.PASS;
            }
            BlockPos pos = hitResult.getBlockPos();
            if (level.getBlockState(pos).is(BlockTags.LEAVES)) {
                long now = level.getGameTime();
                Long last = lastForageTime.get(player.getUUID());
                if (last != null && now - last < 20) { // 1 second cooldown
                    return InteractionResult.PASS;
                }
                lastForageTime.put(player.getUUID(), now);

                if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
                    serverLevel.playSound(null, pos, SoundEvents.GRASS_HIT, SoundSource.BLOCKS, 1.0f, 1.0f);
                    if (serverLevel.getRandom().nextFloat() < 0.60f) { // 60% chance to forage a stick
                        ItemEntity entity = new ItemEntity(serverLevel,
                                hitResult.getLocation().x, hitResult.getLocation().y + 0.1, hitResult.getLocation().z,
                                new ItemStack(Items.STICK, 1));
                        serverLevel.addFreshEntity(entity);
                    }
                }
                player.swing(hand, true);
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.PASS;
        });
    }
}
