package com.chailotl.particular;

import blue.endless.jankson.Jankson.Builder;
import io.wispforest.owo.config.ConfigWrapper;
import io.wispforest.owo.config.Option;
import io.wispforest.owo.config.Option.Key;
import io.wispforest.owo.ui.core.Color;
import java.util.List;
import java.util.function.Consumer;

public class ParticularConfig extends ConfigWrapper<ConfigModel> {
   public final ParticularConfig.Keys keys = new ParticularConfig.Keys();
   private final Option<Boolean> waterSplash = this.optionForKey(this.keys.waterSplash);
   private final Option<Boolean> cascades = this.optionForKey(this.keys.cascades);
   private final Option<Boolean> waterfallSpray = this.optionForKey(this.keys.waterfallSpray);
   private final Option<Boolean> fireflies = this.optionForKey(this.keys.fireflies);
   private final Option<Boolean> fallingLeaves = this.optionForKey(this.keys.fallingLeaves);
   private final Option<Boolean> caveDust = this.optionForKey(this.keys.caveDust);
   private final Option<Boolean> chestBubbles = this.optionForKey(this.keys.chestBubbles);
   private final Option<Boolean> soulSandBubbles = this.optionForKey(this.keys.soulSandBubbles);
   private final Option<Boolean> barrelBubbles = this.optionForKey(this.keys.barrelBubbles);
   private final Option<Boolean> poppingBubbles = this.optionForKey(this.keys.poppingBubbles);
   private final Option<Boolean> rainRipples = this.optionForKey(this.keys.rainRipples);
   private final Option<Boolean> waterDripRipples = this.optionForKey(this.keys.waterDripRipples);
   private final Option<Boolean> cakeEatingParticles = this.optionForKey(this.keys.cakeEatingParticles);
   private final Option<Boolean> emissiveLavaDrips = this.optionForKey(this.keys.emissiveLavaDrips);
   private final Option<Integer> fireflySettings_startTime = this.optionForKey(this.keys.fireflySettings_startTime);
   private final Option<Integer> fireflySettings_endTime = this.optionForKey(this.keys.fireflySettings_endTime);
   private final Option<Float> fireflySettings_minTemp = this.optionForKey(this.keys.fireflySettings_minTemp);
   private final Option<Float> fireflySettings_maxTemp = this.optionForKey(this.keys.fireflySettings_maxTemp);
   private final Option<Boolean> fireflySettings_canSpawnInRain = this.optionForKey(this.keys.fireflySettings_canSpawnInRain);
   private final Option<List<Float>> fireflySettings_dailyRandom = this.optionForKey(this.keys.fireflySettings_dailyRandom);
   private final Option<Float> fireflySettings_grass = this.optionForKey(this.keys.fireflySettings_grass);
   private final Option<Float> fireflySettings_tallGrass = this.optionForKey(this.keys.fireflySettings_tallGrass);
   private final Option<Float> fireflySettings_flowers = this.optionForKey(this.keys.fireflySettings_flowers);
   private final Option<Float> fireflySettings_tallFlowers = this.optionForKey(this.keys.fireflySettings_tallFlowers);
   private final Option<Integer> fallingLeavesSettings_spawnChance = this.optionForKey(this.keys.fallingLeavesSettings_spawnChance);
   private final Option<Boolean> fallingLeavesSettings_spawnRipples = this.optionForKey(this.keys.fallingLeavesSettings_spawnRipples);
   private final Option<Boolean> fallingLeavesSettings_layFlatOnGround = this.optionForKey(this.keys.fallingLeavesSettings_layFlatOnGround);
   private final Option<Boolean> fallingLeavesSettings_layFlatRightAngles = this.optionForKey(this.keys.fallingLeavesSettings_layFlatRightAngles);
   private final Option<Integer> caveDustSettings_spawnChance = this.optionForKey(this.keys.caveDustSettings_spawnChance);
   private final Option<Integer> caveDustSettings_baseMaxAge = this.optionForKey(this.keys.caveDustSettings_baseMaxAge);
   private final Option<Color> caveDustSettings_color = this.optionForKey(this.keys.caveDustSettings_color);
   private final Option<Integer> caveDustSettings_fadeDuration = this.optionForKey(this.keys.caveDustSettings_fadeDuration);
   private final Option<Float> caveDustSettings_maxAcceleration = this.optionForKey(this.keys.caveDustSettings_maxAcceleration);
   private final Option<Integer> caveDustSettings_accelChangeChance = this.optionForKey(this.keys.caveDustSettings_accelChangeChance);
   private final Option<List<net.minecraft.resources.Identifier>> caveDustSettings_excludeBiomes = this.optionForKey(
      this.keys.caveDustSettings_excludeBiomes
   );
   public final ParticularConfig.FireflySettings_ fireflySettings = new ParticularConfig.FireflySettings_();
   public final ParticularConfig.FallingLeavesSettings_ fallingLeavesSettings = new ParticularConfig.FallingLeavesSettings_();
   public final ParticularConfig.CaveDustSettings_ caveDustSettings = new ParticularConfig.CaveDustSettings_();

