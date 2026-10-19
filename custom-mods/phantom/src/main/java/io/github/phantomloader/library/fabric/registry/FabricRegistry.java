package io.github.phantomloader.library.fabric.registry;

import io.github.phantomloader.library.registry.ModRegistry;
import java.util.Collection;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.CreativeModeTab.Row;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import org.apache.commons.lang3.function.TriFunction;

public class FabricRegistry extends ModRegistry {
   public FabricRegistry(String mod) {
      super(mod);
   }

   private <V, T extends V> Supplier<T> register(net.minecraft.core.Registry<V> registry, String name, Supplier<T> object) {
      T registered = (T)Registry.register(registry, Identifier.fromNamespaceAndPath(this.mod, name), object.get());
      return () -> registered;
   }

   @Override
   public <T extends net.minecraft.world.item.Item> Supplier<T> registerItem(String name, Supplier<T> item) {
      return this.register(BuiltInRegistries.ITEM, name, item);
   }

   @Override
   public <T extends net.minecraft.world.level.block.Block> Supplier<T> registerBlock(String name, Supplier<T> block) {
      return this.register(BuiltInRegistries.BLOCK, name, block);
   }

   @Override
   public <T extends net.minecraft.world.level.block.entity.BlockEntity> Supplier<net.minecraft.world.level.block.entity.BlockEntityType<T>> registerBlockEntity(
      String name,
      BiFunction<net.minecraft.core.BlockPos, net.minecraft.world.level.block.state.BlockState, T> blockEntity,
      Set<Supplier<? extends net.minecraft.world.level.block.Block>> blocks
   ) {
      return this.register(
         BuiltInRegistries.BLOCK_ENTITY_TYPE,
         name,
         () -> new BlockEntityType<>(blockEntity::apply, blocks.stream().map(Supplier::get).collect(java.util.stream.Collectors.toSet()))
      );
   }

   @Override
   public Supplier<net.minecraft.world.item.CreativeModeTab> registerCreativeTab(
      String name,
      Supplier<? extends net.minecraft.world.level.ItemLike> icon,
      net.minecraft.network.chat.Component title,
      Collection<Supplier<? extends net.minecraft.world.level.ItemLike>> items
   ) {
      return this.register(
         BuiltInRegistries.CREATIVE_MODE_TAB,
         name,
         (Supplier<net.minecraft.world.item.CreativeModeTab>)(() -> CreativeModeTab.builder(Row.TOP, 0)
            .title(title)
            .icon(() -> new ItemStack((ItemLike)icon.get()))
            .displayItems((params, output) -> items.forEach(item -> output.accept((ItemLike)item.get())))
            .build())
      );
   }

   @Override
   public <T extends net.minecraft.world.entity.Entity> Supplier<net.minecraft.world.entity.EntityType<T>> registerEntity(
      String name, net.minecraft.world.entity.EntityType.Builder<T> builder
   ) {
      Identifier id = Identifier.fromNamespaceAndPath(this.mod, name);
      return this.register(BuiltInRegistries.ENTITY_TYPE, name, () -> builder.build(ResourceKey.create(Registries.ENTITY_TYPE, id)));
   }

   @Override
   public <T extends net.minecraft.world.effect.MobEffect> Supplier<T> registerEffect(String name, Supplier<T> effect) {
      return this.register(BuiltInRegistries.MOB_EFFECT, name, effect);
   }

   @Override
   public <T extends net.minecraft.world.item.enchantment.Enchantment> Supplier<T> registerEnchantment(String name, Supplier<T> enchantment) {
      throw new UnsupportedOperationException("Enchantments are datapack-driven in 26.2 and cannot be registered through Phantom");
   }

   @Override
   public Supplier<com.mojang.serialization.MapCodec<? extends net.minecraft.world.level.storage.loot.functions.LootItemFunction>> registerLootItemFunction(
      String name, Supplier<com.mojang.serialization.MapCodec<? extends net.minecraft.world.level.storage.loot.functions.LootItemFunction>> lootItemFunction
   ) {
      return this.register(BuiltInRegistries.LOOT_FUNCTION_TYPE, name, lootItemFunction);
   }

   @Override
   public <T extends net.minecraft.world.level.levelgen.feature.Feature<?>> Supplier<T> registerFeature(String name, Supplier<T> feature) {
      return this.register(BuiltInRegistries.FEATURE, name, feature);
   }

   @Override
   public <T extends net.minecraft.world.inventory.AbstractContainerMenu> Supplier<net.minecraft.world.inventory.MenuType<T>> registerMenu(
      String name, TriFunction<Integer, net.minecraft.world.entity.player.Inventory, net.minecraft.network.FriendlyByteBuf, T> menu
   ) {
      return this.register(BuiltInRegistries.MENU, name, () -> new MenuType<T>((syncId, inventory) -> menu.apply(syncId, inventory, null), FeatureFlags.DEFAULT_FLAGS));
   }

   @Override
   public <T extends net.minecraft.core.particles.ParticleType<?>> Supplier<T> registerParticles(String name, Supplier<T> particles) {
      return this.register(BuiltInRegistries.PARTICLE_TYPE, name, particles);
   }

   @Override
   public <T extends net.minecraft.world.item.crafting.RecipeSerializer<?>> Supplier<T> registerRecipeSerializer(String name, Supplier<T> recipeSerializer) {
      return this.register(BuiltInRegistries.RECIPE_SERIALIZER, name, recipeSerializer);
   }

   @Override
   public <T extends net.minecraft.world.item.crafting.RecipeType<?>> Supplier<T> registerRecipeType(String name, Supplier<T> recipeType) {
      return this.register(BuiltInRegistries.RECIPE_TYPE, name, recipeType);
   }

   @Override
   public <T extends net.minecraft.sounds.SoundEvent> Supplier<T> registerSound(String name, Supplier<T> sound) {
      return this.register(BuiltInRegistries.SOUND_EVENT, name, sound);
   }

   @Override
   public <T extends net.minecraft.world.level.material.Fluid> Supplier<T> registerFluid(String name, Supplier<T> fluid) {
      return this.register(BuiltInRegistries.FLUID, name, fluid);
   }

   @Override
   public void register() {
   }
}
