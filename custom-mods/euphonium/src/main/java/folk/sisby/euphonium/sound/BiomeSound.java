package folk.sisby.euphonium.sound;

import folk.sisby.euphonium.EuphoniumBiome;
import folk.sisby.euphonium.EuphoniumClient;
import java.util.ConcurrentModificationException;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import org.slf4j.Logger;

public abstract class BiomeSound implements ISoundInstance {
   protected Minecraft client;
   protected boolean isValid = false;
   protected Player player;
   protected ClientLevel level;
   protected LoopingSound soundInstance = null;
   protected float blendScaling = 1.0F;
   protected float volumeScaleFade = 0.005F;

   protected BiomeSound(Player player) {
      this.client = Minecraft.getInstance();
      this.player = player;
      this.level = (ClientLevel)player.level();
   }

   public abstract boolean isValidBiomeCondition(
      net.minecraft.core.Holder<net.minecraft.world.level.biome.Biome> var1,
      net.minecraft.resources.ResourceKey<net.minecraft.world.level.biome.Biome> var2
   );

   @Override
   public void updatePlayer(Player player) {
      this.player = player;
      this.level = (ClientLevel)player.level();
   }

   @Override
   public ClientLevel getLevel() {
      return this.level;
   }

   @Override
   public Player getPlayer() {
      return this.player;
   }

   @Override
   public AbstractTickableSoundInstance getSoundInstance() {
      return this.soundInstance;
   }

   @Override
   public double getVolumeScaling() {
      return EuphoniumClient.CONFIG.biomeAmbience.volumeScaling;
   }

   @Override
   public void tick() {
      Logger log = EuphoniumClient.LOGGER;
      boolean nowValid = this.isValid();
      if (this.isValid && !nowValid) {
         this.isValid = false;
      }

      if (!this.isValid && nowValid) {
         this.isValid = true;
      }

      if (this.isValid) {
         double volume = this.getVolume() * this.getVolumeScaling() * this.blendScaling;
         if (!this.isPlaying()) {
            this.soundInstance = new LoopingSound(this.player, this.getSound(), (float)volume, this.getPitch(), p -> this.isValid);

            try {
               this.getSoundManager().play(this.soundInstance);
            } catch (ConcurrentModificationException var6) {
               log.debug("[{}] Exception in tick", this.getClass().getSimpleName());
            }
         } else if (this.soundInstance.maxVolume != volume) {
            if (this.soundInstance.maxVolume < volume) {
               this.soundInstance.maxVolume = this.soundInstance.maxVolume + this.volumeScaleFade;
            } else {
               this.soundInstance.maxVolume = this.soundInstance.maxVolume - this.volumeScaleFade;
            }
         }
      }
   }

   @Override
   public boolean isValid() {
      if (this.client.level == null || this.level == null) {
         return false;
      } else if (!this.player.isAlive()) {
         return false;
      } else if (!this.isValidPlayerCondition()) {
         return false;
      } else if (!EuphoniumBiome.VALID_DIMENSIONS.contains(this.level.dimension().identifier())) {
         return false;
      } else {
         BlockPos pos = this.player.blockPosition();
         float blend = EuphoniumClient.CONFIG.biomeAmbience.biomeBlend;
         if (blend > 0.0F) {
            for (Direction direction : List.of(Direction.EAST, Direction.WEST, Direction.NORTH, Direction.SOUTH)) {
               for (int i = 0; i < blend; i += 2) {
                  BlockPos relativePos = pos.relative(direction, i);
                  net.minecraft.core.Holder<net.minecraft.world.level.biome.Biome> holder = this.getBiomeHolder(relativePos);
                  net.minecraft.resources.ResourceKey<net.minecraft.world.level.biome.Biome> key = this.getBiomeKey(relativePos);
                  if (this.isValidBiomeCondition(holder, key)) {
                     this.blendScaling = 1.0F - i / blend;
                     return true;
                  }
               }
            }
         } else {
            net.minecraft.core.Holder<net.minecraft.world.level.biome.Biome> holder = this.getBiomeHolder(pos);
            net.minecraft.resources.ResourceKey<net.minecraft.world.level.biome.Biome> key = this.getBiomeKey(pos);
            if (this.isValidBiomeCondition(holder, key)) {
               this.blendScaling = 1.0F;
               return true;
            }
         }

         return false;
      }
   }
}
