package ht.treechop.mixin;

import ht.treechop.common.settings.ChopSettings;
import ht.treechop.common.settings.ChoppingEntity;
import ht.treechop.common.settings.SyncedChopData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({Entity.class})
public class EntityChopSettingsMixin implements ChoppingEntity {
   private SyncedChopData chopSettings;
   private final String KEY = "treechop:chopSettings";

   @Override
   public SyncedChopData getChopData() {
      return this.chopSettings;
   }

   @Override
   public SyncedChopData setChopData(SyncedChopData chopSettings) {
      this.chopSettings = chopSettings;
      return chopSettings;
   }

   @Inject(
      method = {"saveWithoutId"},
      at = {@At("HEAD")}
   )
   public void injectDataSaving(net.minecraft.world.level.storage.ValueOutput output, CallbackInfo info) {
      if (this.chopSettings != null) {
         output.store("treechop:chopSettings", CompoundTag.CODEC, this.chopSettings.makeSaveData());
      }
   }

   @Inject(
      method = {"load"},
      at = {@At("HEAD")}
   )
   public void injectDataLoading(net.minecraft.world.level.storage.ValueInput input, CallbackInfo info) {
      CompoundTag data = input.read("treechop:chopSettings", CompoundTag.CODEC).orElseGet(CompoundTag::new);
      this.chopSettings = new SyncedChopData(new ChopSettings()).readSaveData(data);
   }
}
