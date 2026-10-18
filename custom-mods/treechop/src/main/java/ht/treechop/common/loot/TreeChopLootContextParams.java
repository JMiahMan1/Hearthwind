package ht.treechop.common.loot;

import ht.treechop.TreeChop;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.util.context.ContextKey;
import net.minecraft.util.context.ContextKeySet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

public class TreeChopLootContextParams {
   public static final ContextKey<Integer> BLOCK_CHOP_COUNT = new ContextKey<>(TreeChop.resource("count_block_chops"));
   public static final ContextKey<Boolean> DESTROY_BLOCK = new ContextKey<>(TreeChop.resource("tree_felled"));
   public static final ContextKeySet SET = buildContextKeySet();

   private static ContextKeySet buildContextKeySet() {
      ContextKeySet.Builder builder = new ContextKeySet.Builder();
      Set<ContextKey<?>> required = LootContextParamSets.BLOCK.required();
      Set<ContextKey<?>> optional = LootContextParamSets.BLOCK.allowed().stream().filter(p -> !required.contains(p)).collect(Collectors.toSet());
      required.forEach(builder::required);
      optional.forEach(builder::optional);
      builder.required(BLOCK_CHOP_COUNT).required(DESTROY_BLOCK);
      return builder.build();
   }

   public static void init() {
   }
}
