package ht.treechop.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import ht.treechop.TreeChopException;
import ht.treechop.common.chop.ChopUtil;
import ht.treechop.common.util.LevelUtil;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.arguments.coordinates.Coordinates;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;

public class ServerCommands {
   public static void register(CommandDispatcher<net.minecraft.commands.CommandSourceStack> dispatcher) {
      LiteralArgumentBuilder<net.minecraft.commands.CommandSourceStack> builder = (LiteralArgumentBuilder<net.minecraft.commands.CommandSourceStack>)Commands.literal(
            "treechop"
         )
         .requires(source -> source.permissions().hasPermission(net.minecraft.server.permissions.Permissions.COMMANDS_GAMEMASTER));
      builder.then(
         Commands.literal("chop")
            .then(
               Commands.argument("chopPos", BlockPosArgument.blockPos())
                  .then(Commands.argument("chopCount", IntegerArgumentType.integer(0)).executes(ServerCommands::chop))
            )
      );
      builder.then(Commands.literal("fell").then(Commands.argument("chopPos", BlockPosArgument.blockPos()).executes(ServerCommands::fell)));
      dispatcher.register(builder);
   }

   private static int chop(CommandContext<net.minecraft.commands.CommandSourceStack> context) {
      CommandSourceStack source = (CommandSourceStack)context.getSource();
      BlockPos pos = ((Coordinates)context.getArgument("chopPos", Coordinates.class)).getBlockPos(source);
      int numChops = (Integer)context.getArgument("chopCount", Integer.class);
      chop(context, source, pos, numChops);
      return 1;
   }

   private static void chop(
      CommandContext<net.minecraft.commands.CommandSourceStack> context,
      net.minecraft.commands.CommandSourceStack source,
      net.minecraft.core.BlockPos pos,
      int numChops
   ) {
      try {
         boolean felled = !ChopUtil.chop(
            source.getPlayer(), source.getLevel(), pos, source.getLevel().getBlockState(pos), ItemStack.EMPTY, context, numChops, false
         );
         if (felled) {
            LevelUtil.harvestBlock(source.getPlayer(), source.getLevel(), pos, ItemStack.EMPTY, true);
         }
      } catch (TreeChopException var5) {
      }
   }

   private static int fell(CommandContext<net.minecraft.commands.CommandSourceStack> context) {
      CommandSourceStack source = (CommandSourceStack)context.getSource();
      BlockPos pos = ((Coordinates)context.getArgument("chopPos", Coordinates.class)).getBlockPos(source);
      chop(context, source, pos, 10000);
      return 1;
   }
}
