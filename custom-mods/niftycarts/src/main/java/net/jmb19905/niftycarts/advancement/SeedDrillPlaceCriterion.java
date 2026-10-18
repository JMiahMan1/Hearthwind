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

public class SeedDrillPlaceCriterion extends net.minecraft.advancements.triggers.SimpleCriterionTrigger<SeedDrillPlaceCriterion.TriggerInstance> {
   @NotNull
   public Codec<SeedDrillPlaceCriterion.TriggerInstance> codec() {
      return SeedDrillPlaceCriterion.TriggerInstance.CODEC;
   }

   public void trigger(ServerPlayer serverPlayer, ItemStack stack) {
      super.trigger(serverPlayer, t -> t.matches(stack));
   }

   public record TriggerInstance(
      Optional<net.minecraft.advancements.predicates.ContextAwarePredicate> player, Optional<net.minecraft.advancements.predicates.ItemPredicate> item
   ) implements SimpleInstance {
      private static final Codec<SeedDrillPlaceCriterion.TriggerInstance> CODEC = RecordCodecBuilder.create(
         instance -> instance.group(
               EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(SeedDrillPlaceCriterion.TriggerInstance::player),
               ItemPredicate.CODEC.optionalFieldOf("item").forGetter(SeedDrillPlaceCriterion.TriggerInstance::item)
            )
            .apply(instance, SeedDrillPlaceCriterion.TriggerInstance::new)
      );

      public static net.minecraft.advancements.triggers.Criterion<SeedDrillPlaceCriterion.TriggerInstance> seedDrillPlace() {
         return NCCriteriaTriggers.SEED_DRILL_PLACE.createCriterion(new SeedDrillPlaceCriterion.TriggerInstance(Optional.empty(), Optional.empty()));
      }

      public static net.minecraft.advancements.triggers.Criterion<SeedDrillPlaceCriterion.TriggerInstance> seedDrillPlace(
         net.minecraft.advancements.predicates.ItemPredicate.Builder builder
      ) {
         return NCCriteriaTriggers.SEED_DRILL_PLACE
            .createCriterion(new SeedDrillPlaceCriterion.TriggerInstance(Optional.empty(), Optional.of(builder.build())));
      }

      public boolean matches(ItemStack stack) {
         return this.item.isEmpty() || ((ItemPredicate)this.item.get()).test(stack);
      }

      public Optional<net.minecraft.advancements.predicates.ContextAwarePredicate> player() {
         return this.player;
      }
   }
}
