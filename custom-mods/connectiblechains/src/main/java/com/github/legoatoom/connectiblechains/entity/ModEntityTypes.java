package com.github.legoatoom.connectiblechains.entity;

import com.github.legoatoom.connectiblechains.ConnectibleChains;
import com.github.legoatoom.connectiblechains.util.Helper;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.EntityType.Builder;

public class ModEntityTypes {
   public static final net.minecraft.world.entity.EntityType<ChainKnotEntity> CHAIN_KNOT = register(
      "chain_knot",
      Builder.of((net.minecraft.world.entity.EntityType.EntityFactory<ChainKnotEntity>) ChainKnotEntity::new, MobCategory.MISC)
         .updateInterval(Integer.MAX_VALUE)
         .alwaysUpdateVelocity(false)
         .sized(0.375F, 0.5F)
         .canSpawnFarFromPlayer()
         .fireImmune()
   );
   public static final net.minecraft.world.entity.EntityType<ChainCollisionEntity> CHAIN_COLLISION = register(
      "chain_collision",
      Builder.of((net.minecraft.world.entity.EntityType.EntityFactory<ChainCollisionEntity>) ChainCollisionEntity::new, MobCategory.MISC)
         .clientTrackingRange(1)
         .updateInterval(Integer.MAX_VALUE)
         .alwaysUpdateVelocity(false)
         .sized(0.25F, 0.375F)
         .noSave()
         .noSummon()
         .fireImmune()
   );

   public static <T extends net.minecraft.world.entity.Entity> net.minecraft.world.entity.EntityType<T> register(
      String id, net.minecraft.world.entity.EntityType.Builder<T> builder
   ) {
      net.minecraft.resources.ResourceKey<net.minecraft.world.entity.EntityType<?>> key = ResourceKey.create(
         Registries.ENTITY_TYPE, Helper.identifier(id)
      );
      return Registry.register(BuiltInRegistries.ENTITY_TYPE, key, builder.build(key));
   }

   public static void init() {
      ConnectibleChains.LOGGER.info("Initialized entity types.");
   }
}
