package folk.sisby.euphonium.sound;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.biome.Biome;
import org.jetbrains.annotations.Nullable;

public interface ISoundInstance {
   boolean isValid();

   boolean isValidPlayerCondition();

   void tick();

   void updatePlayer(Player var1);

   Player getPlayer();

   ClientLevel getLevel();

   @Nullable
   SoundEvent getSound();

   AbstractTickableSoundInstance getSoundInstance();

   default Biome getBiome(BlockPos pos) {
      return (Biome)this.getBiomeHolder(pos).value();
   }

   default net.minecraft.resources.ResourceKey<net.minecraft.world.level.biome.Biome> getBiomeKey(net.minecraft.core.BlockPos pos) {
      Biome biome = this.getBiome(pos);
      return (ResourceKey)this.getLevel().registryAccess().lookupOrThrow(Registries.BIOME).getResourceKey(biome).orElse(null);
   }

   default net.minecraft.core.Holder<net.minecraft.world.level.biome.Biome> getBiomeHolder(net.minecraft.core.BlockPos pos) {
      return this.getPlayer().level().getBiome(pos);
   }

   default Minecraft getMinecraft() {
      return Minecraft.getInstance();
   }

   default SoundManager getSoundManager() {
      return this.getMinecraft().getSoundManager();
   }

   default boolean isPlaying() {
      return this.getSoundInstance() != null && this.getSoundManager().isActive(this.getSoundInstance());
   }

   default void stop() {
      this.getSoundManager().stop(this.getSoundInstance());
   }

   default int getDelay() {
      return 0;
   }

   default float getVolume() {
      return 1.0F;
   }

   default float getPitch() {
      return 1.0F;
   }

   default double getVolumeScaling() {
      return 1.0;
   }
}
