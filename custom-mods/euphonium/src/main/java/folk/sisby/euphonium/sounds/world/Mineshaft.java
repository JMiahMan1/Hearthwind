package folk.sisby.euphonium.sounds.world;

import folk.sisby.euphonium.EuphoniumClient;
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

public class Mineshaft implements ISoundType<WorldSound> {
   public static SoundEvent SOUND;

   public Mineshaft() {
      SOUND = SoundEvent.createVariableRangeEvent(EuphoniumClient.id("world.mineshaft"));
   }

   @Override
   public void addSounds(SoundHandler<WorldSound> handler) {
      if (EuphoniumClient.CONFIG.worldAmbience.mineshaft) {
         handler.getSounds().add(new RepeatedWorldSound(handler.getPlayer()) {
            @Override
            public boolean isValidSituationCondition() {
               Optional<net.minecraft.core.BlockPos> rail = BlockPos.findClosestMatch(this.player.blockPosition(), 8, 16, pos -> {
                  Block block = this.level.getBlockState(pos).getBlock();
                  return block == Blocks.RAIL;
               });
               if (rail.isPresent()) {
                  this.setPos((BlockPos)rail.get());
                  return true;
               } else {
                  return false;
               }
            }

            @Override
            public boolean isValidPlayerCondition() {
               return true;
            }

            @Nullable
            @Override
            public SoundEvent getSound() {
               return Mineshaft.SOUND;
            }

            @Override
            public int getDelay() {
               return this.level.getRandom().nextInt(300) + 300;
            }

            @Override
            public float getVolume() {
               return 0.8F;
            }
         });
      }
   }
}
