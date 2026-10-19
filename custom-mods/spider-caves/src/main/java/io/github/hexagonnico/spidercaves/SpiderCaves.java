package io.github.hexagonnico.spidercaves;

import io.github.hexagonnico.spidercaves.blocks.ModChestBlock;
import io.github.hexagonnico.spidercaves.blocks.ModChestBlockEntity;
import io.github.hexagonnico.spidercaves.blocks.SpiderEggBlock;
import io.github.hexagonnico.spidercaves.entities.BlackRecluse;
import io.github.hexagonnico.spidercaves.items.SpiderArmorItem;
import io.github.hexagonnico.spidercaves.worldgen.SimpleChestFeature;
import io.github.phantomloader.library.ModEntryPoint;
import io.github.phantomloader.library.registry.ModRegistry;
import java.util.function.Supplier;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.EntityType.Builder;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

public final class SpiderCaves {
   private static final ModRegistry REGISTRY = ModRegistry.instantiate("spider_caves");
   public static final Supplier<net.minecraft.world.item.Item> SPIDER_FANG = REGISTRY.registerItem("spider_fang");
   public static final Supplier<SpiderArmorItem> SPIDER_HELMET = REGISTRY.registerItem("spider_helmet", p -> new SpiderArmorItem(ArmorType.HELMET, p));
   public static final Supplier<SpiderArmorItem> SPIDER_CHESTPLATE = REGISTRY.registerItem("spider_chestplate", p -> new SpiderArmorItem(ArmorType.CHESTPLATE, p));
   public static final Supplier<SpiderArmorItem> SPIDER_LEGGINGS = REGISTRY.registerItem("spider_leggings", p -> new SpiderArmorItem(ArmorType.LEGGINGS, p));
   public static final Supplier<SpiderArmorItem> SPIDER_BOOTS = REGISTRY.registerItem("spider_boots", p -> new SpiderArmorItem(ArmorType.BOOTS, p));
   public static final Supplier<ModChestBlock> WEB_COVERED_CHEST = REGISTRY.registerBlockVariantAndItem(
      "web_covered_chest", ModChestBlock::new, () -> Blocks.CHEST
   );
   public static final Supplier<net.minecraft.world.level.block.entity.BlockEntityType<ModChestBlockEntity>> CHEST_ENTITY = REGISTRY.registerBlockEntity(
      "web_covered_chest", ModChestBlockEntity::new, WEB_COVERED_CHEST
   );
   public static final Supplier<SimpleChestFeature> SIMPLE_CHEST_FEATURE = REGISTRY.registerFeature("simple_chest", SimpleChestFeature::new);
   public static final Supplier<net.minecraft.world.entity.EntityType<BlackRecluse>> BLACK_RECLUSE = REGISTRY.registerEntity(
      "black_recluse", Builder.of(BlackRecluse::new, MobCategory.MONSTER).sized(1.4F, 0.9F).clientTrackingRange(8)
   );
   public static final Supplier<net.minecraft.world.item.SpawnEggItem> BLACK_RECLUSE_SPAWN_EGG = REGISTRY.registerSpawnEgg(
      "black_recluse_spawn_egg", BLACK_RECLUSE::get, 0, 16711680
   );
   public static final Supplier<SpiderEggBlock> SPIDER_EGG = REGISTRY.registerBlockAndItem(
      "spider_egg",
      p -> new SpiderEggBlock(
         p
            .mapColor(MapColor.WOOL)
            .strength(0.5F)
            .sound(SoundType.METAL)
            .noOcclusion()
            .noLootTable()
            .forceSolidOn()
            .pushReaction(PushReaction.DESTROY)
      )
   );

   public static String modId() {
      return REGISTRY.mod;
   }

   @ModEntryPoint
   public static void register() {
      REGISTRY.register();
   }
}
