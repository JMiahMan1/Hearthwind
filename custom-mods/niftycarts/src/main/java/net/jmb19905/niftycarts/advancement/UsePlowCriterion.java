package net.jmb19905.niftycarts.advancement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.advancements.predicates.entity.EntityPredicate;
import net.minecraft.advancements.predicates.ItemPredicate;
import net.minecraft.advancements.triggers.SimpleCriterionTrigger.SimpleInstance;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class UsePlowCriterion extends net.minecraft.advancements.triggers.SimpleCriterionTrigger<UsePlowCriterion.TriggerInstance> {
   @NotNull
   public Codec<UsePlowCriterion.TriggerInstance> codec() {
      return UsePlowCriterion.TriggerInstance.CODEC;
   }

   public void trigger(ServerPlayer serverPlayer, ItemStack stack) {
      super.trigger(serverPlayer, ti -> ti.matches(stack));
   }

   public record TriggerInstance(
      Optional<net.minecraft.advancements.predicates.ContextAwarePredicate> player, Optional<net.minecraft.advancements.predicates.ItemPredicate> item
   ) implements SimpleInstance {
      private static final Codec<UsePlowCriterion.TriggerInstance> CODEC = RecordCodecBuilder.create(
         instance -> instance.group(
               EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(UsePlowCriterion.TriggerInstance::player),
               ItemPredicate.CODEC.optionalFieldOf("item").forGetter(UsePlowCriterion.TriggerInstance::item)
            )
            .apply(instance, UsePlowCriterion.TriggerInstance::new)
      );

      public static net.minecraft.advancements.triggers.Criterion<UsePlowCriterion.TriggerInstance> usePlow() {
         return NCCriteriaTriggers.USE_PLOW.createCriterion(new UsePlowCriterion.TriggerInstance(Optional.empty(), Optional.empty()));
      }

      public static net.minecraft.advancements.triggers.Criterion<UsePlowCriterion.TriggerInstance> usePlow(
         net.minecraft.advancements.predicates.ItemPredicate.Builder builder
      ) {
         return NCCriteriaTriggers.USE_PLOW.createCriterion(new UsePlowCriterion.TriggerInstance(Optional.empty(), Optional.of(builder.build())));
      }

      public boolean matches(ItemStack stack) {
         return this.item.isEmpty() || ((ItemPredicate)this.item.get()).test(stack);
      }

      public Optional<net.minecraft.advancements.predicates.ContextAwarePredicate> player() {
         return this.player;
      }
   }
}
