package folk.sisby.euphonium.sounds.world;

import folk.sisby.euphonium.EuphoniumClient;
import folk.sisby.euphonium.helper.WorldHelper;
import folk.sisby.euphonium.sound.ISoundType;
import folk.sisby.euphonium.sound.RepeatedWorldSound;
import folk.sisby.euphonium.sound.SoundHandler;
import folk.sisby.euphonium.sound.WorldSound;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

public class Mansion implements ISoundType<WorldSound> {
   public static SoundEvent SOUND;

   public Mansion() {
      SOUND = SoundEvent.createVariableRangeEvent(EuphoniumClient.id("world.mansion"));
   }

   @Override
   public void addSounds(SoundHandler<WorldSound> handler) {
      if (EuphoniumClient.CONFIG.worldAmbience.mansion) {
         handler.getSounds().add(new RepeatedWorldSound(handler.getPlayer()) {
            @Override
            public boolean isValidSituationCondition() {
               AABB bb = new AABB(this.player.blockPosition()).inflate(10.0);
               List<net.minecraft.world.entity.monster.Monster> monsters = this.level.getEntitiesOfClass(Monster.class, bb);
               Optional<net.minecraft.core.BlockPos> optBlock1 = BlockPos.findClosestMatch(this.player.blockPosition(), 8, 8, pos -> {
                  Block block = this.level.getBlockState(pos).getBlock();
                  return block == Blocks.DARK_OAK_PLANKS;
               });
               Optional<net.minecraft.core.BlockPos> optBlock2 = BlockPos.findClosestMatch(this.player.blockPosition(), 8, 8, pos -> {
                  Block block = this.level.getBlockState(pos).getBlock();
                  return block == Blocks.BIRCH_PLANKS;
               });
               if (optBlock1.isPresent() && optBlock2.isPresent()) {
                  Optional<net.minecraft.world.entity.monster.Monster> optMonster = monsters.stream().findAny();
                  if (optMonster.isPresent()) {
                     this.setPos(((Monster)optMonster.get()).blockPosition());
                     return true;
                  }
               }

               return false;
            }

            @Override
            public boolean isValidPlayerCondition() {
               return !WorldHelper.isBelowSeaLevel(this.player);
            }

            @Nullable
            @Override
            public SoundEvent getSound() {
               return Mansion.SOUND;
            }

            @Override
            public int getDelay() {
               return this.level.getRandom().nextInt(200) + 400;
            }

            @Override
            public float getVolume() {
               return 0.82F;
            }
         });
      }
   }
}
