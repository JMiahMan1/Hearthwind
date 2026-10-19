package io.github.phantomloader.library.registry;

import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.apache.commons.lang3.function.TriFunction;

public abstract class ModRegistry {
   public final String mod;

   public static ModRegistry instantiate(String mod) {
      return ServiceLoader.load(RegistryProvider.class)
         .findFirst()
         .orElseThrow(
            () -> new NoSuchElementException(
               "No registry has been defined in META-INF/services. Make sure you are using the correct version of the library mod for your mod loader."
            )
         )
         .instantiate(mod);
   }

   public ModRegistry(String mod) {
      this.mod = mod;
   }

   protected ResourceKey<net.minecraft.world.item.Item> itemKey(String name) {
      return ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(this.mod, name));
   }

   protected ResourceKey<net.minecraft.world.level.block.Block> blockKey(String name) {
      return ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(this.mod, name));
   }

   public abstract <T extends net.minecraft.world.item.Item> Supplier<T> registerItem(String var1, Supplier<T> var2);

   public Supplier<net.minecraft.world.item.Item> registerItem(String name, net.minecraft.world.item.Item.Properties properties) {
      return this.registerItem(name, (Supplier<net.minecraft.world.item.Item>)(() -> new Item(properties.setId(this.itemKey(name)))));
   }

   public Supplier<net.minecraft.world.item.Item> registerItem(String name) {
      return this.registerItem(name, (Supplier<net.minecraft.world.item.Item>)(() -> new Item(new Properties().setId(this.itemKey(name)))));
   }

   public <T extends net.minecraft.world.item.Item> Supplier<T> registerItem(String name, Function<Properties, T> factory) {
      return this.registerItem(name, (Supplier<T>)(() -> (T)factory.apply(new Properties().setId(this.itemKey(name)))));
   }

   public abstract <T extends net.minecraft.world.level.block.Block> Supplier<T> registerBlock(String var1, Supplier<T> var2);

   public Supplier<net.minecraft.world.level.block.Block> registerBlock(
      String name, net.minecraft.world.level.block.state.BlockBehaviour.Properties properties
   ) {
      return this.registerBlock(name, (Supplier<net.minecraft.world.level.block.Block>)(() -> new Block(properties.setId(this.blockKey(name)))));
   }

   public <T extends net.minecraft.world.level.block.Block> Supplier<T> registerBlock(
      String name, Function<net.minecraft.world.level.block.state.BlockBehaviour.Properties, T> factory
   ) {
      return this.registerBlock(name, (Supplier<T>)(() -> (T)factory.apply(
         net.minecraft.world.level.block.state.BlockBehaviour.Properties.of().setId(this.blockKey(name))
      )));
   }

   public Supplier<net.minecraft.world.level.block.Block> registerBlock(String name) {
      return this.registerBlock(name, (Supplier<net.minecraft.world.level.block.Block>)(() -> new Block(
         net.minecraft.world.level.block.state.BlockBehaviour.Properties.of().setId(this.blockKey(name))
      )));
   }

   public Supplier<net.minecraft.world.item.BlockItem> registerBlockItem(String name, Supplier<? extends net.minecraft.world.level.block.Block> block) {
      return this.registerItem(name, (Supplier)(() -> new BlockItem((Block)block.get(), new Properties().setId(this.itemKey(name)).useBlockDescriptionPrefix())));
   }

   public <T extends net.minecraft.world.level.block.Block> Supplier<T> registerBlockAndItem(String name, Supplier<T> block) {
      Supplier<T> registered = this.registerBlock(name, block);
      this.registerBlockItem(name, registered);
      return registered;
   }

   public Supplier<net.minecraft.world.level.block.Block> registerBlockAndItem(
      String name, net.minecraft.world.level.block.state.BlockBehaviour.Properties properties
   ) {
      return this.registerBlockAndItem(name, (Supplier<net.minecraft.world.level.block.Block>)(() -> new Block(properties.setId(this.blockKey(name)))));
   }

   public Supplier<net.minecraft.world.level.block.Block> registerBlockAndItem(String name) {
      return this.registerBlockAndItem(name, net.minecraft.world.level.block.state.BlockBehaviour.Properties.of());
   }

   public <T extends net.minecraft.world.level.block.Block> Supplier<T> registerBlockAndItem(
      String name, Function<net.minecraft.world.level.block.state.BlockBehaviour.Properties, T> factory
   ) {
      Supplier<T> registered = this.registerBlock(name, factory);
      this.registerBlockItem(name, registered);
      return registered;
   }

   public Supplier<net.minecraft.world.level.block.Block> registerBlockVariant(
      String name, Supplier<? extends net.minecraft.world.level.block.Block> base
   ) {
      return this.registerBlock(
         name,
         (Supplier<net.minecraft.world.level.block.Block>)(() -> new Block(
            net.minecraft.world.level.block.state.BlockBehaviour.Properties.ofFullCopy((BlockBehaviour)base.get())
         ))
      );
   }

   public <T extends net.minecraft.world.level.block.Block> Supplier<T> registerBlockVariant(
      String name,
      Function<net.minecraft.world.level.block.state.BlockBehaviour.Properties, T> constructor,
      Supplier<? extends net.minecraft.world.level.block.Block> base
   ) {
      return this.registerBlock(
         name,
         (Supplier<T>)(() -> (T)((Block)constructor.apply(
            net.minecraft.world.level.block.state.BlockBehaviour.Properties.ofFullCopy((BlockBehaviour)base.get()).setId(this.blockKey(name))
         )))
      );
   }

   public Supplier<net.minecraft.world.level.block.Block> registerBlockVariantAndItem(
      String name, Supplier<? extends net.minecraft.world.level.block.Block> base
   ) {
      return this.registerBlockAndItem(
         name,
         (Supplier<net.minecraft.world.level.block.Block>)(() -> new Block(
            net.minecraft.world.level.block.state.BlockBehaviour.Properties.ofFullCopy((BlockBehaviour)base.get())
         ))
      );
   }

   public <T extends net.minecraft.world.level.block.Block> Supplier<T> registerBlockVariantAndItem(
      String name,
      Function<net.minecraft.world.level.block.state.BlockBehaviour.Properties, T> constructor,
      Supplier<? extends net.minecraft.world.level.block.Block> base
   ) {
      Supplier<T> registered = this.registerBlock(name, (Supplier<T>)(() -> (T)((Block)constructor.apply(
         net.minecraft.world.level.block.state.BlockBehaviour.Properties.ofFullCopy((BlockBehaviour)base.get()).setId(this.blockKey(name))
      ))));
      this.registerBlockItem(name, registered);
      return registered;
   }

   public abstract <T extends net.minecraft.world.level.block.entity.BlockEntity> Supplier<net.minecraft.world.level.block.entity.BlockEntityType<T>> registerBlockEntity(
      String var1,
      BiFunction<net.minecraft.core.BlockPos, net.minecraft.world.level.block.state.BlockState, T> var2,
      Set<Supplier<? extends net.minecraft.world.level.block.Block>> var3
   );

   public <T extends net.minecraft.world.level.block.entity.BlockEntity> Supplier<net.minecraft.world.level.block.entity.BlockEntityType<T>> registerBlockEntity(
      String name,
      BiFunction<net.minecraft.core.BlockPos, net.minecraft.world.level.block.state.BlockState, T> blockEntity,
      Supplier<? extends net.minecraft.world.level.block.Block> block
   ) {
      return this.registerBlockEntity(name, blockEntity, Set.of(block));
   }

   public abstract Supplier<net.minecraft.world.item.CreativeModeTab> registerCreativeTab(
      String var1,
      Supplier<? extends net.minecraft.world.level.ItemLike> var2,
      net.minecraft.network.chat.Component var3,
      Collection<Supplier<? extends net.minecraft.world.level.ItemLike>> var4
   );

   public Supplier<net.minecraft.world.item.CreativeModeTab> registerCreativeTab(
      String name, Supplier<? extends net.minecraft.world.level.ItemLike> icon, net.minecraft.network.chat.Component title
   ) {
      return this.registerCreativeTab(name, icon, title, Set.of());
   }

   public Supplier<net.minecraft.world.item.CreativeModeTab> registerCreativeTab(
      String name,
      Supplier<? extends net.minecraft.world.level.ItemLike> icon,
      String title,
      Collection<Supplier<? extends net.minecraft.world.level.ItemLike>> items
   ) {
      return this.registerCreativeTab(name, icon, Component.translatable(title), items);
   }

   public Supplier<net.minecraft.world.item.CreativeModeTab> registerCreativeTab(
      String name, Supplier<? extends net.minecraft.world.level.ItemLike> icon, String title
   ) {
      return this.registerCreativeTab(name, icon, title, Set.of());
   }

   public abstract <T extends net.minecraft.world.entity.Entity> Supplier<net.minecraft.world.entity.EntityType<T>> registerEntity(
      String var1, net.minecraft.world.entity.EntityType.Builder<T> var2
   );

   public Supplier<net.minecraft.world.item.SpawnEggItem> registerSpawnEgg(
      String name,
      Supplier<net.minecraft.world.entity.EntityType<? extends net.minecraft.world.entity.Mob>> entity,
      int primaryColor,
      int secondaryColor
   ) {
      return this.registerItem(name, (Supplier)(() -> new SpawnEggItem(new Properties().setId(this.itemKey(name)).spawnEgg((EntityType<?>)entity.get()))));
   }

   public abstract <T extends net.minecraft.world.effect.MobEffect> Supplier<T> registerEffect(String var1, Supplier<T> var2);

   public abstract <T extends net.minecraft.world.item.enchantment.Enchantment> Supplier<T> registerEnchantment(String var1, Supplier<T> var2);

   public abstract Supplier<com.mojang.serialization.MapCodec<? extends net.minecraft.world.level.storage.loot.functions.LootItemFunction>> registerLootItemFunction(
      String var1, Supplier<com.mojang.serialization.MapCodec<? extends net.minecraft.world.level.storage.loot.functions.LootItemFunction>> var2
   );

   public abstract <T extends net.minecraft.world.level.levelgen.feature.Feature<?>> Supplier<T> registerFeature(String var1, Supplier<T> var2);

   public abstract <T extends net.minecraft.world.inventory.AbstractContainerMenu> Supplier<net.minecraft.world.inventory.MenuType<T>> registerMenu(
      String var1, TriFunction<Integer, net.minecraft.world.entity.player.Inventory, net.minecraft.network.FriendlyByteBuf, T> var2
   );

   public abstract <T extends net.minecraft.core.particles.ParticleType<?>> Supplier<T> registerParticles(String var1, Supplier<T> var2);

   public abstract <T extends net.minecraft.world.item.crafting.RecipeSerializer<?>> Supplier<T> registerRecipeSerializer(String var1, Supplier<T> var2);

   public abstract <T extends net.minecraft.world.item.crafting.RecipeType<?>> Supplier<T> registerRecipeType(String var1, Supplier<T> var2);

   public Supplier<net.minecraft.world.item.crafting.RecipeType<?>> registerRecipeType(String name) {
      String identifier = this.mod + ":" + name;
      return this.registerRecipeType(name, () -> new net.minecraft.world.item.crafting.RecipeType<net.minecraft.world.item.crafting.Recipe<?>>() {
         @Override
         public String toString() {
            return identifier;
         }
      });
   }

   public abstract <T extends net.minecraft.sounds.SoundEvent> Supplier<T> registerSound(String var1, Supplier<T> var2);

   public Supplier<net.minecraft.sounds.SoundEvent> registerSound(String name) {
      return this.registerSound(name, (Supplier<net.minecraft.sounds.SoundEvent>)(() -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(this.mod, name))));
   }

   public abstract <T extends net.minecraft.world.level.material.Fluid> Supplier<T> registerFluid(String var1, Supplier<T> var2);

   public abstract void register();
}
