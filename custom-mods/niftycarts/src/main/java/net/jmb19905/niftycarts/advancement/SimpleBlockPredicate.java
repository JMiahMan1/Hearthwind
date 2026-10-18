package net.jmb19905.niftycarts.advancement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public record SimpleBlockPredicate(
   Optional<net.minecraft.tags.TagKey<net.minecraft.world.level.block.Block>> blockTag,
   Optional<net.minecraft.core.HolderSet<net.minecraft.world.level.block.Block>> blocks
) implements Predicate<net.minecraft.world.level.block.state.BlockState> {
   public static final Codec<SimpleBlockPredicate> CODEC = RecordCodecBuilder.create(
      instance -> instance.group(
            TagKey.codec(BuiltInRegistries.BLOCK.key()).optionalFieldOf("tag").forGetter(SimpleBlockPredicate::blockTag),
            RegistryCodecs.homogeneousList(BuiltInRegistries.BLOCK.key()).optionalFieldOf("blocks").forGetter(SimpleBlockPredicate::blocks)
         )
         .apply(instance, SimpleBlockPredicate::new)
   );

   public boolean test(BlockState block) {
      return (this.blockTag.isEmpty() || block.is((TagKey)this.blockTag.get()))
         && (this.blocks.isEmpty() || block.is((HolderSet)this.blocks.get()));
   }

   public static class Builder {
      @Nullable
      private net.minecraft.core.HolderSet<net.minecraft.world.level.block.Block> blocks;
      @Nullable
      private net.minecraft.tags.TagKey<net.minecraft.world.level.block.Block> tag;

      private Builder() {
      }

      public static SimpleBlockPredicate.Builder block() {
         return new SimpleBlockPredicate.Builder();
      }

      public SimpleBlockPredicate.Builder of(Block... blocks) {
         this.blocks = HolderSet.direct(Arrays.stream(blocks).map(Block::builtInRegistryHolder).toList());
         return this;
      }

      public SimpleBlockPredicate.Builder of(net.minecraft.tags.TagKey<net.minecraft.world.level.block.Block> tag) {
         this.tag = tag;
         return this;
      }

      public SimpleBlockPredicate build() {
         return new SimpleBlockPredicate(Optional.ofNullable(this.tag), Optional.ofNullable(this.blocks));
      }
   }
}
