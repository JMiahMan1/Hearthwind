package net.dungeonz.util;

import java.util.List;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.server.MinecraftServer;

public class InventoryHelper {

    public static void fillInventoryWithLoot(MinecraftServer server, ServerLevel world, BlockPos pos, String lootTableString) {
        // Clear inventory
        ((Container) world.getBlockEntity(pos)).clearContent();
        // Generate loot
        LootTable lootTable = server.reloadableRegistries().getLootTable(ResourceKey.create(Registries.LOOT_TABLE, Identifier.parse(lootTableString)));
        LootParams.Builder builder = new LootParams.Builder(world).withParameter(LootContextParams.ORIGIN, new Vec3(pos.getX(), pos.getY(), pos.getZ()));
        lootTable.fill((Container) world.getBlockEntity(pos), builder.create(LootContextParamSets.CHEST), world.getRandom().nextLong());
    }

    public static boolean hasRequiredItemStacks(Inventory playerInventory, List<ItemStack> requiredItemStacks) {
        if (playerInventory.player.isCreative()) {
            return true;
        }
        for (int i = 0; i < requiredItemStacks.size(); i++) {
            int requiredCount = requiredItemStacks.get(i).getCount();
            for (int u = 0; u < playerInventory.getNonEquipmentItems().size(); u++) {
                if (ItemStack.isSameItem(playerInventory.getNonEquipmentItems().get(u), requiredItemStacks.get(i))) {
                    requiredCount -= playerInventory.getNonEquipmentItems().get(u).getCount();
                    if (requiredCount <= 0) {
                        break;
                    }
                }
            }
            if (requiredCount > 0) {
                return false;
            }
        }
        return true;
    }

    public static void decrementRequiredItemStacks(Inventory playerInventory, List<ItemStack> requiredItemStacks) {
        if (!requiredItemStacks.isEmpty() && !playerInventory.player.isCreative()) {
            for (int i = 0; i < requiredItemStacks.size(); i++) {
                int requiredCount = requiredItemStacks.get(i).getCount();
                for (int u = 0; u < playerInventory.getNonEquipmentItems().size(); u++) {
                    if (ItemStack.isSameItem(playerInventory.getNonEquipmentItems().get(u), requiredItemStacks.get(i))) {
                        if (playerInventory.getNonEquipmentItems().get(u).getCount() >= requiredCount) {
                            playerInventory.getNonEquipmentItems().get(u).shrink(requiredCount);
                            break;
                        }
                        requiredCount -= playerInventory.getNonEquipmentItems().get(u).getCount();
                        playerInventory.getNonEquipmentItems().get(u).setCount(0);

                        if (requiredCount <= 0) {
                            break;
                        }
                    }
                }
            }
        }
    }

}
