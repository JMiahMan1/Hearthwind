package ht.treechop.common.loot;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import ht.treechop.TreeChop;
import java.util.Set;
import net.minecraft.resources.Identifier;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.level.storage.loot.IntRange;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public record CountBlockChopsLootItemCondition(IntRange range) implements LootItemCondition {
   public static final Identifier ID = TreeChop.resource("count_block_chops");
   public static final MapCodec<CountBlockChopsLootItemCondition> CODEC = RecordCodecBuilder.mapCodec(
      p_297208_ -> p_297208_.group(IntRange.CODEC.fieldOf("range").forGetter(CountBlockChopsLootItemCondition::range))
         .apply(p_297208_, CountBlockChopsLootItemCondition::new)
   );

   @Override
   public MapCodec<? extends LootItemCondition> codec() {
      return CODEC;
   }

   @Override
   public Set<ContextKey<?>> getReferencedContextParams() {
      return this.range.getReferencedContextParams();
   }

   public boolean test(LootContext context) {
      Integer count = context.getOptionalParameter(TreeChopLootContextParams.BLOCK_CHOP_COUNT);
      return count != null && this.range.test(context, count);
   }
}
