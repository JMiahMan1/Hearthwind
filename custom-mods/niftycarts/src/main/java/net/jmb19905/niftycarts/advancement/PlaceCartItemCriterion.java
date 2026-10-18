package net.jmb19905.niftycarts.advancement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.jmb19905.niftycarts.item.CartItem;
import net.minecraft.advancements.predicates.entity.EntityPredicate;
import net.minecraft.advancements.predicates.ItemPredicate;
import net.minecraft.advancements.predicates.ItemPredicate.Builder;
import net.minecraft.advancements.triggers.SimpleCriterionTrigger.SimpleInstance;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.NotNull;

public class PlaceCartItemCriterion extends net.minecraft.advancements.triggers.SimpleCriterionTrigger<PlaceCartItemCriterion.TriggerInstance> {
   public void trigger(ServerPlayer serverPlayer, ItemStack stack) {
      super.trigger(serverPlayer, ti -> ti.matches(stack));
   }

   @NotNull
   public Codec<PlaceCartItemCriterion.TriggerInstance> codec() {
      return PlaceCartItemCriterion.TriggerInstance.CODEC;
   }

   public record TriggerInstance(
      Optional<net.minecraft.advancements.predicates.ContextAwarePredicate> player, Optional<net.minecraft.advancements.predicates.ItemPredicate> item
   ) implements SimpleInstance {
      private static final Codec<PlaceCartItemCriterion.TriggerInstance> CODEC = RecordCodecBuilder.create(
         instance -> instance.group(
               EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(PlaceCartItemCriterion.TriggerInstance::player),
               ItemPredicate.CODEC.optionalFieldOf("item").forGetter(PlaceCartItemCriterion.TriggerInstance::item)
            )
            .apply(instance, PlaceCartItemCriterion.TriggerInstance::new)
      );

      public static net.minecraft.advancements.triggers.Criterion<PlaceCartItemCriterion.TriggerInstance> placeCart() {
         return NCCriteriaTriggers.PLACE_CART_ITEM.createCriterion(new PlaceCartItemCriterion.TriggerInstance(Optional.empty(), Optional.empty()));
      }

      public static net.minecraft.advancements.triggers.Criterion<PlaceCartItemCriterion.TriggerInstance> placeCart(CartItem item) {
         return placeCart(Builder.item().of(BuiltInRegistries.ITEM, new ItemLike[]{item}));
      }

      public static net.minecraft.advancements.triggers.Criterion<PlaceCartItemCriterion.TriggerInstance> placeCart(
         net.minecraft.advancements.predicates.ItemPredicate.Builder builder
      ) {
         return NCCriteriaTriggers.PLACE_CART_ITEM
            .createCriterion(new PlaceCartItemCriterion.TriggerInstance(Optional.empty(), Optional.of(builder.build())));
      }

      public boolean matches(ItemStack stack) {
         return this.item.isEmpty() || ((ItemPredicate)this.item.get()).test(stack);
      }

      public Optional<net.minecraft.advancements.predicates.ContextAwarePredicate> player() {
         return this.player;
      }
   }
}
