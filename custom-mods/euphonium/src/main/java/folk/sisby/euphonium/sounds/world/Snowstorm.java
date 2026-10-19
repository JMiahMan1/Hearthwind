package folk.sisby.euphonium.sounds.world;

import folk.sisby.euphonium.EuphoniumClient;
import folk.sisby.euphonium.helper.WorldHelper;
import folk.sisby.euphonium.sound.ISoundType;
import folk.sisby.euphonium.sound.RepeatedWorldSound;
import folk.sisby.euphonium.sound.SoundHandler;
import folk.sisby.euphonium.sound.WorldSound;
import folk.sisby.euphonium.sounds.biome.Icy;
import net.minecraft.sounds.SoundEvent;
import org.jetbrains.annotations.Nullable;

public class Snowstorm implements ISoundType<WorldSound> {
   public static SoundEvent SOUND;

   public Snowstorm() {
      SOUND = SoundEvent.createVariableRangeEvent(EuphoniumClient.id("world.snowstorm"));
   }

   @Override
   public void addSounds(SoundHandler<WorldSound> handler) {
      if (EuphoniumClient.CONFIG.worldAmbience.snowstorm) {
         handler.getSounds().add(new RepeatedWorldSound(handler.getPlayer()) {
            @Override
            public boolean isValidSituationCondition() {
               net.minecraft.core.Holder<net.minecraft.world.level.biome.Biome> holder = this.getBiomeHolder(this.player.blockPosition());
               return Icy.VALID_BIOME.test(holder);
            }

            @Override
            public boolean isValidPlayerCondition() {
               return WorldHelper.isOutside(this.player) && this.getLevel().isThundering();
            }

            @Nullable
            @Override
            public SoundEvent getSound() {
               return Snowstorm.SOUND;
            }

            @Override
            public int getDelay() {
               return this.level.getRandom().nextInt(250) + 250;
            }

            @Override
            public float getVolume() {
               return 0.9F;
            }
         });
      }
   }
}