   private ParticularConfig() {
      super(ConfigModel.class);
   }

   private ParticularConfig(Consumer<Builder> janksonBuilder) {
      super(ConfigModel.class, janksonBuilder);
   }

   public static ParticularConfig createAndLoad() {
      ParticularConfig wrapper = new ParticularConfig();
      wrapper.load();
      return wrapper;
   }

   public static ParticularConfig createAndLoad(Consumer<Builder> janksonBuilder) {
      ParticularConfig wrapper = new ParticularConfig(janksonBuilder);
      wrapper.load();
      return wrapper;
   }

   public boolean waterSplash() {
      return (Boolean)this.waterSplash.value();
   }

   public void waterSplash(boolean value) {
      this.waterSplash.set(value);
   }

   public boolean cascades() {
      return (Boolean)this.cascades.value();
   }

   public void cascades(boolean value) {
      this.cascades.set(value);
   }

   public boolean waterfallSpray() {
      return (Boolean)this.waterfallSpray.value();
   }

   public void waterfallSpray(boolean value) {
      this.waterfallSpray.set(value);
   }

   public boolean fireflies() {
      return (Boolean)this.fireflies.value();
   }

   public void fireflies(boolean value) {
      this.fireflies.set(value);
   }

   public boolean fallingLeaves() {
      return (Boolean)this.fallingLeaves.value();
   }

   public void fallingLeaves(boolean value) {
      this.fallingLeaves.set(value);
   }

   public boolean caveDust() {
      return (Boolean)this.caveDust.value();
   }

   public void caveDust(boolean value) {
      this.caveDust.set(value);
   }

   public boolean chestBubbles() {
      return (Boolean)this.chestBubbles.value();
   }

   public void chestBubbles(boolean value) {
      this.chestBubbles.set(value);
   }

   public boolean soulSandBubbles() {
      return (Boolean)this.soulSandBubbles.value();
   }

   public void soulSandBubbles(boolean value) {
      this.soulSandBubbles.set(value);
   }

   public boolean barrelBubbles() {
      return (Boolean)this.barrelBubbles.value();
   }

   public void barrelBubbles(boolean value) {
      this.barrelBubbles.set(value);
   }

   public boolean poppingBubbles() {
      return (Boolean)this.poppingBubbles.value();
   }

   public void poppingBubbles(boolean value) {
      this.poppingBubbles.set(value);
   }

   public boolean rainRipples() {
      return (Boolean)this.rainRipples.value();
   }

   public void rainRipples(boolean value) {
      this.rainRipples.set(value);
   }

   public boolean waterDripRipples() {
      return (Boolean)this.waterDripRipples.value();
   }

   public void waterDripRipples(boolean value) {
      this.waterDripRipples.set(value);
   }

   public boolean cakeEatingParticles() {
      return (Boolean)this.cakeEatingParticles.value();
   }

   public void cakeEatingParticles(boolean value) {
      this.cakeEatingParticles.set(value);
   }

   public boolean emissiveLavaDrips() {
      return (Boolean)this.emissiveLavaDrips.value();
   }

   public void emissiveLavaDrips(boolean value) {
      this.emissiveLavaDrips.set(value);
   }

   public interface CaveDustSettings {
      int spawnChance();

      void spawnChance(int var1);

      int baseMaxAge();

      void baseMaxAge(int var1);

      Color color();

      void color(Color var1);

      int fadeDuration();

      void fadeDuration(int var1);

      float maxAcceleration();

      void maxAcceleration(float var1);

      int accelChangeChance();

      void accelChangeChance(int var1);

      List<net.minecraft.resources.Identifier> excludeBiomes();

      void excludeBiomes(List<net.minecraft.resources.Identifier> var1);
   }

   public class CaveDustSettings_ implements ParticularConfig.CaveDustSettings {
      @Override
      public int spawnChance() {
         return (Integer)ParticularConfig.this.caveDustSettings_spawnChance.value();
      }

      @Override
      public void spawnChance(int value) {
         ParticularConfig.this.caveDustSettings_spawnChance.set(value);
      }

      @Override
      public int baseMaxAge() {
         return (Integer)ParticularConfig.this.caveDustSettings_baseMaxAge.value();
      }

      @Override
      public void baseMaxAge(int value) {
         ParticularConfig.this.caveDustSettings_baseMaxAge.set(value);
      }

