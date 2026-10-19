package folk.sisby.euphonium.sounds.world;

import folk.sisby.euphonium.EuphoniumClient;
import folk.sisby.euphonium.helper.WorldHelper;
import folk.sisby.euphonium.sound.ISoundType;
import folk.sisby.euphonium.sound.RepeatedWorldSound;
import folk.sisby.euphonium.sound.SoundHandler;
import folk.sisby.euphonium.sound.WorldSound;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.Nullable;

public class UndergroundWater implements ISoundType<WorldSound> {
   public static SoundEvent SOUND;

   public UndergroundWater() {
      SOUND = SoundEvent.createVariableRangeEvent(EuphoniumClient.id("world.underground_water"));
   }

   @Override
   public void addSounds(SoundHandler<WorldSound> handler) {
      if (EuphoniumClient.CONFIG.worldAmbience.undergroundWater) {
         handler.getSounds().add(new RepeatedWorldSound(handler.getPlayer()) {
            @Override
            public boolean isValidSituationCondition() {
               Optional<net.minecraft.core.BlockPos> optWater = BlockPos.findClosestMatch(this.player.blockPosition(), 12, 8, pos -> {
                  Block block = this.level.getBlockState(pos).getBlock();
                  return block == Blocks.WATER;
               });
               if (optWater.isPresent()) {
                  this.setPos((BlockPos)optWater.get());
                  return true;
               } else {
                  return false;
               }
            }

            @Override
            public boolean isValidPlayerCondition() {
               return !WorldHelper.isOutside(this.player) && !this.player.isUnderWater() && WorldHelper.isBelowSeaLevel(this.player);
            }

            @Nullable
            @Override
            public SoundEvent getSound() {
               return UndergroundWater.SOUND;
            }

            @Override
            public int getDelay() {
               return this.level.getRandom().nextInt(150) + 120;
            }

            @Override
            public float getVolume() {
               return 0.3F;
            }

            @Override
            public float getPitch() {
               return 0.77F + 0.3F * this.level.getRandom().nextFloat();
            }
         });
      }
   }
}
