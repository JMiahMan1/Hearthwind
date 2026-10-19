package folk.sisby.euphonium.sound;

import folk.sisby.euphonium.EuphoniumClient;
import folk.sisby.euphonium.helper.WorldHelper;
import net.minecraft.world.entity.player.Player;

public abstract class SurfaceBiomeSound extends BiomeSound {
   protected boolean playWhenThundering;

   protected SurfaceBiomeSound(Player player, boolean playWhenThundering) {
      super(player);
      this.playWhenThundering = playWhenThundering;
   }

   @Override
   public boolean isValidPlayerCondition() {
      return WorldHelper.isThundering(this.getPlayer()) && !this.playWhenThundering ? false : WorldHelper.isOutside(this.getPlayer());
   }

   @Override
   public double getVolumeScaling() {
      int cullDistance = EuphoniumClient.CONFIG.biomeAmbience.cullSoundAboveGround;
      if (cullDistance > 0) {
         float distanceFromGround = WorldHelper.distanceFromGround(this.getPlayer(), cullDistance);
         double multiplier = 1.0 - distanceFromGround / cullDistance;
         return super.getVolumeScaling() * Math.max(0.0, multiplier);
      } else {
         return super.getVolumeScaling();
      }
   }
}
