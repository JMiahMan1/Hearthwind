package com.starfish_studios.another_furniture.registry;

import com.starfish_studios.another_furniture.registry.fabric.AFRegistryImpl;
import java.util.Collection;
import java.util.function.Supplier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.NotNull;

public class AFRegistry {
   public static <T extends net.minecraft.world.level.block.Block> Supplier<T> registerBlock(String name, Supplier<T> block) {
      return AFRegistryImpl.registerBlock(name, block);
   }
   public static <T extends net.minecraft.world.item.Item> Supplier<T> registerItem(String name, Supplier<T> item, String tab_id) {
      return AFRegistryImpl.registerItem(name, item, tab_id);
   }
   public static <T extends net.minecraft.sounds.SoundEvent> Supplier<T> registerSoundEvent(String name, Supplier<T> soundEvent) {
      return AFRegistryImpl.registerSoundEvent(name, soundEvent);
   }
   public static <T extends net.minecraft.world.entity.Entity> Supplier<net.minecraft.world.entity.EntityType<T>> registerEntityType(
      String name,
      net.minecraft.world.entity.EntityType.EntityFactory<T> factory,
      net.minecraft.world.entity.MobCategory category,
      float width,
      float height
   ) {
      return AFRegistryImpl.registerEntityType(name, factory, category, width, height);
   }
   public static <T extends net.minecraft.world.level.block.entity.BlockEntityType<E>, E extends net.minecraft.world.level.block.entity.BlockEntity> Supplier<T> registerBlockEntityType(
      String name, Supplier<T> blockEntity
   ) {
      return AFRegistryImpl.registerBlockEntityType(name, blockEntity);
   }
   public static <T extends net.minecraft.world.level.block.entity.BlockEntity> net.minecraft.world.level.block.entity.BlockEntityType<T> createBlockEntityType(
      AFRegistry.BlockEntitySupplier<T> blockEntity, net.minecraft.world.level.block.Block... validBlocks
   ) {
      return AFRegistryImpl.createBlockEntityType(blockEntity, validBlocks);
   }
   public static <T extends net.minecraft.world.entity.Entity, S extends net.minecraft.client.renderer.entity.state.EntityRenderState> void registerEntityRenderers(
      Supplier<net.minecraft.world.entity.EntityType<T>> type, net.minecraft.client.renderer.entity.EntityRendererProvider<T> renderProvider
   ) {
      AFRegistryImpl.registerEntityRenderers(type, renderProvider);
   }
   public static <T extends net.minecraft.world.level.block.entity.BlockEntity, S extends net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState> void registerBlockEntityRenderer(
      Supplier<net.minecraft.world.level.block.entity.BlockEntityType<T>> type,
      net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider<T, S> renderProvider
   ) {
      AFRegistryImpl.registerBlockEntityRenderer(type, renderProvider);
   }

   public static <T extends net.minecraft.world.level.block.Block> void setFlammable(Supplier<T> block, int encouragement, int flammability) {
      setFlammable(Blocks.FIRE, block, encouragement, flammability);
   }
   public static <T extends net.minecraft.world.level.block.Block> void setFlammable(
      net.minecraft.world.level.block.Block fireBlock, Supplier<T> block, int encouragement, int flammability
   ) {
      AFRegistryImpl.setFlammable(fireBlock, block, encouragement, flammability);
   }
   public static boolean isModLoaded(String mod) {
      return AFRegistryImpl.isModLoaded(mod);
   }
   public static boolean isFakePlayer(Player player) {
      return AFRegistryImpl.isFakePlayer(player);
   }
   public static Collection<net.minecraft.world.item.ItemStack> getAllModItems() {
      return AFRegistryImpl.getAllModItems();
   }

   @FunctionalInterface
   public interface BlockEntitySupplier<T extends net.minecraft.world.level.block.entity.BlockEntity> {
      @NotNull
      T create(net.minecraft.core.BlockPos var1, net.minecraft.world.level.block.state.BlockState var2);
   }
}
