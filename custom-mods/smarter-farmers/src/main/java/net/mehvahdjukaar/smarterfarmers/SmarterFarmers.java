package net.mehvahdjukaar.smarterfarmers;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import net.mehvahdjukaar.smarterfarmers.mixins.VillagerAccessor;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class SmarterFarmers {
   public static final String MOD_ID = "smarterfarmers";
   public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

   public static final TagKey<net.minecraft.world.level.block.Block> HARVEST_ON_FARMLAND_SPECIAL = TagKey.create(Registries.BLOCK, res("harvest_on_farmland"));
   public static final TagKey<net.minecraft.world.level.block.Block> HARVEST_ON_TILLABLE_NO_REPLANT = TagKey.create(Registries.BLOCK, res("harvestable_on_tillable_no_replant"));
   public static final TagKey<net.minecraft.world.level.block.Block> HARVEST_ON_TILLABLE = TagKey.create(Registries.BLOCK, res("harvestable_on_tillable"));
   public static final TagKey<net.minecraft.world.level.block.Block> FARMER_TILLABLE = TagKey.create(Registries.BLOCK, res("tillable"));
   public static final TagKey<net.minecraft.world.level.block.Block> VALID_FARMLAND = TagKey.create(Registries.BLOCK, res("farmland"));
   public static final TagKey<net.minecraft.world.level.block.Block> HARVEST_BLACKLIST = TagKey.create(Registries.BLOCK, res("harvest_blacklist"));
   public static final TagKey<Item> EAT_BLACKLIST = TagKey.create(Registries.ITEM, res("villagers_cant_eat"));
   public static final TagKey<Item> STEM_SEEDS = TagKey.create(Registries.ITEM, res("stem_seeds"));

   public static SmarterFarmersConfig CONFIG;
   private static final Map<Item, Integer> OLD_FOOD_POINTS = new HashMap<>();

   public static Identifier res(String name) {
      return Identifier.fromNamespaceAndPath(MOD_ID, name);
   }

   public static void commonInit() {
      AutoConfig.register(SmarterFarmersConfig.class, JanksonConfigSerializer::new);
      CONFIG = AutoConfig.getConfigHolder(SmarterFarmersConfig.class).getConfig();
   }

   public static void setupWithTags() {
      try {
         if (OLD_FOOD_POINTS.isEmpty()) {
            OLD_FOOD_POINTS.putAll(Villager.FOOD_POINTS);
         }
         Map<Item, Integer> newMap = new HashMap<>(OLD_FOOD_POINTS);
         for (Item i : BuiltInRegistries.ITEM) {
            ItemStack stack = i.getDefaultInstance();
            FoodProperties food = stack.get(DataComponents.FOOD);
            if (food != null && stack.getRarity() == Rarity.COMMON && !stack.is(EAT_BLACKLIST)
                  && hasOnlyBeneficialEffects(stack) && i.getCraftingRemainder() == null
                  && i.getDefaultMaxStackSize() > 1) {
               newMap.put(i, Math.max(1, food.nutrition() * 2 / 3));
            }
         }
         VillageFoodPoints.set(newMap);
      } catch (Exception e) {
         LOGGER.warn("Failed to rebuild villager food points", e);
      }
   }

   private static boolean hasOnlyBeneficialEffects(ItemStack stack) {
      Consumable consumable = stack.get(DataComponents.CONSUMABLE);
      if (consumable == null) {
         return true;
      }
      return consumable.onConsumeEffects().stream().allMatch(effect ->
            !(effect instanceof ApplyStatusEffectsConsumeEffect apply)
                  || apply.effects().stream().allMatch(instance -> instance.getEffect().value().isBeneficial()));
   }

   private static final class VillageFoodPoints {
      private static void set(Map<Item, Integer> points) {
         VillagerAccessor.setFoodPoints(points);
      }
   }

   public static void spawnEatingParticles(AbstractVillager entity) {
      Vec3 vec3 = new Vec3(0.0, 0.0, 0.4).yRot(-entity.yBodyRot * Mth.DEG_TO_RAD).add(entity.getEyePosition());
      ItemParticleOption option = new ItemParticleOption(ParticleTypes.ITEM, net.minecraft.world.item.ItemStackTemplate.fromNonEmptyStack(entity.getMainHandItem()));
      net.minecraft.util.RandomSource random = entity.level().getRandom();
      entity.level().addParticle(option,
            vec3.x + Mth.randomBetween(random, -0.05F, 0.05F),
            vec3.y + Mth.randomBetween(random, -0.05F, 0.05F),
            vec3.z + Mth.randomBetween(random, -0.05F, 0.05F),
            Mth.randomBetween(random, -0.05F, 0.05F),
            0.1 + Mth.randomBetween(random, -0.05F, 0.05F),
            Mth.randomBetween(random, -0.05F, 0.05F));
   }
}
