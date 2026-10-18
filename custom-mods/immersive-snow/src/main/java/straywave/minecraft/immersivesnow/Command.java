package straywave.minecraft.immersivesnow;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap.Types;

public class Command {
   private static final Types[] HEIGHTMAPS = new Types[]{Types.WORLD_SURFACE, Types.OCEAN_FLOOR, Types.MOTION_BLOCKING, Types.MOTION_BLOCKING_NO_LEAVES};

   private static void sendResponse(CommandContext<net.minecraft.commands.CommandSourceStack> context, String message) {
      ((CommandSourceStack)context.getSource()).sendSystemMessage(Component.literal(message));
   }

   public static void register(CommandDispatcher<net.minecraft.commands.CommandSourceStack> dispatcher) {
      LiteralArgumentBuilder<net.minecraft.commands.CommandSourceStack> command = LiteralArgumentBuilder.literal("snow");
      LiteralArgumentBuilder<net.minecraft.commands.CommandSourceStack> recalculate = LiteralArgumentBuilder.literal("recalculate");
      recalculate.executes(Command::recalculate);
      command.then(recalculate);
      LiteralArgumentBuilder<net.minecraft.commands.CommandSourceStack> forget = LiteralArgumentBuilder.literal("forget");
      forget.executes(Command::forget);
      command.then(forget);
      LiteralArgumentBuilder<net.minecraft.commands.CommandSourceStack> heightmaps = LiteralArgumentBuilder.literal("heightmaps");
      heightmaps.executes(Command::heightmaps);
      command.then(heightmaps);
      dispatcher.register(command);
   }

   private static int recalculate(CommandContext<net.minecraft.commands.CommandSourceStack> context) {
      BlockPos blockPos = ((CommandSourceStack)context.getSource()).getPlayer().blockPosition();
      ChunkPos pos = ChunkPos.containing(blockPos);
      Queue.add(new Queue.Entry(pos, 0), true);
      sendResponse(context, String.format("Added chunk at %s %s to queue", pos.x(), pos.z()));
      return 1;
   }

   private static int forget(CommandContext<net.minecraft.commands.CommandSourceStack> context) {
      Queue.clear();
      Memory.erase();
      sendResponse(context, "Cleared queue and forgot all chunks");
      return 1;
   }

   private static int heightmaps(CommandContext<net.minecraft.commands.CommandSourceStack> context) {
      BlockPos playerPos = ((CommandSourceStack)context.getSource()).getPlayer().blockPosition();
      sendResponse(context, String.format("Block %s %s:", playerPos.getX(), playerPos.getZ()));

      for (Types heightmap : HEIGHTMAPS) {
         BlockPos blockPos = ((CommandSourceStack)context.getSource()).getLevel().getHeightmapPos(heightmap, playerPos);
         BlockState blockState = ((CommandSourceStack)context.getSource()).getLevel().getBlockState(blockPos);
         Block block = blockState.getBlock();
         BlockPos belowPos = blockPos.below();
         BlockState belowState = ((CommandSourceStack)context.getSource()).getLevel().getBlockState(belowPos);
         Block below = belowState.getBlock();
         String message = String.format("- %s: %s (%s); %s (%s)", heightmap.getSerializedName(), blockPos.toShortString(), block, belowPos.toShortString(), below);
         sendResponse(context, message);
      }

      return 1;
   }
}
