package folk.sisby.euphonium.sound;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public class SoundHandler<T extends ISoundInstance> {
   private final List<T> sounds = new ArrayList<>();
   private Player player;

   public SoundHandler(@NotNull Player player) {
      this.updatePlayer(player);
   }

   public void updatePlayer(@NotNull Player player) {
      this.player = player;
      this.sounds.forEach(s -> s.updatePlayer(this.player));
   }

   public List<T> getSounds() {
      return this.sounds;
   }

   public Player getPlayer() {
      return this.player;
   }

   public void tick() {
      if (this.player != null) {
         this.sounds.forEach(ISoundInstance::tick);
      }
   }

   public void stop() {
      this.sounds.forEach(ISoundInstance::stop);
   }
}
