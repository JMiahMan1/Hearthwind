package dev.jmiahman.hearthwind.survival;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

/**
 * Debug commands for live testing: /hearthwind hydration set <value>, temp set, etc.
 * OP only, for dev-server and singleplayer.
 */
public final class HearthwindDebugCommand {
    private HearthwindDebugCommand() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("hearthwind")
                .requires(src -> net.minecraft.commands.Commands.LEVEL_MODERATORS.check(src.permissions()))
                .then(Commands.literal("hydration")
                        .then(Commands.literal("set")
                                .then(Commands.argument("value", DoubleArgumentType.doubleArg(0, 20))
                                        .executes(ctx -> {
                                            ServerPlayer p = ctx.getSource().getPlayerOrException();
                                            double v = DoubleArgumentType.getDouble(ctx, "value");
                                            p.setAttached(HearthwindSurvivalThirst.HYDRATION, v);
                                            ctx.getSource().sendSuccess(() -> net.minecraft.network.chat.Component.literal(
                                                    "Set hydration to " + v), false);
                                            return 1;
                                        })))
                        .then(Commands.literal("get")
                                .executes(ctx -> {
                                    ServerPlayer p = ctx.getSource().getPlayerOrException();
                                    double v = HearthwindSurvivalThirst.hydration(p);
                                    ctx.getSource().sendSuccess(() -> net.minecraft.network.chat.Component.literal(
                                            "Hydration: " + v), false);
                                    return 1;
                                })))
                .then(Commands.literal("temperature")
                        .then(Commands.literal("set")
                                .then(Commands.argument("value", DoubleArgumentType.doubleArg(-10, 10))
                                        .executes(ctx -> {
                                            ServerPlayer p = ctx.getSource().getPlayerOrException();
                                            double v = DoubleArgumentType.getDouble(ctx, "value");
                                            p.setAttached(HearthwindSurvivalTemperature.TEMPERATURE, v);
                                            ctx.getSource().sendSuccess(() -> net.minecraft.network.chat.Component.literal(
                                                    "Set temperature to " + v), false);
                                            return 1;
                                        })))
                        .then(Commands.literal("get")
                                .executes(ctx -> {
                                    ServerPlayer p = ctx.getSource().getPlayerOrException();
                                    double v = HearthwindSurvivalTemperature.get(p);
                                    double target = HearthwindSurvivalTemperature.targetFor(p);
                                    ctx.getSource().sendSuccess(() -> net.minecraft.network.chat.Component.literal(
                                            "Temp: " + String.format("%.1f", v) + " target " + String.format("%.1f", target)), false);
                                    return 1;
                                })))
                .then(Commands.literal("test")
                        .then(Commands.literal("flaskfill")
                                .executes(ctx -> {
                                    ServerPlayer p = ctx.getSource().getPlayerOrException();
                                    var filled = new net.minecraft.world.item.ItemStack(FlaskItems.LEATHER_FLASK);
                                    FlaskItems.setFill(filled, 2, FlaskData.IMPURIFIED);
                                    if (!p.getInventory().add(filled)) p.drop(filled, false);
                                    ctx.getSource().sendSuccess(() -> net.minecraft.network.chat.Component.literal(
                                            "Gave a filled leather flask"), false);
                                    return 1;
                                }))
                        .then(Commands.literal("baresip")
                                .executes(ctx -> {
                                    ServerPlayer p = ctx.getSource().getPlayerOrException();
                                    double before = HearthwindSurvivalThirst.hydration(p);
                                    HearthwindSurvivalThirst.addHydration(p, HearthwindSurvivalConfig.get().bareHand.sipQuench);
                                    if (p.getRandom().nextFloat() < HearthwindSurvivalConfig.get().bareHand.sipThirstChance) {
                                        p.addEffect(new net.minecraft.world.effect.MobEffectInstance(ThirstMobEffect.HOLDER,
                                                HearthwindSurvivalConfig.get().bareHand.sipThirstDuration, 1));
                                    }
                                    double after = HearthwindSurvivalThirst.hydration(p);
                                    ctx.getSource().sendSuccess(() -> net.minecraft.network.chat.Component.literal(
                                            "Bare-hand sip: " + String.format("%.1f->%.1f", before, after)), false);
                                    return 1;
                                }))
                        .then(Commands.literal("drink")
                                .then(Commands.argument("tier", com.mojang.brigadier.arguments.StringArgumentType.word())
                                        .suggests((c, b) -> {
                                            b.suggest("leather");
                                            b.suggest("iron");
                                            b.suggest("golden");
                                            b.suggest("diamond");
                                            b.suggest("netherite");
                                            return b.buildFuture();
                                        })
                                        .executes(ctx -> {
                                            ServerPlayer p = ctx.getSource().getPlayerOrException();
                                            String tier = com.mojang.brigadier.arguments.StringArgumentType.getString(ctx, "tier");
                                            dev.jmiahman.hearthwind.survival.LeatherFlaskItem item = switch (tier) {
                                                case "leather" -> FlaskItems.LEATHER_FLASK;
                                                case "iron" -> FlaskItems.IRON_LEATHER_FLASK;
                                                case "golden" -> FlaskItems.GOLDEN_LEATHER_FLASK;
                                                case "diamond" -> FlaskItems.DIAMOND_LEATHER_FLASK;
                                                case "netherite" -> FlaskItems.NETHERITE_LEATHER_FLASK;
                                                default -> null;
                                            };
                                            if (item == null) {
                                                ctx.getSource().sendFailure(net.minecraft.network.chat.Component.literal(
                                                        "Unknown tier: leather/iron/golden/diamond/netherite"));
                                                return 0;
                                            }
                                            var inv = p.getInventory();
                                            for (int i = 0; i < inv.getContainerSize(); i++) {
                                                var stack = inv.getItem(i);
                                                if (stack.is(item)) {
                                                    double before = HearthwindSurvivalThirst.hydration(p);
                                                    FlaskItems.onFlaskConsumed(p, stack);
                                                    double after = HearthwindSurvivalThirst.hydration(p);
                                                    ctx.getSource().sendSuccess(() -> net.minecraft.network.chat.Component.literal(
                                                            "Drank from " + tier + " flask: " + String.format("%.1f->%.1f", before, after)), false);
                                                    return 1;
                                                }
                                            }
                                            ctx.getSource().sendFailure(net.minecraft.network.chat.Component.literal("No " + tier + " flask in inventory"));
                                            return 0;
                                        })))
                        .then(Commands.literal("move")
                                .then(Commands.argument("x", com.mojang.brigadier.arguments.DoubleArgumentType.doubleArg())
                                .then(Commands.argument("y", com.mojang.brigadier.arguments.DoubleArgumentType.doubleArg())
                                .then(Commands.argument("z", com.mojang.brigadier.arguments.DoubleArgumentType.doubleArg())
                                        .executes(ctx -> {
                                            ServerPlayer p = ctx.getSource().getPlayerOrException();
                                            double x = com.mojang.brigadier.arguments.DoubleArgumentType.getDouble(ctx, "x");
                                            double y = com.mojang.brigadier.arguments.DoubleArgumentType.getDouble(ctx, "y");
                                            double z = com.mojang.brigadier.arguments.DoubleArgumentType.getDouble(ctx, "z");
                                            p.teleportTo(x, y, z);
                                            ctx.getSource().sendSuccess(() -> net.minecraft.network.chat.Component.literal(
                                                    "Moved to " + String.format("%.1f,%.1f,%.1f", x, y, z)), false);
                                            return 1;
                                        }))))))
        );
    }
}
