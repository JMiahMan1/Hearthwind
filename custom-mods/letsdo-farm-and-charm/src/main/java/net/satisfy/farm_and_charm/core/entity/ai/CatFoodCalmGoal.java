package net.satisfy.farm_and_charm.core.entity.ai;

import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.feline.Cat;
import net.minecraft.world.entity.player.Player;
import net.satisfy.farm_and_charm.core.registry.ObjectRegistry;

import java.util.EnumSet;
import java.util.List;

/**
 * 26.2 replacement for the deleted CatAvoidEntityGoalMixin/CatTemptGoalMixin.
 * Cat's inner goals went package-private and can no longer be mixed into, so
 * instead this high-priority goal (2, ahead of vanilla scare ~6 / tempt ~8)
 * activates while a nearby player holds cat food: it stops navigation and
 * stares at the player, so the cat neither flees nor wanders while fed.
 */
public class CatFoodCalmGoal extends Goal {
    private final Cat cat;
    private Player feeder;

    public CatFoodCalmGoal(Cat cat) {
        this.cat = cat;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        List<? extends Player> players = cat.level().players();
        for (Player p : players) {
            if (p.distanceToSqr(cat) < 100.0
                    && p.isHolding(ObjectRegistry.CAT_FOOD.get().asItem())) {
                feeder = p;
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean canContinueToUse() {
        return feeder != null && feeder.isAlive()
                && feeder.distanceToSqr(cat) < 144.0
                && feeder.isHolding(ObjectRegistry.CAT_FOOD.get().asItem());
    }

    @Override
    public void start() {
        cat.getNavigation().stop();
    }

    @Override
    public void stop() {
        feeder = null;
    }

    @Override
    public void tick() {
        if (feeder != null) {
            cat.getLookControl().setLookAt(feeder, 30.0F, 30.0F);
        }
    }
}
