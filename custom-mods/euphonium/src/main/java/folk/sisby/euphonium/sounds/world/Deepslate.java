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

public class Deepslate implements ISoundType<WorldSound> {
   public static SoundEvent SOUND;

   public Deepslate() {
      SOUND = SoundEvent.createVariableRangeEvent(EuphoniumClient.id("world.deepslate"));
   }

   @Override
   public void addSounds(SoundHandler<WorldSound> handler) {
      if (EuphoniumClient.CONFIG.worldAmbience.deepslate) {
         handler.getSounds().add(new RepeatedWorldSound(handler.getPlayer()) {
            @Override
            public boolean isValidSituationCondition() {
               Optional<net.minecraft.core.BlockPos> optBlock = BlockPos.findClosestMatch(this.player.blockPosition(), 8, 4, pos -> {
                  Block block = this.level.getBlockState(pos).getBlock();
                  return block == Blocks.DEEPSLATE;
               });
               return optBlock.isPresent();
            }

            @Override
            public boolean isValidPlayerCondition() {
               return !WorldHelper.isOutside(this.player) && WorldHelper.isBelowSeaLevel(this.player);
            }

            @Nullable
            @Override
            public SoundEvent getSound() {
               return Deepslate.SOUND;
            }

            @Override
            public int getDelay() {
               return this.level.getRandom().nextInt(400) + 400;
            }

            @Override
            public float getVolume() {
               return 0.7F;
            }
         });
      }
   }
}
