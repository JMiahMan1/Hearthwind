package folk.sisby.euphonium.sound;

import folk.sisby.euphonium.EuphoniumClient;
import java.util.function.Predicate;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;

public class LoopingSound extends AbstractTickableSoundInstance {
   public static int FADE_TIME = 140;
   private final Player player;
   private final Predicate<net.minecraft.world.entity.player.Player> predicate;
   public float maxVolume;
   private int longTicks;

   public LoopingSound(
      net.minecraft.world.entity.player.Player player,
      net.minecraft.sounds.SoundEvent sound,
      float volume,
      float pitch,
      Predicate<net.minecraft.world.entity.player.Player> predicate
   ) {
      super(sound, EuphoniumClient.CONFIG.channel, RandomSource.create());
      this.maxVolume = volume;
      this.player = player;
      this.looping = true;
      this.delay = 0;
      this.volume = 0.01F;
      this.pitch = pitch;
      this.relative = true;
      this.predicate = predicate;
      this.longTicks = -50;
   }

   public void tick() {
      if (this.player.isAlive()) {
         if (this.predicate.test(this.player)) {
            this.longTicks++;
         } else {
            this.longTicks--;
         }

         this.longTicks = Math.min(this.longTicks, FADE_TIME);
         this.volume = Math.max(0.0F, Math.min((float)this.longTicks / FADE_TIME, 1.0F)) * this.maxVolume;
         boolean donePlaying = this.isStopped();
         if (!donePlaying && this.volume == 0.0F && this.longTicks < -100) {
            this.stop();
         }
      } else {
         this.stop();
      }
   }
}
