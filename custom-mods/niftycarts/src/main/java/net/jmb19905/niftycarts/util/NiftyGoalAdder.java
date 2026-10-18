package net.jmb19905.niftycarts.util;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.UnmodifiableIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.goal.WrappedGoal;

public final class NiftyGoalAdder<T extends net.minecraft.world.entity.Entity> {
   private final Class<T> type;
   private final Function<T, net.minecraft.world.entity.ai.goal.GoalSelector> selector;
   private final ImmutableList<NiftyGoalAdder.GoalEntry<T>> goals;

   private NiftyGoalAdder(NiftyGoalAdder.Builder<T> builder) {
      this.type = builder.type;
      this.selector = builder.selector;
      this.goals = builder.goals.build();
   }

   public void onEntityJoinWorld(Entity entity) {
      if (!entity.level().isClientSide() && this.type.isInstance(entity)) {
         Set<net.minecraft.world.entity.ai.goal.WrappedGoal> oldGoals = this.getGoals((T)((Entity)this.type.cast(entity)));
         List<net.minecraft.world.entity.ai.goal.WrappedGoal> newGoals = new ArrayList<>(oldGoals.size() + this.goals.size());
         UnmodifiableIterator var4 = this.goals.iterator();

         while (var4.hasNext()) {
            NiftyGoalAdder.GoalEntry<T> goal = (NiftyGoalAdder.GoalEntry<T>)var4.next();
            newGoals.add(new WrappedGoal(goal.priority, (Goal)goal.factory.apply((T)((Entity)this.type.cast(entity)))));
         }

         newGoals.addAll(oldGoals);
         oldGoals.clear();
         oldGoals.addAll(newGoals);
      }
   }

   private Set<net.minecraft.world.entity.ai.goal.WrappedGoal> getGoals(T entity) {
      return ((GoalSelector)this.selector.apply(entity)).getAvailableGoals();
   }

   public static <T extends net.minecraft.world.entity.Mob> NiftyGoalAdder.Builder<T> mobGoal(Class<T> type) {
      return builder(type, (Function<T, net.minecraft.world.entity.ai.goal.GoalSelector>)(m -> m.goalSelector));
   }

   public static <T extends net.minecraft.world.entity.Mob> NiftyGoalAdder.Builder<T> mobTarget(Class<T> type) {
      return builder(type, (Function<T, net.minecraft.world.entity.ai.goal.GoalSelector>)(m -> m.targetSelector));
   }

   public static <T extends net.minecraft.world.entity.Entity> NiftyGoalAdder.Builder<T> builder(
      Class<T> type, Function<T, net.minecraft.world.entity.ai.goal.GoalSelector> selector
   ) {
      return new NiftyGoalAdder.Builder<>(type, selector);
   }

   public static final class Builder<T extends net.minecraft.world.entity.Entity> {
      private final Class<T> type;
      private final Function<T, net.minecraft.world.entity.ai.goal.GoalSelector> selector;
      private final com.google.common.collect.ImmutableList.Builder<NiftyGoalAdder.GoalEntry<T>> goals = new com.google.common.collect.ImmutableList.Builder();

      private Builder(Class<T> type, Function<T, net.minecraft.world.entity.ai.goal.GoalSelector> selector) {
         this.type = type;
         this.selector = selector;
      }

      public NiftyGoalAdder.Builder<T> add(int priority, Function<T, net.minecraft.world.entity.ai.goal.Goal> factory) {
         this.goals.add(new NiftyGoalAdder.GoalEntry<>(priority, factory));
         return this;
      }

      public NiftyGoalAdder<T> build() {
         return new NiftyGoalAdder<>(this);
      }
   }

   private record GoalEntry<T extends net.minecraft.world.entity.Entity>(int priority, Function<T, net.minecraft.world.entity.ai.goal.Goal> factory) {
   }
}
