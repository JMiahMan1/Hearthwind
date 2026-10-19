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
import net.minecraft.world.level.block.AmethystBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.Nullable;

public class Geode implements ISoundType<WorldSound> {
   public static SoundEvent SOUND;

   public Geode() {
      SOUND = SoundEvent.createVariableRangeEvent(EuphoniumClient.id("world.geode"));
   }

   @Override
   public void addSounds(SoundHandler<WorldSound> handler) {
      if (EuphoniumClient.CONFIG.worldAmbience.geode) {
         handler.getSounds().add(new RepeatedWorldSound(handler.getPlayer()) {
            @Override
            public boolean isValidSituationCondition() {
               Optional<net.minecraft.core.BlockPos> optAmethyst = BlockPos.findClosestMatch(this.player.blockPosition(), 12, 8, pos -> {
                  Block block = this.level.getBlockState(pos).getBlock();
                  return block instanceof AmethystBlock;
               });
               Optional<net.minecraft.core.BlockPos> optSmoothBasalt = BlockPos.findClosestMatch(this.player.blockPosition(), 12, 8, pos -> {
                  Block block = this.level.getBlockState(pos).getBlock();
                  return block == Blocks.SMOOTH_BASALT;
               });
               if (optAmethyst.isPresent() && optSmoothBasalt.isPresent()) {
                  this.setPos((BlockPos)optAmethyst.get());
                  return true;
               } else {
                  return false;
               }
            }

            @Override
            public boolean isValidPlayerCondition() {
               return !WorldHelper.isOutside(this.player) && WorldHelper.isBelowSeaLevel(this.player);
            }

            @Nullable
            @Override
            public SoundEvent getSound() {
               return Geode.SOUND;
            }

            @Override
            public int getDelay() {
               return this.level.getRandom().nextInt(350) + 300;
            }

            @Override
            public float getVolume() {
               return 0.5F;
            }

            @Override
            public float getPitch() {
               return 0.8F + 0.4F * this.level.getRandom().nextFloat();
            }
         });
      }
   }
}
