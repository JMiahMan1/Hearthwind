package folk.sisby.euphonium.sounds.world;

import folk.sisby.euphonium.EuphoniumClient;
import folk.sisby.euphonium.EuphoniumWorld;
import folk.sisby.euphonium.sound.ISoundType;
import folk.sisby.euphonium.sound.LoopedWorldSound;
import folk.sisby.euphonium.sound.SoundHandler;
import folk.sisby.euphonium.sound.WorldSound;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import org.jetbrains.annotations.Nullable;

public class CaveDrone implements ISoundType<WorldSound> {
   public static SoundEvent SOUND;

   public CaveDrone() {
      SOUND = SoundEvent.createVariableRangeEvent(EuphoniumClient.id("world.cave"));
   }

   @Override
   public void addSounds(SoundHandler<WorldSound> handler) {
      if (EuphoniumClient.CONFIG.worldAmbience.caveDrone) {
         handler.getSounds()
            .add(
               new LoopedWorldSound(handler.getPlayer()) {
                  @Override
                  public boolean isValidSituationCondition() {
                     BlockPos pos = this.player.blockPosition();
                     int light = this.level.getMaxLocalRawBrightness(pos);
                     if (!EuphoniumWorld.VALID_CAVE_DIMENSIONS.contains(this.level.dimension().identifier())) {
                        return false;
                     } else {
                        return !this.level.canSeeSkyFromBelowWater(pos) && pos.getY() <= this.player.level().getSeaLevel()
                           ? pos.getY() <= EuphoniumClient.CONFIG.worldAmbience.caveDroneDepth
                              || light <= EuphoniumClient.CONFIG.worldAmbience.caveLightLevel
                           : false;
                     }
                  }

                  @Override
                  public boolean isValidPlayerCondition() {
                     return !this.player.isUnderWater();
                  }

                  @Nullable
                  @Override
                  public SoundEvent getSound() {
                     return CaveDrone.SOUND;
                  }
               }
            );
      }
   }
}
