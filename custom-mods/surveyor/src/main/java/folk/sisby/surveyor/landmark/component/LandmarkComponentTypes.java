package folk.sisby.surveyor.landmark.component;

import com.mojang.serialization.Codec;
import folk.sisby.surveyor.Surveyor;
import folk.sisby.surveyor.mixin.AccessAbstractBlock;
import folk.sisby.surveyor.util.RegionPos;
import java.util.BitSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.HoverEvent.ShowText;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.ProblemReporter.ScopedCollector;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.material.MapColor.Brightness;
import net.minecraft.world.level.storage.TagValueOutput;

public class LandmarkComponentTypes {
   public static final LandmarkComponentType<BlockPos> POS = register(
      "pos",
      BlockPos.CODEC,
      p -> Component.literal("[").append(Component.literal(p.toShortString()).withStyle(ChatFormatting.GOLD)).append(Component.literal("]"))
   );
   public static final LandmarkComponentType<Component> NAME = register(
      "name", ComponentSerialization.CODEC, t -> Component.literal("\"").append(t.copy().withStyle(ChatFormatting.GREEN)).append(Component.literal("\""))
   );
   public static final LandmarkComponentType<List<Component>> LORE = register(
      "lore",
      Codec.list(ComponentSerialization.CODEC),
      l -> ComponentUtils.formatList(l, Component.literal("\" | \""), t -> t.copy().withStyle(ChatFormatting.GREEN))
   );
   public static final LandmarkComponentType<Integer> COLOR = register(
      "color",
      Codec.INT,
      i -> Component.literal("#")
         .setStyle(Style.EMPTY.withColor(16777215 & i))
         .append(Component.literal(Integer.toHexString(16777215 & i).toUpperCase()).withStyle(ChatFormatting.GOLD))
   );
   public static final LandmarkComponentType<Long> TIME = register(
      "time",
      Codec.LONG,
      tick -> Component.nullToEmpty("Day %d, %d:%02d".formatted(1L + tick / 24000L, (6000L + tick) % 24000L / 1000L, tick % 1000L > 500L ? 30 : 0))
   );
   public static final LandmarkComponentType<Integer> SEED = register(
      "seed", Codec.INT, i -> Component.literal(String.valueOf(i)).withStyle(ChatFormatting.GOLD)
   );
   public static final LandmarkComponentType<BoundingBox> BOX = register(
      "box",
      BoundingBox.CODEC,
      b -> Component.literal("[")
         .append(Component.literal(new BlockPos(b.minX(), b.minY(), b.minZ()).toShortString()).withStyle(ChatFormatting.GOLD))
         .append(Component.literal("]->["))
         .append(Component.literal(new BlockPos(b.maxX(), b.maxY(), b.maxZ()).toShortString()).withStyle(ChatFormatting.GOLD))
         .append(Component.literal("]"))
   );
   public static final LandmarkComponentType<ItemStack> STACK = register(
      "stack",
      ItemStack.CODEC,
      s -> Component.literal("")
         .append(Component.literal("["))
         .append(s.getItem().getName(s).copy().withStyle(s.getRarity().color()))
         .append(Component.literal("]"))
         .append(
            s.has(DataComponents.CUSTOM_NAME)
               ? Component.literal(" - \"").append(s.getHoverName().copy().withStyle(ChatFormatting.GREEN)).append(Component.literal("\""))
               : Component.literal("")
         )
   );
   public static final LandmarkComponentType<Map<RegionPos, BitSet>> CHUNKS = register(
      "chunks",
      Codec.unboundedMap(RegionPos.CODEC, ExtraCodecs.BIT_SET),
      m -> Component.literal("%d chunks".formatted(m.values().stream().mapToInt(BitSet::cardinality).sum()))
         .withStyle(
            s -> s.withHoverEvent(
               new ShowText(Component.literal(RegionPos.regionsToChunks(m).stream().<CharSequence>map(ChunkPos::toString).collect(Collectors.joining(", "))))
            )
         )
   );

   public static LandmarkComponentMap.Builder forBlock(LandmarkComponentMap.Builder builder, LevelAccessor world, BlockPos pos) {
      BlockState state = world.getBlockState(pos);
      ItemStack stack = ((AccessAbstractBlock)state.getBlock()).invokeGetPickStack(world, pos, world.getBlockState(pos), true);
      BlockEntity entity = world.getBlockEntity(pos);
      if (entity != null
         && BuiltInRegistries.BLOCK_ENTITY_TYPE
            .getResourceKey(entity.getType())
            .map(t -> Surveyor.CONFIG.builtins.allowedBlockEntities.contains(t.toString()))
            .orElse(false)) {
         ScopedCollector logging = new ScopedCollector(entity.problemPath(), Surveyor.LOGGER);

         try {
            TagValueOutput nbtWriteView = TagValueOutput.createWithContext(logging, world.registryAccess());
            entity.saveWithoutMetadata(nbtWriteView);
            BlockItem.setBlockEntityData(stack, entity.getType(), nbtWriteView);
         } catch (Throwable var10) {
            try {
               logging.close();
            } catch (Throwable var9) {
               var10.addSuppressed(var9);
            }

            throw var10;
         }

         logging.close();
      }

      builder.add(NAME, (Component)(!stack.isEmpty() ? stack.getHoverName() : state.getBlock().getName()));
      if (!stack.isEmpty()) {
         builder.add(STACK, stack);
      }

      int color = state.getMapColor(world, pos).calculateARGBColor(Brightness.HIGH);
      if (color != 0) {
         builder.add(COLOR, color);
      }

      builder.add(POS, pos);
      return builder;
   }

   private static <T> LandmarkComponentType<T> register(String path, Codec<T> codec, Function<T, Component> viewer) {
      return register(Surveyor.id(path), codec, viewer);
   }

   public static <T> LandmarkComponentType<T> register(Identifier id, Codec<T> codec, Function<T, Component> viewer) {
      LandmarkComponentType<T> type = new LandmarkComponentType<>(id, codec, viewer);
      LandmarkComponentType.register(type);
      return type;
   }

   public static void touch() {
   }
}
