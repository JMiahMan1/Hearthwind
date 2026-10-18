package net.smitherz.init;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Collection;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.smitherz.item.Gem;
import net.smitherz.item.Upgradeable;
import net.smitherz.util.UpgradeHelper;

public class CommandInit {
   public static void init() {
      CommandRegistrationCallback.EVENT
         .register(
            (CommandRegistrationCallback)(dispatcher, dedicated, environment) -> dispatcher.register(
               (LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("smither")
                        .requires(serverCommandSource -> serverCommandSource.permissions().hasPermission(net.minecraft.server.permissions.Permissions.COMMANDS_GAMEMASTER)))
                     .then(
                        Commands.literal("slots")
                           .then(
                              ((RequiredArgumentBuilder)((RequiredArgumentBuilder)Commands.argument("targets", EntityArgument.players())
                                       .then(
                                          Commands.literal("add")
                                             .then(
                                                Commands.argument("count", IntegerArgumentType.integer())
                                                   .executes(
                                                      commandContext -> executeSlotCommand(
                                                         (CommandSourceStack)commandContext.getSource(),
                                                         EntityArgument.getPlayers(commandContext, "targets"),
                                                         IntegerArgumentType.getInteger(commandContext, "count"),
                                                         0
                                                      )
                                                   )
                                             )
                                       ))
                                    .then(
                                       Commands.literal("remove")
                                          .then(
                                             Commands.argument("count", IntegerArgumentType.integer())
                                                .executes(
                                                   commandContext -> executeSlotCommand(
                                                      (CommandSourceStack)commandContext.getSource(),
                                                      EntityArgument.getPlayers(commandContext, "targets"),
                                                      IntegerArgumentType.getInteger(commandContext, "count"),
                                                      1
                                                   )
                                                )
                                          )
                                    ))
                                 .then(
                                    Commands.literal("set")
                                       .then(
                                          Commands.argument("count", IntegerArgumentType.integer())
                                             .executes(
                                                commandContext -> executeSlotCommand(
                                                   (CommandSourceStack)commandContext.getSource(),
                                                   EntityArgument.getPlayers(commandContext, "targets"),
                                                   IntegerArgumentType.getInteger(commandContext, "count"),
                                                   2
                                                )
                                             )
                                       )
                                 )
                           )
                     ))
                  .then(
                     Commands.literal("gems")
                        .then(
                           ((RequiredArgumentBuilder)Commands.argument("targets", EntityArgument.players())
                                 .then(
                                    Commands.literal("add")
                                       .then(
                                          Commands.argument("item", ItemArgument.item(dedicated))
                                             .executes(
                                                commandContext -> executeGemCommand(
                                                   (CommandSourceStack)commandContext.getSource(),
                                                   EntityArgument.getPlayers(commandContext, "targets"),
                                                   ItemArgument.getItem(commandContext, "item"),
                                                   0
                                                )
                                             )
                                       )
                                 ))
                              .then(
                                 Commands.literal("remove")
                                    .then(
                                       Commands.argument("item", ItemArgument.item(dedicated))
                                          .executes(
                                             commandContext -> executeGemCommand(
                                                (CommandSourceStack)commandContext.getSource(),
                                                EntityArgument.getPlayers(commandContext, "targets"),
                                                ItemArgument.getItem(commandContext, "item"),
                                                1
                                             )
                                          )
                                    )
                              )
                        )
                  )
            )
         );
   }

   private static int executeGemCommand(
      net.minecraft.commands.CommandSourceStack source,
      Collection<net.minecraft.server.level.ServerPlayer> targets,
      net.minecraft.commands.arguments.item.ItemInput item,
      int code
   ) {
      ItemStack gemStack = null;

      try {
         gemStack = item.createItemStack(1);
      } catch (CommandSyntaxException var9) {
      }

      for (ServerPlayer serverPlayerEntity : targets) {
         Component text;
         if (!serverPlayerEntity.getMainHandItem().isEmpty()
            && net.smitherz.util.UpgradeHelper.isUpgradeable(serverPlayerEntity.getMainHandItem())
            && (item.item().value() instanceof Gem || gemStack.is(TagInit.GEMS))) {
            ItemStack stack = serverPlayerEntity.getMainHandItem();
            if (code == 0) {
               if (UpgradeHelper.getGemSlotSize(stack) >= UpgradeHelper.getGemStacks(stack).toList().size() + 1) {
                  UpgradeHelper.addStackToUpgradeable(stack, gemStack);
                  text = Component.translatable("commands.smitherz.gem_added", new Object[]{serverPlayerEntity.getDisplayName()});
               } else {
                  text = Component.translatable("commands.smitherz.gem_added_missing", new Object[]{serverPlayerEntity.getDisplayName()});
               }
            } else if (UpgradeHelper.removeStackFromUpgradeable(stack, gemStack)) {
               text = Component.translatable("commands.smitherz.gem_removed", new Object[]{serverPlayerEntity.getDisplayName()});
            } else {
               text = Component.translatable("commands.smitherz.gem_removed_missing", new Object[]{serverPlayerEntity.getDisplayName()});
            }
         } else {
            text = Component.translatable("commands.smitherz.gem_change_fail", new Object[]{serverPlayerEntity.getDisplayName()});
         }

         source.sendSuccess(() -> text, true);
      }

      return 1;
   }

   private static int executeSlotCommand(
      net.minecraft.commands.CommandSourceStack source, Collection<net.minecraft.server.level.ServerPlayer> targets, int count, int code
   ) {
      for (ServerPlayer serverPlayerEntity : targets) {
         if (!serverPlayerEntity.getMainHandItem().isEmpty() && net.smitherz.util.UpgradeHelper.isUpgradeable(serverPlayerEntity.getMainHandItem())) {
            if (code == 0) {
               if (UpgradeHelper.getGemSlotSize(serverPlayerEntity.getMainHandItem()) <= 0) {
                  UpgradeHelper.setGemSlots(serverPlayerEntity.getMainHandItem(), count);
               } else {
                  UpgradeHelper.addGemSlots(serverPlayerEntity.getMainHandItem(), count);
               }
            } else if (code == 1) {
               UpgradeHelper.removeGemSlots(serverPlayerEntity.getMainHandItem(), count);
            } else if (code == 2) {
               UpgradeHelper.setGemSlots(serverPlayerEntity.getMainHandItem(), count);
            }

            source.sendSuccess(() -> Component.translatable("commands.smitherz.slot_change", new Object[]{serverPlayerEntity.getDisplayName()}), true);
         } else {
            source.sendSuccess(() -> Component.translatable("commands.smitherz.slot_change_fail", new Object[]{serverPlayerEntity.getDisplayName()}), true);
         }
      }

      return 1;
   }
}
