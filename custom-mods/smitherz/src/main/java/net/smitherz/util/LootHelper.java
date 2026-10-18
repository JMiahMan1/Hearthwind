package net.smitherz.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.smitherz.SmitherzMain;
import net.smitherz.init.ConfigInit;

public class LootHelper {
   private static final boolean isRpgDifficultyLoaded = FabricLoader.getInstance().isModLoaded("rpgdifficulty");

   public static void dropGemLoot(LivingEntity livingEntity, DamageSource damageSource, boolean causedByPlayer) {
      if (ConfigInit.CONFIG.mobsCanDropGems && causedByPlayer && livingEntity instanceof Mob mobEntity) {
         if (SmitherzMain.gemDropMap.containsKey(mobEntity.getType())) {
            int rarity = 0;
            int maxValue = 0;

            for (int value : SmitherzMain.gemDropMap.get(mobEntity.getType()).keySet()) {
               if (value > maxValue) {
                  maxValue = value;
               }
            }

            rarity = UpgradeHelper.skewedRandomInt(maxValue);
            if (SmitherzMain.gemDropMap.get(mobEntity.getType()).containsKey(rarity)) {
               Map<net.minecraft.world.item.Item, Float> itemChance = SmitherzMain.gemDropMap.get(mobEntity.getType()).get(rarity);
               List<Entry<net.minecraft.world.item.Item, Float>> entryList = new ArrayList<>(itemChance.entrySet());
               Entry<net.minecraft.world.item.Item, Float> randomEntry = entryList.get(UpgradeHelper.RANDOM.nextInt(itemChance.size()));
               if (UpgradeHelper.RANDOM.nextFloat() <= randomEntry.getValue()) {
                  livingEntity.spawnAtLocation((net.minecraft.server.level.ServerLevel)livingEntity.level(), new ItemStack((ItemLike)randomEntry.getKey()));
               }
            }
         }
      }
   }
}
