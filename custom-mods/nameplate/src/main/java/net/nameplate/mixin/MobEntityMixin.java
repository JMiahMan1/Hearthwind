package net.nameplate.mixin;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.nameplate.NameplateMain;
import net.nameplate.access.MobEntityAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({Mob.class})
public class MobEntityMixin implements MobEntityAccess {
   @Unique
   private int mobRpgLevel = 1;
   @Unique
   private boolean showMobRpgLabel = true;

   @Inject(
      method = {"<init>"},
      at = {@At("TAIL")}
   )
   private void initMixin(EntityType<? extends Mob> entityType, net.minecraft.world.level.Level world, CallbackInfo info) {
      if (NameplateMain.CONFIG.excludedEntities.contains(EntityType.getKey(entityType).toString())) {
         this.showMobRpgLabel = false;
      }
   }

   @Inject(
      method = {"addAdditionalSaveData"},
      at = {@At("TAIL")}
   )
   private void addAdditionalSaveDataMixin(ValueOutput output, CallbackInfo info) {
      output.putInt("MobRpgLevel", this.mobRpgLevel);
      output.putBoolean("HasMobRpgLabel", this.showMobRpgLabel);
   }

   @Inject(
      method = {"readAdditionalSaveData"},
      at = {@At("TAIL")}
   )
   private void readAdditionalSaveDataMixin(ValueInput input, CallbackInfo info) {
      this.mobRpgLevel = input.getIntOr("MobRpgLevel", 1);
      this.showMobRpgLabel = input.getBooleanOr("HasMobRpgLabel", true);
   }

   @Override
   public void setMobRpgLevel(int level) {
      this.mobRpgLevel = level;
   }

   @Override
   public int getMobRpgLevel() {
      return this.mobRpgLevel;
   }

   @Override
   public boolean showMobRpgLabel() {
      return this.showMobRpgLabel;
   }

   @Override
   public void setShowMobRpgLabel(boolean setLabel) {
      this.showMobRpgLabel = setLabel;
   }
}
