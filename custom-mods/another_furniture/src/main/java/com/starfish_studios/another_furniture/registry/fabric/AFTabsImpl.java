package com.starfish_studios.another_furniture.registry.fabric;

import com.starfish_studios.another_furniture.AnotherFurniture;
import com.starfish_studios.another_furniture.registry.AFBlocks;
import com.starfish_studios.another_furniture.registry.AFRegistry;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

public class AFTabsImpl {
   public static final CreativeModeTab AFM_TAB = (CreativeModeTab)Registry.register(
      BuiltInRegistries.CREATIVE_MODE_TAB,
      AnotherFurniture.res("tab"),
      FabricCreativeModeTab.builder()
         .title(Component.translatable("item_group.another_furniture.tab"))
         .icon(() -> new ItemStack(((Block)AFBlocks.OAK_CHAIR.get()).asItem()))
         .displayItems((parameters, output) -> output.acceptAll(AFRegistry.getAllModItems()))
         .build()
   );

   public static void register() {
   }
}
