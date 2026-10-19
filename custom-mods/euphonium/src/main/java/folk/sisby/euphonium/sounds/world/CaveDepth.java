package folk.sisby.euphonium.sounds.world;

import folk.sisby.euphonium.EuphoniumClient;
import folk.sisby.euphonium.EuphoniumWorld;
import folk.sisby.euphonium.sound.ISoundType;
import folk.sisby.euphonium.sound.LoopedWorldSound;
import folk.sisby.euphonium.sound.SoundHandler;
import folk.sisby.euphonium.sound.WorldSound;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.biome.Biomes;
import org.jetbrains.annotations.Nullable;

public class CaveDepth implements ISoundType<WorldSound> {
   public static SoundEvent SOUND;

   public CaveDepth() {
      SOUND = SoundEvent.createVariableRangeEvent(EuphoniumClient.id("world.deep_cave"));
   }

   @Override
   public void addSounds(SoundHandler<WorldSound> handler) {
      if (EuphoniumClient.CONFIG.worldAmbience.caveDepth) {
         handler.getSounds().add(new LoopedWorldSound(handler.getPlayer()) {
            @Override
            public boolean isValidSituationCondition() {
               BlockPos pos = this.player.blockPosition();
               net.minecraft.resources.ResourceKey<net.minecraft.world.level.biome.Biome> key = this.getBiomeKey(pos);
               if (key == Biomes.DEEP_DARK) {
                  return false;
               } else if (!EuphoniumWorld.VALID_CAVE_DIMENSIONS.contains(this.level.dimension().identifier())) {
                  return false;
               } else {
                  int light = this.level.getMaxLocalRawBrightness(pos);
                  int bottom = this.level.getMinY() < 0 ? 0 : 32;
                  return !this.level.canSeeSkyFromBelowWater(pos) && pos.getY() <= bottom && light < EuphoniumClient.CONFIG.worldAmbience.caveLightLevel;
               }
            }

            @Override
            public boolean isValidPlayerCondition() {
               return !this.player.isUnderWater();
            }

            @Nullable
            @Override
            public SoundEvent getSound() {
               return CaveDepth.SOUND;
            }
         });
      }
   }
}
