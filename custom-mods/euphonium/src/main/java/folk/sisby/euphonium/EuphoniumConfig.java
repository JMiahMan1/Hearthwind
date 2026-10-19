package folk.sisby.euphonium;

import folk.sisby.kaleido.api.WrappedConfig;
import folk.sisby.kaleido.api.WrappedConfig.Section;
import folk.sisby.kaleido.lib.quiltconfig.api.annotations.Comment;
import folk.sisby.kaleido.lib.quiltconfig.api.annotations.FloatRange;
import folk.sisby.kaleido.lib.quiltconfig.api.annotations.IntegerRange;
import folk.sisby.kaleido.lib.quiltconfig.api.values.ValueList;
import folk.sisby.kaleido.lib.quiltconfig.impl.Comments;
import java.util.List;
import net.minecraft.sounds.SoundSource;

public class EuphoniumConfig extends WrappedConfig {
   @Comment({"The channel that Euphonium will use for playing sounds"})
   public SoundSource channel = SoundSource.AMBIENT;
   @Comment({"Plays ambient background sound according to the biome and time of day"})
   public boolean biomeAmbienceEnabled = true;
   @Comment({"Plays ambient sound according to world features around the player"})
   public boolean worldAmbienceEnabled = true;
   public EuphoniumConfig.BiomeAmbience biomeAmbience = new EuphoniumConfig.BiomeAmbience();
   public EuphoniumConfig.WorldAmbience worldAmbience = new EuphoniumConfig.WorldAmbience();

   public static class BiomeAmbience implements Section {
      @Comments({@Comment({"Number of blocks above the ground that biome ambience will be silenced"}), @Comment({"Set to zero to disable"})})
      public int cullSoundAboveGround = 32;
      @Comments({@Comment({"Number of blocks to check for neighbouring biomes"}), @Comment({"Set to zero to disable"})})
      public int biomeBlend = 32;
      @Comment({"Affects the volume of all biome ambient sounds"})
      @FloatRange(
         min = 0.0,
         max = 1.0
      )
      public double volumeScaling = 0.55;
      @Comment({"Dimensions in which biome ambience will be played"})
      public List<String> dimensions = ValueList.create("minecraft:overworld", new String[]{"minecraft:overworld", "minecraft:the_end"});
   }

   public static class WorldAmbience implements Section {
      @Comment({"Affects the volume of all world ambient sounds"})
      @FloatRange(
         min = 0.0,
         max = 1.0
      )
      public double volumeScaling = 0.55;
      @Comments({@Comment({"Number of blocks above the ground that biome ambience will be silenced"}), @Comment({"Set to zero to disable"})})
      public int cullSoundAboveGround = 32;
      @Comment({"Plays ambient sounds while anywhere in the End"})
      public boolean alien = true;
      @Comment({"Plays ambient sounds in cold and/or barren overworld environments"})
      public boolean bleak = true;
      @Comment({"Plays ambient sounds in dry and/or hot overworld environments"})
      public boolean dry = true;
      @Comment({"Plays ambient sounds when high up in the overworld"})
      public boolean high = true;
      @Comment({"Plays ambient sounds in plains environments at night"})
      public boolean nightPlains = true;
      @Comment({"Plays ambient sounds when in a cold biome during a thunderstorm"})
      public boolean snowstorm = true;
      @Comment({"Plays ambient sounds while inside a woodland mansion"})
      public boolean mansion = true;
      @Comment({"Plays ambient sounds when a player is inside a village"})
      public boolean village = true;
      @Comment({"Plays ambient sounds from a nearby mineshaft"})
      public boolean mineshaft = true;
      @Comment({"Plays ambient sounds from a nearby amethyst geode"})
      public boolean geode = true;
      @Comment({"Plays water sounds from a nearby water source when underground"})
      public boolean undergroundWater = true;
      @Comment({"Plays ambient sounds when the player is underground and near gravel blocks"})
      public boolean gravel = true;
      @Comment({"Plays ambient sounds when the player is underground and near deepslate blocks"})
      public boolean deepslate = true;
      @Comment({"Plays more Intense cave sounds when below Y 0 and light level is lower than 10"})
      public boolean caveDepth = true;
      @Comment({"Plays a low drone sound when in a cave below a certain depth"})
      public boolean caveDrone = true;
      @Comment({"Depth (Y Coordinate) under which cave drone ambience will play"})
      public int caveDroneDepth = 48;
      @Comment({"Light level at which cave ambience (drone and depth) will stop playing"})
      @IntegerRange(
         min = 0L,
         max = 16L
      )
      public int caveLightLevel = 10;
      @Comment({"Dimensions in which cave ambience (drone and depth) will be played"})
      public List<String> caveDimensions = ValueList.create("minecraft:overworld", new String[]{"minecraft:overworld"});
   }
}
