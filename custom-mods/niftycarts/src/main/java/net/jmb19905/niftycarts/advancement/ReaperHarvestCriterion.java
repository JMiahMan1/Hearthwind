package net.jmb19905.niftycarts.advancement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.advancements.predicates.entity.EntityPredicate;
import net.minecraft.advancements.triggers.SimpleCriterionTrigger.SimpleInstance;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class ReaperHarvestCriterion extends net.minecraft.advancements.triggers.SimpleCriterionTrigger<ReaperHarvestCriterion.TriggerInstance> {
   public void trigger(ServerPlayer serverPlayer, BlockState state) {
      super.trigger(serverPlayer, t -> t.matches(state));
   }

   @NotNull
   public Codec<ReaperHarvestCriterion.TriggerInstance> codec() {
      return ReaperHarvestCriterion.TriggerInstance.CODEC;
   }

   public record TriggerInstance(Optional<net.minecraft.advancements.predicates.ContextAwarePredicate> player, Optional<SimpleBlockPredicate> block)
      implements SimpleInstance {
      private static final Codec<ReaperHarvestCriterion.TriggerInstance> CODEC = RecordCodecBuilder.create(
         instance -> instance.group(
               EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(ReaperHarvestCriterion.TriggerInstance::player),
               SimpleBlockPredicate.CODEC.optionalFieldOf("block").forGetter(ReaperHarvestCriterion.TriggerInstance::block)
            )
            .apply(instance, ReaperHarvestCriterion.TriggerInstance::new)
      );

      public static net.minecraft.advancements.triggers.Criterion<ReaperHarvestCriterion.TriggerInstance> reaperHarvest() {
         return NCCriteriaTriggers.REAPER_HARVEST.createCriterion(new ReaperHarvestCriterion.TriggerInstance(Optional.empty(), Optional.empty()));
      }

      public static net.minecraft.advancements.triggers.Criterion<ReaperHarvestCriterion.TriggerInstance> reaperHarvest(SimpleBlockPredicate.Builder builder) {
         return NCCriteriaTriggers.REAPER_HARVEST.createCriterion(new ReaperHarvestCriterion.TriggerInstance(Optional.empty(), Optional.of(builder.build())));
      }

      public boolean matches(BlockState state) {
         return this.block.isEmpty() || this.block.get().test(state);
      }

      public Optional<net.minecraft.advancements.predicates.ContextAwarePredicate> player() {
         return this.player;
      }
   }
}
