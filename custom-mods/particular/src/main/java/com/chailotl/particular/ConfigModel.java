package com.chailotl.particular;

import com.chailotl.particular.sushi_bar.owo.config.SushiModmenu;
import io.wispforest.owo.config.annotation.Config;
import io.wispforest.owo.config.annotation.Nest;
import io.wispforest.owo.config.annotation.PredicateConstraint;
import io.wispforest.owo.config.annotation.RangeConstraint;
import io.wispforest.owo.config.annotation.SectionHeader;
import io.wispforest.owo.ui.core.Color;
import java.util.Arrays;
import java.util.List;
import net.minecraft.resources.Identifier;

@SushiModmenu(
   modId = "particular"
)
@Config(
   name = "particular",
   wrapperName = "ParticularConfig"
)
public class ConfigModel {
   @SectionHeader("enabledEffects")
   public boolean waterSplash = true;
   public boolean cascades = true;
   public boolean waterfallSpray = true;
   public boolean fireflies = true;
   public boolean fallingLeaves = true;
   public boolean caveDust = true;
   public boolean chestBubbles = true;
   public boolean soulSandBubbles = true;
   public boolean barrelBubbles = true;
   public boolean poppingBubbles = true;
   public boolean rainRipples = true;
   public boolean waterDripRipples = true;
   public boolean cakeEatingParticles = true;
   public boolean emissiveLavaDrips = true;
   @SectionHeader("advancedSettings")
   @Nest
   public ConfigModel.FireflySettings fireflySettings = new ConfigModel.FireflySettings();
   @Nest
   public ConfigModel.FallingLeavesSettings fallingLeavesSettings = new ConfigModel.FallingLeavesSettings();
   @Nest
   public ConfigModel.CaveDustSettings caveDustSettings = new ConfigModel.CaveDustSettings();

   public static class CaveDustSettings {
      @PredicateConstraint("minOne")
      public int spawnChance = 700;
      @PredicateConstraint("minOne")
      public int baseMaxAge = 200;
      public Color color = Color.ofRgb(8421504);
      @PredicateConstraint("minZero")
      public int fadeDuration = 20;
      public float maxAcceleration = 0.03F;
      @PredicateConstraint("minOne")
      public int accelChangeChance = 180;
      public List<net.minecraft.resources.Identifier> excludeBiomes = Arrays.asList(
         Identifier.parse("minecraft:lush_caves"),
         Identifier.parse("minecraft:dripstone_caves"),
         Identifier.parse("minecraft:deep_dark")
      );

      public static boolean minOne(int num) {
         return num >= 1;
      }

      public static boolean minZero(int num) {
         return num >= 0;
      }
   }

   public static class FallingLeavesSettings {
      @PredicateConstraint("minOne")
      public int spawnChance = 60;
      public boolean spawnRipples = true;
      public boolean layFlatOnGround = true;
      public boolean layFlatRightAngles = false;

      public static boolean minOne(int num) {
         return num >= 1;
      }
   }

   public static class FireflySettings {
      @RangeConstraint(
         min = 0.0,
         max = 23999.0
      )
      public int startTime = 12000;
      @RangeConstraint(
         min = 0.0,
         max = 23999.0
      )
      public int endTime = 23000;
      public float minTemp = 0.5F;
      public float maxTemp = 0.99F;
      public boolean canSpawnInRain = false;
      @SectionHeader("frequencyModifiers")
      public List<Float> dailyRandom = Arrays.asList(0.0F, 0.0F, 0.0F, 0.33F, 0.66F, 1.0F);
      @RangeConstraint(
         min = 0.0,
         max = 1.0
      )
      public float grass = 0.16666667F;
      @RangeConstraint(
         min = 0.0,
         max = 1.0
      )
      public float tallGrass = 0.083333336F;
      @RangeConstraint(
         min = 0.0,
         max = 1.0
      )
      public float flowers = 1.0F;
      @RangeConstraint(
         min = 0.0,
         max = 1.0
      )
      public float tallFlowers = 0.5F;
   }
}
