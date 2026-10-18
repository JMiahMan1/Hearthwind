package com.starfish_studios.another_furniture.registry;

import com.starfish_studios.another_furniture.block.entity.DrawerBlockEntity;
import com.starfish_studios.another_furniture.block.entity.FlowerBoxBlockEntity;
import com.starfish_studios.another_furniture.block.entity.ServiceBellBlockEntity;
import com.starfish_studios.another_furniture.block.entity.ShelfBlockEntity;
import java.util.function.Supplier;
import net.minecraft.world.level.block.Block;

public class AFBlockEntityTypes {
   public static final Supplier<net.minecraft.world.level.block.entity.BlockEntityType<ShelfBlockEntity>> SHELF = AFRegistry.registerBlockEntityType(
      "shelf",
      () -> AFRegistry.createBlockEntityType(
         ShelfBlockEntity::new,
         (Block)AFBlocks.OAK_SHELF.get(),
         (Block)AFBlocks.SPRUCE_SHELF.get(),
         (Block)AFBlocks.BIRCH_SHELF.get(),
         (Block)AFBlocks.JUNGLE_SHELF.get(),
         (Block)AFBlocks.ACACIA_SHELF.get(),
         (Block)AFBlocks.DARK_OAK_SHELF.get(),
         (Block)AFBlocks.MANGROVE_SHELF.get(),
         (Block)AFBlocks.CRIMSON_SHELF.get(),
         (Block)AFBlocks.WARPED_SHELF.get(),
         (Block)AFBlocks.BAMBOO_SHELF.get(),
         (Block)AFBlocks.CHERRY_SHELF.get()
      )
   );
   public static final Supplier<net.minecraft.world.level.block.entity.BlockEntityType<ServiceBellBlockEntity>> SERVICE_BELL = AFRegistry.registerBlockEntityType(
      "service_bell", () -> AFRegistry.createBlockEntityType(ServiceBellBlockEntity::new, (Block)AFBlocks.SERVICE_BELL.get())
   );
   public static final Supplier<net.minecraft.world.level.block.entity.BlockEntityType<FlowerBoxBlockEntity>> FLOWER_BOX = AFRegistry.registerBlockEntityType(
      "planter_box",
      () -> AFRegistry.createBlockEntityType(
         FlowerBoxBlockEntity::new,
         (Block)AFBlocks.OAK_FLOWER_BOX.get(),
         (Block)AFBlocks.SPRUCE_FLOWER_BOX.get(),
         (Block)AFBlocks.BIRCH_FLOWER_BOX.get(),
         (Block)AFBlocks.JUNGLE_FLOWER_BOX.get(),
         (Block)AFBlocks.ACACIA_FLOWER_BOX.get(),
         (Block)AFBlocks.DARK_OAK_FLOWER_BOX.get(),
         (Block)AFBlocks.MANGROVE_FLOWER_BOX.get(),
         (Block)AFBlocks.CRIMSON_FLOWER_BOX.get(),
         (Block)AFBlocks.WARPED_FLOWER_BOX.get(),
         (Block)AFBlocks.BAMBOO_FLOWER_BOX.get(),
         (Block)AFBlocks.CHERRY_FLOWER_BOX.get()
      )
   );
   public static final Supplier<net.minecraft.world.level.block.entity.BlockEntityType<DrawerBlockEntity>> DRAWER = AFRegistry.registerBlockEntityType(
      "drawer",
      () -> AFRegistry.createBlockEntityType(
         DrawerBlockEntity::new,
         (Block)AFBlocks.OAK_DRAWER.get(),
         (Block)AFBlocks.SPRUCE_DRAWER.get(),
         (Block)AFBlocks.BIRCH_DRAWER.get(),
         (Block)AFBlocks.JUNGLE_DRAWER.get(),
         (Block)AFBlocks.ACACIA_DRAWER.get(),
         (Block)AFBlocks.DARK_OAK_DRAWER.get(),
         (Block)AFBlocks.MANGROVE_DRAWER.get(),
         (Block)AFBlocks.CRIMSON_DRAWER.get(),
         (Block)AFBlocks.WARPED_DRAWER.get(),
         (Block)AFBlocks.BAMBOO_DRAWER.get(),
         (Block)AFBlocks.CHERRY_DRAWER.get()
      )
   );

   public static void init() {
   }
}
