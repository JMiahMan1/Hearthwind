package net.mehvahdjukaar.smarterfarmers.fabric;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.CommonLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.CommonLifecycleEvents.TagsLoaded;
import net.mehvahdjukaar.smarterfarmers.SmarterFarmers;
import net.mehvahdjukaar.smarterfarmers.mixins.WorkAtComposterAccessor;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.ComposterBlock;

public class SmarterFarmersFabric implements ModInitializer {
   private static boolean hasInitialized = false;

   public void onInitialize() {
      SmarterFarmers.commonInit();
      CommonLifecycleEvents.TAGS_LOADED.register((TagsLoaded)(resourceManager, tagManager) -> {
         SmarterFarmers.setupWithTags();
         if (!hasInitialized) {
            hasInitialized = true;
            Builder<net.minecraft.world.item.Item> builder = ImmutableList.builder();
            builder.addAll(WorkAtComposterAccessor.getCompostableItems());
            ObjectIterator var3 = ComposterBlock.COMPOSTABLES.keySet().iterator();

            while (var3.hasNext()) {
               ItemLike entry = (ItemLike)var3.next();
               if (!Villager.FOOD_POINTS.containsKey(entry.asItem())) {
                  builder.add(entry.asItem());
               }
            }

            WorkAtComposterAccessor.setCompostableItems(builder.build());
         }
      });
   }
}
