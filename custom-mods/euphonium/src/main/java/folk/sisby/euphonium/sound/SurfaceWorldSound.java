package folk.sisby.euphonium.sound;

import folk.sisby.euphonium.EuphoniumClient;
import folk.sisby.euphonium.helper.WorldHelper;
import net.minecraft.world.entity.player.Player;

public abstract class SurfaceWorldSound extends RepeatedWorldSound {
   protected SurfaceWorldSound(Player player) {
      super(player);
   }

   @Override
   public boolean isValidPlayerCondition() {
      return WorldHelper.isOutside(this.getPlayer());
   }

   @Override
   public double getVolumeScaling() {
      int cullDistance = EuphoniumClient.CONFIG.worldAmbience.cullSoundAboveGround;
      return cullDistance > 0
         ? super.getVolumeScaling() * Math.max(0.0, 1.0 - WorldHelper.distanceFromGround(this.getPlayer(), cullDistance) / cullDistance)
         : super.getVolumeScaling();
   }
}
