package folk.sisby.euphonium;

import folk.sisby.euphonium.sound.BiomeSound;
import folk.sisby.euphonium.sound.ISoundType;
import folk.sisby.euphonium.sound.SoundHandler;
import folk.sisby.euphonium.sounds.biome.Badlands;
import folk.sisby.euphonium.sounds.biome.Beach;
import folk.sisby.euphonium.sounds.biome.Caves;
import folk.sisby.euphonium.sounds.biome.Desert;
import folk.sisby.euphonium.sounds.biome.Forest;
import folk.sisby.euphonium.sounds.biome.Icy;
import folk.sisby.euphonium.sounds.biome.Jungle;
import folk.sisby.euphonium.sounds.biome.Mountains;
import folk.sisby.euphonium.sounds.biome.Ocean;
import folk.sisby.euphonium.sounds.biome.Plains;
import folk.sisby.euphonium.sounds.biome.River;
import folk.sisby.euphonium.sounds.biome.Savanna;
import folk.sisby.euphonium.sounds.biome.Swamp;
import folk.sisby.euphonium.sounds.biome.Taiga;
import folk.sisby.euphonium.sounds.biome.TheEnd;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class EuphoniumBiome {
   private static final ISoundType<BiomeSound> BADLANDS = new Badlands();
   private static final ISoundType<BiomeSound> BEACH = new Beach();
   private static final ISoundType<BiomeSound> CAVES = new Caves();
   private static final ISoundType<BiomeSound> DESERT = new Desert();
   private static final ISoundType<BiomeSound> FOREST = new Forest();
   private static final ISoundType<BiomeSound> ICY = new Icy();
   private static final ISoundType<BiomeSound> JUNGLE = new Jungle();
   private static final ISoundType<BiomeSound> MOUNTAINS = new Mountains();
   private static final ISoundType<BiomeSound> OCEAN = new Ocean();
   private static final ISoundType<BiomeSound> PLAINS = new Plains();
   private static final ISoundType<BiomeSound> RIVER = new River();
   private static final ISoundType<BiomeSound> SAVANNA = new Savanna();
   private static final ISoundType<BiomeSound> SWAMP = new Swamp();
   private static final ISoundType<BiomeSound> TAIGA = new Taiga();
   private static final ISoundType<BiomeSound> THE_END = new TheEnd();
   public static List<net.minecraft.resources.Identifier> VALID_DIMENSIONS = new ArrayList<>();
   private static EuphoniumBiome.Handler handler;

   public static void init() {
      if (EuphoniumClient.CONFIG.biomeAmbienceEnabled) {
         ClientEntityEvents.ENTITY_LOAD.register(EuphoniumBiome::handleClientEntityJoin);
         ClientEntityEvents.ENTITY_UNLOAD.register(EuphoniumBiome::handleClientEntityLeave);
         ClientTickEvents.END_CLIENT_TICK.register(EuphoniumBiome::handleClientTick);
         EuphoniumClient.CONFIG.biomeAmbience.dimensions.forEach(dim -> VALID_DIMENSIONS.add(Identifier.tryParse(dim)));
      }
   }

   private static void handleClientTick(Minecraft client) {
      if (handler != null && !client.isPaused()) {
         handler.tick();
      }
   }

   private static void handleClientEntityLeave(Entity entity, Level level) {
      if (entity instanceof LocalPlayer && handler != null) {
         handler.stop();
      }
   }

   private static void handleClientEntityJoin(Entity entity, Level level) {
      if (entity instanceof LocalPlayer player) {
         trySetupSoundHandler(player);
      }
   }

   private static void trySetupSoundHandler(Player player) {
      if (player instanceof LocalPlayer) {
         if (handler == null) {
            handler = new EuphoniumBiome.Handler(player);
         }

         handler.updatePlayer(player);
      }
   }

   public static class Handler extends SoundHandler<BiomeSound> {
      public Handler(@NotNull Player player) {
         super(player);
         EuphoniumBiome.BADLANDS.addSounds(this);
         EuphoniumBiome.BEACH.addSounds(this);
         EuphoniumBiome.CAVES.addSounds(this);
         EuphoniumBiome.DESERT.addSounds(this);
         EuphoniumBiome.FOREST.addSounds(this);
         EuphoniumBiome.ICY.addSounds(this);
         EuphoniumBiome.JUNGLE.addSounds(this);
         EuphoniumBiome.MOUNTAINS.addSounds(this);
         EuphoniumBiome.OCEAN.addSounds(this);
         EuphoniumBiome.PLAINS.addSounds(this);
         EuphoniumBiome.RIVER.addSounds(this);
         EuphoniumBiome.SAVANNA.addSounds(this);
         EuphoniumBiome.SWAMP.addSounds(this);
         EuphoniumBiome.TAIGA.addSounds(this);
         EuphoniumBiome.THE_END.addSounds(this);
      }
   }
}
