package folk.sisby.euphonium;

import folk.sisby.euphonium.sound.ISoundType;
import folk.sisby.euphonium.sound.SoundHandler;
import folk.sisby.euphonium.sound.WorldSound;
import folk.sisby.euphonium.sounds.world.Alien;
import folk.sisby.euphonium.sounds.world.Bleak;
import folk.sisby.euphonium.sounds.world.CaveDepth;
import folk.sisby.euphonium.sounds.world.CaveDrone;
import folk.sisby.euphonium.sounds.world.Deepslate;
import folk.sisby.euphonium.sounds.world.Dry;
import folk.sisby.euphonium.sounds.world.Geode;
import folk.sisby.euphonium.sounds.world.Gravel;
import folk.sisby.euphonium.sounds.world.High;
import folk.sisby.euphonium.sounds.world.Mansion;
import folk.sisby.euphonium.sounds.world.Mineshaft;
import folk.sisby.euphonium.sounds.world.NightPlains;
import folk.sisby.euphonium.sounds.world.Snowstorm;
import folk.sisby.euphonium.sounds.world.UndergroundWater;
import folk.sisby.euphonium.sounds.world.Village;
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

public class EuphoniumWorld {
   public static final List<net.minecraft.resources.Identifier> VALID_CAVE_DIMENSIONS = new ArrayList<>();
   private static final ISoundType<WorldSound> ALIEN = new Alien();
   private static final ISoundType<WorldSound> BLEAK = new Bleak();
   private static final ISoundType<WorldSound> CAVE_DRONE = new CaveDrone();
   private static final ISoundType<WorldSound> CAVE_DEPTH = new CaveDepth();
   private static final ISoundType<WorldSound> DEEPSLATE = new Deepslate();
   private static final ISoundType<WorldSound> DRY = new Dry();
   private static final ISoundType<WorldSound> GEODE = new Geode();
   private static final ISoundType<WorldSound> GRAVEL = new Gravel();
   private static final ISoundType<WorldSound> HIGH = new High();
   private static final ISoundType<WorldSound> MANSION = new Mansion();
   private static final ISoundType<WorldSound> MINESHAFT = new Mineshaft();
   private static final ISoundType<WorldSound> NIGHT_PLAINS = new NightPlains();
   private static final ISoundType<WorldSound> SNOWSTORM = new Snowstorm();
   private static final ISoundType<WorldSound> UNDERGROUND_WATER = new UndergroundWater();
   private static final ISoundType<WorldSound> VILLAGE = new Village();
   private static EuphoniumWorld.Handler handler;

   public static void init() {
      if (EuphoniumClient.CONFIG.worldAmbienceEnabled) {
         ClientEntityEvents.ENTITY_LOAD.register(EuphoniumWorld::handleClientEntityJoin);
         ClientEntityEvents.ENTITY_UNLOAD.register(EuphoniumWorld::handleClientEntityLeave);
         ClientTickEvents.END_CLIENT_TICK.register(EuphoniumWorld::handleClientTick);
         EuphoniumClient.CONFIG.worldAmbience.caveDimensions.forEach(dim -> VALID_CAVE_DIMENSIONS.add(Identifier.tryParse(dim)));
      }
   }

   public static void addCaveAmbienceToDimension(Level level) {
      Identifier dimension = level.dimension().identifier();
      if (!VALID_CAVE_DIMENSIONS.contains(dimension)) {
         VALID_CAVE_DIMENSIONS.add(dimension);
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
            handler = new EuphoniumWorld.Handler(player);
         }

         handler.updatePlayer(player);
      }
   }

   public static class Handler extends SoundHandler<WorldSound> {
      public Handler(@NotNull Player player) {
         super(player);
         EuphoniumWorld.ALIEN.addSounds(this);
         EuphoniumWorld.BLEAK.addSounds(this);
         EuphoniumWorld.CAVE_DRONE.addSounds(this);
         EuphoniumWorld.CAVE_DEPTH.addSounds(this);
         EuphoniumWorld.DEEPSLATE.addSounds(this);
         EuphoniumWorld.DRY.addSounds(this);
         EuphoniumWorld.GEODE.addSounds(this);
         EuphoniumWorld.GRAVEL.addSounds(this);
         EuphoniumWorld.HIGH.addSounds(this);
         EuphoniumWorld.MANSION.addSounds(this);
         EuphoniumWorld.MINESHAFT.addSounds(this);
         EuphoniumWorld.NIGHT_PLAINS.addSounds(this);
         EuphoniumWorld.SNOWSTORM.addSounds(this);
         EuphoniumWorld.UNDERGROUND_WATER.addSounds(this);
         EuphoniumWorld.VILLAGE.addSounds(this);
      }
   }
}
