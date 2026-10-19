package folk.sisby.euphonium.sound;

import folk.sisby.euphonium.EuphoniumClient;
import java.util.ConcurrentModificationException;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.world.entity.player.Player;

public abstract class LoopedWorldSound extends WorldSound {
   protected LoopingSound soundInstance;

   public LoopedWorldSound(Player player) {
      super(player);
   }

   @Override
   public AbstractTickableSoundInstance getSoundInstance() {
      return this.soundInstance;
   }

   @Override
   public void tick() {
      boolean nowValid = this.isValid();
      if (this.isValid && !nowValid) {
         this.isValid = false;
      }

      if (!this.isValid && nowValid) {
         this.isValid = true;
      }

      if (this.isValid && !this.isPlaying()) {
         this.soundInstance = new LoopingSound(
            this.player, this.getSound(), (float)(this.getVolume() * this.getVolumeScaling()), this.getPitch(), p -> this.isValid
         );

         try {
            this.getSoundManager().play(this.soundInstance);
         } catch (ConcurrentModificationException var3) {
            EuphoniumClient.LOGGER.debug("{}: Exception in tick", this.getClass());
         }
      }
   }
}
