package net.jmb19905.niftycarts.entity.ai.goal;

import java.util.EnumSet;
import net.jmb19905.niftycarts.util.NiftyWorld;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.Goal.Flag;

public final class PullCartGoal extends Goal {
   private final Entity mob;

   public PullCartGoal(Entity entity) {
      this.mob = entity;
      this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
   }

   public boolean canUse() {
      return NiftyWorld.get(this.mob.level()).isPulling(this.mob);
   }
}
