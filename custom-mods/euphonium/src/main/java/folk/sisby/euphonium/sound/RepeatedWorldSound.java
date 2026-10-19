package folk.sisby.euphonium.sound;

import folk.sisby.euphonium.EuphoniumClient;
import java.util.ConcurrentModificationException;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public abstract class RepeatedWorldSound extends WorldSound {
   protected SingleSound soundInstance;
   protected BlockPos pos;
   protected int soundTicks = 100;

   public RepeatedWorldSound(Player player) {
      super(player);
   }

   @Override
   public AbstractTickableSoundInstance getSoundInstance() {
      return this.soundInstance;
   }

   @Override
   public void tick() {
      if (--this.soundTicks < 0) {
         this.soundTicks = this.getDelay();
         this.isValid = this.isValid();
         if (this.isValid) {
            this.soundInstance = new SingleSound(
               this.getPlayer(), this.getSound(), (float)(this.getVolume() * this.getVolumeScaling()), this.getPitch(), this.getPos()
            );
            SoundManager manager = this.getSoundManager();

            try {
               if (!manager.isActive(this.soundInstance)) {
                  manager.play(this.soundInstance);
               }
            } catch (ConcurrentModificationException var3) {
               EuphoniumClient.LOGGER.debug("{}: Exception in manager.play", this.getClass());
            }
         }
      }
   }

   @Override
   public int getDelay() {
      return this.level.getRandom().nextInt(200) + 200;
   }

   @Nullable
   public BlockPos getPos() {
      return this.pos;
   }

   public void setPos(BlockPos pos) {
      this.pos = pos;
   }
}
