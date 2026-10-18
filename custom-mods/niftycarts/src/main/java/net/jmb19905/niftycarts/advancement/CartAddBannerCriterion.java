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

public class CartAddBannerCriterion extends net.minecraft.advancements.triggers.SimpleCriterionTrigger<CartAddBannerCriterion.TriggerInstance> {
   @NotNull
   public Codec<CartAddBannerCriterion.TriggerInstance> codec() {
      return CartAddBannerCriterion.TriggerInstance.CODEC;
   }

   public void trigger(ServerPlayer serverPlayer, ItemStack stack) {
      super.trigger(serverPlayer, t -> t.matches(stack));
   }

   public record TriggerInstance(
      Optional<net.minecraft.advancements.predicates.ContextAwarePredicate> player, Optional<net.minecraft.advancements.predicates.ItemPredicate> item
   ) implements SimpleInstance {
      public static final Codec<CartAddBannerCriterion.TriggerInstance> CODEC = RecordCodecBuilder.create(
         instance -> instance.group(
               EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(CartAddBannerCriterion.TriggerInstance::player),
               ItemPredicate.CODEC.optionalFieldOf("item").forGetter(CartAddBannerCriterion.TriggerInstance::item)
            )
            .apply(instance, CartAddBannerCriterion.TriggerInstance::new)
      );

      public boolean matches(ItemStack stack) {
         return this.item.isEmpty() || ((ItemPredicate)this.item.get()).test(stack);
      }

      public static net.minecraft.advancements.triggers.Criterion<CartAddBannerCriterion.TriggerInstance> usedBanner(
         net.minecraft.advancements.predicates.ItemPredicate.Builder builder
      ) {
         return NCCriteriaTriggers.CART_ADD_BANNER
            .createCriterion(new CartAddBannerCriterion.TriggerInstance(Optional.empty(), Optional.of(builder.build())));
      }

      @NotNull
      public Optional<net.minecraft.advancements.predicates.ContextAwarePredicate> player() {
         return this.player;
      }
   }
}
