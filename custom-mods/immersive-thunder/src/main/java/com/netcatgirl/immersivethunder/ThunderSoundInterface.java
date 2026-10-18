package com.netcatgirl.immersivethunder;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.level.Level;

public interface ThunderSoundInterface {
   double closeDistance = 90.0;
   double mediumDistance = 140.0;
   float thunderCloseVolume = 5000.0F;
   float thunderMediumVolume = 10000.0F;
   float thunderFarVolume = 10000.0F;
   float impactSoundVolume = 2.0F;

   void playThunderSound(Level var1, LightningBolt var2, SoundEvent var3, float var4, boolean var5);
}