      @Override
      public Color color() {
         return (Color)ParticularConfig.this.caveDustSettings_color.value();
      }

      @Override
      public void color(Color value) {
         ParticularConfig.this.caveDustSettings_color.set(value);
      }

      @Override
      public int fadeDuration() {
         return (Integer)ParticularConfig.this.caveDustSettings_fadeDuration.value();
      }

      @Override
      public void fadeDuration(int value) {
         ParticularConfig.this.caveDustSettings_fadeDuration.set(value);
      }

      @Override
      public float maxAcceleration() {
         return (Float)ParticularConfig.this.caveDustSettings_maxAcceleration.value();
      }

      @Override
      public void maxAcceleration(float value) {
         ParticularConfig.this.caveDustSettings_maxAcceleration.set(value);
      }

      @Override
      public int accelChangeChance() {
         return (Integer)ParticularConfig.this.caveDustSettings_accelChangeChance.value();
      }

      @Override
      public void accelChangeChance(int value) {
         ParticularConfig.this.caveDustSettings_accelChangeChance.set(value);
      }

      @Override
      public List<net.minecraft.resources.Identifier> excludeBiomes() {
         return (List<net.minecraft.resources.Identifier>)ParticularConfig.this.caveDustSettings_excludeBiomes.value();
      }

      @Override
      public void excludeBiomes(List<net.minecraft.resources.Identifier> value) {
         ParticularConfig.this.caveDustSettings_excludeBiomes.set(value);
      }
   }

   public interface FallingLeavesSettings {
      int spawnChance();

      void spawnChance(int var1);

      boolean spawnRipples();

      void spawnRipples(boolean var1);

      boolean layFlatOnGround();

      void layFlatOnGround(boolean var1);

      boolean layFlatRightAngles();

      void layFlatRightAngles(boolean var1);
   }

   public class FallingLeavesSettings_ implements ParticularConfig.FallingLeavesSettings {
      @Override
      public int spawnChance() {
         return (Integer)ParticularConfig.this.fallingLeavesSettings_spawnChance.value();
      }

      @Override
      public void spawnChance(int value) {
         ParticularConfig.this.fallingLeavesSettings_spawnChance.set(value);
      }

      @Override
      public boolean spawnRipples() {
         return (Boolean)ParticularConfig.this.fallingLeavesSettings_spawnRipples.value();
      }

      @Override
      public void spawnRipples(boolean value) {
         ParticularConfig.this.fallingLeavesSettings_spawnRipples.set(value);
      }

      @Override
      public boolean layFlatOnGround() {
         return (Boolean)ParticularConfig.this.fallingLeavesSettings_layFlatOnGround.value();
      }

      @Override
      public void layFlatOnGround(boolean value) {
         ParticularConfig.this.fallingLeavesSettings_layFlatOnGround.set(value);
      }

      @Override
      public boolean layFlatRightAngles() {
         return (Boolean)ParticularConfig.this.fallingLeavesSettings_layFlatRightAngles.value();
      }

      @Override
      public void layFlatRightAngles(boolean value) {
         ParticularConfig.this.fallingLeavesSettings_layFlatRightAngles.set(value);
      }
   }

   public interface FireflySettings {
      int startTime();

      void startTime(int var1);

      int endTime();

      void endTime(int var1);

      float minTemp();

      void minTemp(float var1);

      float maxTemp();

      void maxTemp(float var1);

      boolean canSpawnInRain();

      void canSpawnInRain(boolean var1);

      List<Float> dailyRandom();

      void dailyRandom(List<Float> var1);

      float grass();

      void grass(float var1);

      float tallGrass();

      void tallGrass(float var1);

      float flowers();

      void flowers(float var1);

      float tallFlowers();

      void tallFlowers(float var1);
   }

   public class FireflySettings_ implements ParticularConfig.FireflySettings {
      @Override
      public int startTime() {
         return (Integer)ParticularConfig.this.fireflySettings_startTime.value();
      }

      @Override
      public void startTime(int value) {
         ParticularConfig.this.fireflySettings_startTime.set(value);
      }

      @Override
      public int endTime() {
         return (Integer)ParticularConfig.this.fireflySettings_endTime.value();
      }

      @Override
      public void endTime(int value) {
         ParticularConfig.this.fireflySettings_endTime.set(value);
      }

      @Override
      public float minTemp() {
         return (Float)ParticularConfig.this.fireflySettings_minTemp.value();
      }

      @Override
      public void minTemp(float value) {
         ParticularConfig.this.fireflySettings_minTemp.set(value);
      }

      @Override
      public float maxTemp() {
         return (Float)ParticularConfig.this.fireflySettings_maxTemp.value();
      }

