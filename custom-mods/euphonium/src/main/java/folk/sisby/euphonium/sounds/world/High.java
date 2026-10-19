package folk.sisby.euphonium.sounds.world;

import folk.sisby.euphonium.EuphoniumClient;
import folk.sisby.euphonium.helper.WorldHelper;
import folk.sisby.euphonium.sound.ISoundType;
import folk.sisby.euphonium.sound.RepeatedWorldSound;
import folk.sisby.euphonium.sound.SoundHandler;
import folk.sisby.euphonium.sound.WorldSound;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class High implements ISoundType<WorldSound> {
   public static SoundEvent SOUND;

   public High() {
      SOUND = SoundEvent.createVariableRangeEvent(EuphoniumClient.id("world.high"));
   }

   @Override
   public void addSounds(SoundHandler<WorldSound> handler) {
      if (EuphoniumClient.CONFIG.worldAmbience.high) {
         handler.getSounds().add(new RepeatedWorldSound(handler.getPlayer()) {
            @Override
            public boolean isValidSituationCondition() {
               int top = this.level.getMaxY() + 1 > 256 ? 200 : 150;
               return this.level.dimension() == Level.OVERWORLD && this.player.blockPosition().getY() > top;
            }

            @Override
            public boolean isValidPlayerCondition() {
               return this.level.dimension() == Level.OVERWORLD && WorldHelper.isOutside(this.player);
            }

            @Nullable
            @Override
            public SoundEvent getSound() {
               return High.SOUND;
            }

            @Override
            public int getDelay() {
               return this.level.getRandom().nextInt(100) + 100;
            }

            @Override
            public float getVolume() {
               return 0.85F;
            }
         });
      }
   }
}
