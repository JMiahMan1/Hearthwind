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

public class SteerCartCriterion extends net.minecraft.advancements.triggers.SimpleCriterionTrigger<SteerCartCriterion.TriggerInstance> {
   @NotNull
   public Codec<SteerCartCriterion.TriggerInstance> codec() {
      return SteerCartCriterion.TriggerInstance.CODEC;
   }

   public void trigger(ServerPlayer serverPlayer, AbstractDrawnEntity cart, float distance) {
      super.trigger(serverPlayer, t -> t.matches(cart, distance));
   }

   public record TriggerInstance(
      Optional<net.minecraft.advancements.predicates.ContextAwarePredicate> player,
      Optional<net.minecraft.advancements.predicates.entity.EntityTypePredicate> type,
      Optional<Float> minDist
   ) implements SimpleInstance {
      private static final Codec<SteerCartCriterion.TriggerInstance> CODEC = RecordCodecBuilder.create(
         instance -> instance.group(
               EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(SteerCartCriterion.TriggerInstance::player),
               EntityTypePredicate.CODEC.optionalFieldOf("type").forGetter(SteerCartCriterion.TriggerInstance::type),
               Codec.FLOAT.optionalFieldOf("distance").forGetter(SteerCartCriterion.TriggerInstance::minDist)
            )
            .apply(instance, SteerCartCriterion.TriggerInstance::new)
      );

      public static net.minecraft.advancements.triggers.Criterion<SteerCartCriterion.TriggerInstance> steerCart() {
         return NCCriteriaTriggers.STEER_CART.createCriterion(new SteerCartCriterion.TriggerInstance(Optional.empty(), Optional.empty(), Optional.empty()));
      }

      public static net.minecraft.advancements.triggers.Criterion<SteerCartCriterion.TriggerInstance> steerCart(
         net.minecraft.advancements.predicates.entity.EntityTypePredicate type, float minDist
      ) {
         return NCCriteriaTriggers.STEER_CART.createCriterion(new SteerCartCriterion.TriggerInstance(Optional.empty(), Optional.of(type), Optional.of(minDist)));
      }

      public boolean matches(AbstractDrawnEntity cart, float distance) {
         return (this.minDist.isEmpty() || distance >= this.minDist.get())
            && (this.type.isEmpty() || ((EntityTypePredicate)this.type.get()).matches(net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.wrapAsHolder(cart.getType())));
      }

      public Optional<net.minecraft.advancements.predicates.ContextAwarePredicate> player() {
         return this.player;
      }
   }
}
