package com.starfish_studios.another_furniture.registry.fabric;

import com.starfish_studios.another_furniture.AnotherFurniture;
import com.starfish_studios.another_furniture.registry.AFRegistry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityType.Builder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class AFRegistryImpl {
   static List<Supplier<? extends net.minecraft.world.item.Item>> itemList = new ArrayList<>();

   public static <T extends net.minecraft.world.level.block.Block> Supplier<T> registerBlock(String name, Supplier<T> block) {
      T registry = (T)((Block)Registry.register(BuiltInRegistries.BLOCK, AnotherFurniture.res(name), (Block)block.get()));
      return () -> registry;
   }

   public static <T extends net.minecraft.world.item.Item> Supplier<T> registerItem(String name, Supplier<T> item, String tab_id) {
      T registry = (T)((Item)Registry.register(BuiltInRegistries.ITEM, AnotherFurniture.res(name), (Item)item.get()));
      itemList.add(() -> registry);
      return () -> registry;
   }

   public static <T extends net.minecraft.sounds.SoundEvent> Supplier<T> registerSoundEvent(String name, Supplier<T> soundEvent) {
      T registry = (T)((SoundEvent)Registry.register(BuiltInRegistries.SOUND_EVENT, AnotherFurniture.res(name), (SoundEvent)soundEvent.get()));
      return () -> registry;
   }

   public static <T extends net.minecraft.world.entity.Entity> Supplier<net.minecraft.world.entity.EntityType<T>> registerEntityType(
      String name,
      net.minecraft.world.entity.EntityType.EntityFactory<T> factory,
      net.minecraft.world.entity.MobCategory category,
      float width,
      float height
   ) {
      net.minecraft.world.entity.EntityType<T> registry = (EntityType)Registry.register(
         BuiltInRegistries.ENTITY_TYPE, AnotherFurniture.res(name), Builder.of(factory, category).sized(width, height).build(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ENTITY_TYPE, AnotherFurniture.res(name)))
      );
      return () -> registry;
   }

   public static <T extends net.minecraft.world.level.block.entity.BlockEntityType<E>, E extends net.minecraft.world.level.block.entity.BlockEntity> Supplier<T> registerBlockEntityType(
      String name, Supplier<T> blockEntity
   ) {
      T registry = (T)((BlockEntityType)Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, AnotherFurniture.res(name), (BlockEntityType)blockEntity.get()));
      return () -> registry;
   }

   public static <T extends net.minecraft.world.level.block.entity.BlockEntity> net.minecraft.world.level.block.entity.BlockEntityType<T> createBlockEntityType(
      AFRegistry.BlockEntitySupplier<T> blockEntity, net.minecraft.world.level.block.Block... validBlocks
   ) {
      return new net.minecraft.world.level.block.entity.BlockEntityType<>(blockEntity::create, java.util.Set.of(validBlocks));
   }

   public static <T extends net.minecraft.world.entity.Entity, S extends net.minecraft.client.renderer.entity.state.EntityRenderState> void registerEntityRenderers(
      Supplier<net.minecraft.world.entity.EntityType<T>> type, net.minecraft.client.renderer.entity.EntityRendererProvider<T> renderProvider
   ) {
      EntityRendererRegistry.register((EntityType)type.get(), renderProvider);
   }

   public static <T extends net.minecraft.world.level.block.entity.BlockEntity, S extends net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState> void registerBlockEntityRenderer(
      Supplier<net.minecraft.world.level.block.entity.BlockEntityType<T>> type,
      net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider<T, S> renderProvider
   ) {
      BlockEntityRenderers.register((BlockEntityType)type.get(), renderProvider);
   }

   public static <T extends net.minecraft.world.level.block.Block> void setFlammable(
      net.minecraft.world.level.block.Block fireBlock, Supplier<T> block, int encouragement, int flammability
   ) {
      FlammableBlockRegistry.getInstance(fireBlock).add((Block)block.get(), encouragement, flammability);
   }

   public static boolean isModLoaded(String mod) {
      return FabricLoader.getInstance().isModLoaded(mod);
   }

   public static boolean isFakePlayer(Player player) {
      return player instanceof ServerPlayer && player.getClass() != ServerPlayer.class;
   }

   public static Collection<net.minecraft.world.item.ItemStack> getAllModItems() {
      List<net.minecraft.world.item.ItemStack> stacks = new ArrayList<>();
      for (Supplier<? extends net.minecraft.world.item.Item> supplier : itemList) {
         stacks.add(supplier.get().getDefaultInstance());
      }
      return stacks;
   }
}
