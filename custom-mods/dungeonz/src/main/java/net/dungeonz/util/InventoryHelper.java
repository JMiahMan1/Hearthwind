package net.dungeonz.util;

import java.util.List;
import net.minecraft.class_1263;
import net.minecraft.class_1661;
import net.minecraft.class_173;
import net.minecraft.class_1799;
import net.minecraft.class_181;
import net.minecraft.class_2338;
import net.minecraft.class_243;
import net.minecraft.class_2960;
import net.minecraft.class_3218;
import net.minecraft.class_52;
import net.minecraft.class_5321;
import net.minecraft.class_7924;
import net.minecraft.class_8567;
import net.minecraft.server.MinecraftServer;

public class InventoryHelper {

    public static void fillInventoryWithLoot(MinecraftServer server, class_3218 world, class_2338 pos, String lootTableString) {
        // Clear inventory
        ((class_1263) world.method_8321(pos)).method_5448();
        // Generate loot
        class_52 lootTable = server.method_58576().method_58295(class_5321.method_29179(class_7924.field_50079, class_2960.method_60654(lootTableString)));
        class_8567.class_8568 builder = new class_8567.class_8568(world).method_51874(class_181.field_24424, new class_243(pos.method_10263(), pos.method_10264(), pos.method_10260()));
        lootTable.method_329((class_1263) world.method_8321(pos), builder.method_51875(class_173.field_1179), world.method_8409().method_43055());
    }

    public static boolean hasRequiredItemStacks(class_1661 playerInventory, List<class_1799> requiredItemStacks) {
        if (playerInventory.field_7546.method_7337()) {
            return true;
        }
        for (int i = 0; i < requiredItemStacks.size(); i++) {
            int requiredCount = requiredItemStacks.get(i).method_7947();
            for (int u = 0; u < playerInventory.field_7547.size(); u++) {
                if (class_1799.method_7984(playerInventory.field_7547.get(u), requiredItemStacks.get(i))) {
                    requiredCount -= playerInventory.field_7547.get(u).method_7947();
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

    public static void decrementRequiredItemStacks(class_1661 playerInventory, List<class_1799> requiredItemStacks) {
        if (!requiredItemStacks.isEmpty() && !playerInventory.field_7546.method_7337()) {
            for (int i = 0; i < requiredItemStacks.size(); i++) {
                int requiredCount = requiredItemStacks.get(i).method_7947();
                for (int u = 0; u < playerInventory.field_7547.size(); u++) {
                    if (class_1799.method_7984(playerInventory.field_7547.get(u), requiredItemStacks.get(i))) {
                        if (playerInventory.field_7547.get(u).method_7947() >= requiredCount) {
                            playerInventory.field_7547.get(u).method_7934(requiredCount);
                            break;
                        }
                        requiredCount -= playerInventory.field_7547.get(u).method_7947();
                        playerInventory.field_7547.get(u).method_7939(0);

                        if (requiredCount <= 0) {
                            break;
                        }
                    }
                }
            }
        }
    }

}
