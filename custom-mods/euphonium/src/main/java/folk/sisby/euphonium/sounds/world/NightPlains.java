package folk.sisby.euphonium.sounds.world;

import folk.sisby.euphonium.EuphoniumClient;
import folk.sisby.euphonium.helper.WorldHelper;
import folk.sisby.euphonium.sound.ISoundType;
import folk.sisby.euphonium.sound.SoundHandler;
import folk.sisby.euphonium.sound.SurfaceWorldSound;
import folk.sisby.euphonium.sound.WorldSound;
import folk.sisby.euphonium.sounds.biome.Plains;
import folk.sisby.euphonium.sounds.biome.Savanna;
import net.minecraft.sounds.SoundEvent;
import org.jetbrains.annotations.Nullable;

public class NightPlains implements ISoundType<WorldSound> {
   public static SoundEvent SOUND;

   public NightPlains() {
      SOUND = SoundEvent.createVariableRangeEvent(EuphoniumClient.id("world.night_plains"));
   }

   @Override
   public void addSounds(SoundHandler<WorldSound> handler) {
      if (EuphoniumClient.CONFIG.worldAmbience.nightPlains) {
         handler.getSounds().add(new SurfaceWorldSound(handler.getPlayer()) {
            @Override
            public boolean isValidSituationCondition() {
               net.minecraft.core.Holder<net.minecraft.world.level.biome.Biome> holder = this.getBiomeHolder(this.player.blockPosition());
               net.minecraft.resources.ResourceKey<net.minecraft.world.level.biome.Biome> key = this.getBiomeKey(this.player.blockPosition());
               return Plains.VALID_BIOME.test(holder, key) || Savanna.VALID_BIOME.test(holder);
            }

            @Override
            public boolean isValidPlayerCondition() {
               return WorldHelper.isNight(this.player) && WorldHelper.isOutside(this.player) && !WorldHelper.isBelowSeaLevel(this.player);
            }

            @Nullable
            @Override
            public SoundEvent getSound() {
               return NightPlains.SOUND;
            }

            @Override
            public int getDelay() {
               return this.level.getRandom().nextInt(500) + 500;
            }

            @Override
            public float getVolume() {
               return 0.6F;
            }
         });
      }
   }
}