      @Override
      public void maxTemp(float value) {
         ParticularConfig.this.fireflySettings_maxTemp.set(value);
      }

      @Override
      public boolean canSpawnInRain() {
         return (Boolean)ParticularConfig.this.fireflySettings_canSpawnInRain.value();
      }

      @Override
      public void canSpawnInRain(boolean value) {
         ParticularConfig.this.fireflySettings_canSpawnInRain.set(value);
      }

      @Override
      public List<Float> dailyRandom() {
         return (List<Float>)ParticularConfig.this.fireflySettings_dailyRandom.value();
      }

      @Override
      public void dailyRandom(List<Float> value) {
         ParticularConfig.this.fireflySettings_dailyRandom.set(value);
      }

      @Override
      public float grass() {
         return (Float)ParticularConfig.this.fireflySettings_grass.value();
      }

      @Override
      public void grass(float value) {
         ParticularConfig.this.fireflySettings_grass.set(value);
      }

      @Override
      public float tallGrass() {
         return (Float)ParticularConfig.this.fireflySettings_tallGrass.value();
      }

      @Override
      public void tallGrass(float value) {
         ParticularConfig.this.fireflySettings_tallGrass.set(value);
      }

      @Override
      public float flowers() {
         return (Float)ParticularConfig.this.fireflySettings_flowers.value();
      }

      @Override
      public void flowers(float value) {
         ParticularConfig.this.fireflySettings_flowers.set(value);
      }

      @Override
      public float tallFlowers() {
         return (Float)ParticularConfig.this.fireflySettings_tallFlowers.value();
      }

      @Override
      public void tallFlowers(float value) {
         ParticularConfig.this.fireflySettings_tallFlowers.set(value);
      }
   }

   public static class Keys {
      public final Key waterSplash = new Key("waterSplash");
      public final Key cascades = new Key("cascades");
      public final Key waterfallSpray = new Key("waterfallSpray");
      public final Key fireflies = new Key("fireflies");
      public final Key fallingLeaves = new Key("fallingLeaves");
      public final Key caveDust = new Key("caveDust");
      public final Key chestBubbles = new Key("chestBubbles");
      public final Key soulSandBubbles = new Key("soulSandBubbles");
      public final Key barrelBubbles = new Key("barrelBubbles");
      public final Key poppingBubbles = new Key("poppingBubbles");
      public final Key rainRipples = new Key("rainRipples");
      public final Key waterDripRipples = new Key("waterDripRipples");
      public final Key cakeEatingParticles = new Key("cakeEatingParticles");
      public final Key emissiveLavaDrips = new Key("emissiveLavaDrips");
      public final Key fireflySettings_startTime = new Key("fireflySettings.startTime");
      public final Key fireflySettings_endTime = new Key("fireflySettings.endTime");
      public final Key fireflySettings_minTemp = new Key("fireflySettings.minTemp");
      public final Key fireflySettings_maxTemp = new Key("fireflySettings.maxTemp");
      public final Key fireflySettings_canSpawnInRain = new Key("fireflySettings.canSpawnInRain");
      public final Key fireflySettings_dailyRandom = new Key("fireflySettings.dailyRandom");
      public final Key fireflySettings_grass = new Key("fireflySettings.grass");
      public final Key fireflySettings_tallGrass = new Key("fireflySettings.tallGrass");
      public final Key fireflySettings_flowers = new Key("fireflySettings.flowers");
      public final Key fireflySettings_tallFlowers = new Key("fireflySettings.tallFlowers");
      public final Key fallingLeavesSettings_spawnChance = new Key("fallingLeavesSettings.spawnChance");
      public final Key fallingLeavesSettings_spawnRipples = new Key("fallingLeavesSettings.spawnRipples");
      public final Key fallingLeavesSettings_layFlatOnGround = new Key("fallingLeavesSettings.layFlatOnGround");
      public final Key fallingLeavesSettings_layFlatRightAngles = new Key("fallingLeavesSettings.layFlatRightAngles");
      public final Key caveDustSettings_spawnChance = new Key("caveDustSettings.spawnChance");
      public final Key caveDustSettings_baseMaxAge = new Key("caveDustSettings.baseMaxAge");
      public final Key caveDustSettings_color = new Key("caveDustSettings.color");
      public final Key caveDustSettings_fadeDuration = new Key("caveDustSettings.fadeDuration");
      public final Key caveDustSettings_maxAcceleration = new Key("caveDustSettings.maxAcceleration");
      public final Key caveDustSettings_accelChangeChance = new Key("caveDustSettings.accelChangeChance");
      public final Key caveDustSettings_excludeBiomes = new Key("caveDustSettings.excludeBiomes");
   }
}
