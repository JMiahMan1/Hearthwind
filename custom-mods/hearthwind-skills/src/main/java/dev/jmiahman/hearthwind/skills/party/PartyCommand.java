package dev.jmiahman.hearthwind.skills.party;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public final class PartyCommand {
    private PartyCommand() {}

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            registerCommands(dispatcher);
        });
    }

    public static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("party")
                .then(Commands.literal("create")
                        .executes(ctx -> {
                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                            PartyManager.createParty(player, null);
                            return 1;
                        })
                        .then(Commands.argument("name", StringArgumentType.greedyString())
                                .executes(ctx -> {
                                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                                    String name = StringArgumentType.getString(ctx, "name");
                                    PartyManager.createParty(player, name);
                                    return 1;
                                })))
                .then(Commands.literal("invite")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(ctx -> {
                                    ServerPlayer source = ctx.getSource().getPlayerOrException();
                                    ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
                                    PartyManager.invitePlayer(source, target);
                                    return 1;
                                })))
                .then(Commands.literal("accept")
                        .executes(ctx -> {
                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                            PartyManager.acceptInvite(player);
                            return 1;
                        }))
                .then(Commands.literal("leave")
                        .executes(ctx -> {
                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                            PartyManager.leaveParty(player);
                            return 1;
                        }))
                .then(Commands.literal("kick")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(ctx -> {
                                    ServerPlayer source = ctx.getSource().getPlayerOrException();
                                    ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
                                    PartyManager.kickPlayer(source, target);
                                    return 1;
                                })))
                .then(Commands.literal("disband")
                        .executes(ctx -> {
                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                            PartyManager.disbandParty(player);
                            return 1;
                        }))
                .then(Commands.literal("pvp")
                        .then(Commands.argument("enabled", BoolArgumentType.bool())
                                .executes(ctx -> {
                                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                                    boolean enable = BoolArgumentType.getBool(ctx, "enabled");
                                    PartyManager.togglePvp(player, enable);
                                    return 1;
                                })))
                .then(Commands.literal("info")
                        .executes(ctx -> {
                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                            Party party = PartyManager.getPartyByPlayer(player.getUUID());
                            if (party == null) {
                                player.sendSystemMessage(Component.literal("You are not currently in a party. Create one with /party create").withStyle(ChatFormatting.YELLOW));
                                return 0;
                            }
                            player.sendSystemMessage(Component.literal("=== " + party.getName() + " ===").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
                            player.sendSystemMessage(Component.literal("PvP Friendly Fire: " + (party.isPvpEnabled() ? "ON" : "OFF")).withStyle(party.isPvpEnabled() ? ChatFormatting.RED : ChatFormatting.GREEN));
                            player.sendSystemMessage(Component.literal("Members (" + party.getMembers().size() + "):").withStyle(ChatFormatting.AQUA));
                            MinecraftServer server = player.level().getServer();
                            for (var memberId : party.getMembers()) {
                                ServerPlayer member = server != null ? server.getPlayerList().getPlayer(memberId) : null;
                                String name = member == null ? "Offline Member" : member.getName().getString();
                                boolean isLeader = party.isLeader(memberId);
                                player.sendSystemMessage(Component.literal(" - " + name + (isLeader ? " (Leader)" : ""))
                                        .withStyle(isLeader ? ChatFormatting.GOLD : ChatFormatting.WHITE));
                            }
                            return 1;
                        }))
        );
    }
}
