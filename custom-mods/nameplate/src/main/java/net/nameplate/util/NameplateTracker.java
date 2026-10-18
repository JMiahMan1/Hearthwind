package net.nameplate.util;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.DefaultAttributes;
import net.nameplate.NameplateMain;
import net.nameplate.access.MobEntityAccess;
import net.nameplate.network.packet.LevelPacket;

public class NameplateTracker {
   public static void startTracking(Mob mobEntity, ServerPlayer serverPlayer) {
      if (((MobEntityAccess)mobEntity).showMobRpgLabel()) {
         int mobLevel = getMobLevel(mobEntity);
         ((MobEntityAccess)mobEntity).setMobRpgLevel(mobLevel);
         ServerPlayNetworking.send(serverPlayer, new LevelPacket(mobLevel, mobEntity.getId(), ((MobEntityAccess)mobEntity).showMobRpgLabel()));
      }
   }

   @SuppressWarnings("unchecked")
   public static int getMobLevel(Mob mobEntity) {
      int level = 1;
      AttributeSupplier supplier = DefaultAttributes.getSupplier((EntityType<? extends LivingEntity>)mobEntity.getType());
      if (supplier != null) {
         level = (int)(
               NameplateMain.CONFIG.levelMultiplier
                  * mobEntity.getAttributeBaseValue(Attributes.MAX_HEALTH)
                  / Math.abs(supplier.getBaseValue(Attributes.MAX_HEALTH))
            )
            - NameplateMain.CONFIG.levelMultiplier
            + 1;
      }

      if (level < 1) {
         level = 1;
      }

      return level;
   }
}
