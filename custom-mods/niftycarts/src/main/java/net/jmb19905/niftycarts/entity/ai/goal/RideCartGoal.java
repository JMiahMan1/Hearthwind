package net.jmb19905.niftycarts.entity.ai.goal;

import java.util.EnumSet;
import net.jmb19905.niftycarts.entity.AbstractDrawnEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.Goal.Flag;

public final class RideCartGoal extends Goal {
   private final Entity mob;

   public RideCartGoal(Entity mob) {
      this.mob = mob;
      this.setFlags(EnumSet.of(Flag.MOVE, Flag.JUMP));
   }

   public boolean canUse() {
      return this.mob.getVehicle() instanceof AbstractDrawnEntity;
   }
}
