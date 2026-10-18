package net.jmb19905.niftycarts.advancement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.jmb19905.niftycarts.entity.AbstractDrawnEntity;
import net.minecraft.advancements.predicates.entity.EntityPredicate;
import net.minecraft.advancements.predicates.entity.EntityTypePredicate;
import net.minecraft.advancements.triggers.SimpleCriterionTrigger.SimpleInstance;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

public class PullCartCriterion extends net.minecraft.advancements.triggers.SimpleCriterionTrigger<PullCartCriterion.TriggerInstance> {
   public void trigger(ServerPlayer serverPlayer, AbstractDrawnEntity entity, float dist, float fillLevel) {
      super.trigger(serverPlayer, t -> t.matches(entity, dist, fillLevel));
   }

   @NotNull
   public Codec<PullCartCriterion.TriggerInstance> codec() {
      return PullCartCriterion.TriggerInstance.CODEC;
   }

   public record TriggerInstance(
      Optional<net.minecraft.advancements.predicates.ContextAwarePredicate> player,
      Optional<net.minecraft.advancements.predicates.entity.EntityTypePredicate> type,
      Optional<Float> minDist,
      Optional<Integer> minPassengerCount,
      Optional<Float> fillLevel
   ) implements SimpleInstance {
      private static final Codec<PullCartCriterion.TriggerInstance> CODEC = RecordCodecBuilder.create(
         instance -> instance.group(
               EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(PullCartCriterion.TriggerInstance::player),
               EntityTypePredicate.CODEC.optionalFieldOf("type").forGetter(PullCartCriterion.TriggerInstance::type),
               Codec.FLOAT.optionalFieldOf("distance").forGetter(PullCartCriterion.TriggerInstance::minDist),
               Codec.INT.optionalFieldOf("passengers").forGetter(PullCartCriterion.TriggerInstance::minPassengerCount),
               Codec.FLOAT.optionalFieldOf("fill").forGetter(PullCartCriterion.TriggerInstance::fillLevel)
            )
            .apply(instance, PullCartCriterion.TriggerInstance::new)
      );

      public static net.minecraft.advancements.triggers.Criterion<PullCartCriterion.TriggerInstance> pullCart(
         net.minecraft.advancements.predicates.entity.EntityTypePredicate type
      ) {
         return NCCriteriaTriggers.PULL_CART
            .createCriterion(new PullCartCriterion.TriggerInstance(Optional.empty(), Optional.of(type), Optional.empty(), Optional.empty(), Optional.empty()));
      }

      public static net.minecraft.advancements.triggers.Criterion<PullCartCriterion.TriggerInstance> pullCartFill(
         net.minecraft.advancements.predicates.entity.EntityTypePredicate type, float fillLevel
      ) {
         return NCCriteriaTriggers.PULL_CART
            .createCriterion(
               new PullCartCriterion.TriggerInstance(Optional.empty(), Optional.of(type), Optional.empty(), Optional.empty(), Optional.of(fillLevel))
            );
      }

      public static net.minecraft.advancements.triggers.Criterion<PullCartCriterion.TriggerInstance> pullCartDist(
         net.minecraft.advancements.predicates.entity.EntityTypePredicate type, float minDist
      ) {
         return NCCriteriaTriggers.PULL_CART
            .createCriterion(new PullCartCriterion.TriggerInstance(Optional.empty(), Optional.of(type), Optional.of(minDist), Optional.empty(), Optional.empty()));
      }

      public static net.minecraft.advancements.triggers.Criterion<PullCartCriterion.TriggerInstance> pullCart(
         net.minecraft.advancements.predicates.entity.EntityTypePredicate type, float minDist, int minPassengerCount
      ) {
         return NCCriteriaTriggers.PULL_CART
            .createCriterion(
               new PullCartCriterion.TriggerInstance(
                  Optional.empty(), Optional.of(type), Optional.of(minDist), Optional.of(minPassengerCount), Optional.empty()
               )
            );
      }

      public boolean matches(AbstractDrawnEntity entity, float distance, float fillLevel) {
         return (this.type.isEmpty() || ((EntityTypePredicate)this.type.get()).matches(net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.wrapAsHolder(entity.getType())))
            && (this.minDist.isEmpty() || distance >= this.minDist.get())
            && (this.minPassengerCount.isEmpty() || entity.getPassengers().size() >= this.minPassengerCount.get())
            && (this.fillLevel.isEmpty() || fillLevel >= this.fillLevel.get());
      }

      public Optional<net.minecraft.advancements.predicates.ContextAwarePredicate> player() {
         return this.player;
      }
   }
}
