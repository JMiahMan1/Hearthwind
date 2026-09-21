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
				.then(Commands.literal("nutrition")
						.then(Commands.literal("get")
								.executes(ctx -> {
									HearthwindSurvivalDiet.debugStatus(ctx.getSource().getPlayerOrException());
									return 1;
								}))
						.then(Commands.literal("set")
								.then(Commands.argument("nutrient", com.mojang.brigadier.arguments.StringArgumentType.word())
										.suggests((c, b) -> {
											for (String name : HearthwindSurvivalDiet.NUTRIENT_NAMES) {
												b.suggest(name);
											}
											return b.buildFuture();
										})
										.then(Commands.argument("value", com.mojang.brigadier.arguments.IntegerArgumentType.integer(0, 3000))
												.executes(ctx -> {
													ServerPlayer p = ctx.getSource().getPlayerOrException();
													String nutrient = com.mojang.brigadier.arguments.StringArgumentType.getString(ctx, "nutrient");
													int index = -1;
													for (int i = 0; i < HearthwindSurvivalDiet.NUTRIENT_NAMES.length; i++) {
														if (HearthwindSurvivalDiet.NUTRIENT_NAMES[i].equalsIgnoreCase(nutrient)) {
															index = i;
														}
													}
													if (index < 0) {
														ctx.getSource().sendFailure(net.minecraft.network.chat.Component.literal(
																"Unknown nutrient: " + String.join("/", HearthwindSurvivalDiet.NUTRIENT_NAMES)));
														return 0;
													}
													int value = com.mojang.brigadier.arguments.IntegerArgumentType.getInteger(ctx, "value");
													HearthwindSurvivalDiet.setLevel(p, index, value);
													HearthwindSurvivalDiet.debugStatus(p);
													return 1;
												})))))
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
                                .then(Commands.argument("value", com.mojang.brigadier.arguments.IntegerArgumentType.integer(-2400, 2400))
                                        .executes(ctx -> {
                                            ServerPlayer p = ctx.getSource().getPlayerOrException();
                                            int v = com.mojang.brigadier.arguments.IntegerArgumentType.getInteger(ctx, "value");
                                            HearthwindSurvivalTemperature.setBody(p, v);
                                            ctx.getSource().sendSuccess(() -> net.minecraft.network.chat.Component.literal(
                                                    "Set body temperature to " + v), false);
                                            return 1;
                                        })))
                        .then(Commands.literal("wetness")
                                .then(Commands.argument("value", com.mojang.brigadier.arguments.IntegerArgumentType.integer(0, 200))
                                        .executes(ctx -> {
                                            ServerPlayer p = ctx.getSource().getPlayerOrException();
                                            int v = com.mojang.brigadier.arguments.IntegerArgumentType.getInteger(ctx, "value");
                                            HearthwindSurvivalTemperature.setWetness(p, v);
                                            ctx.getSource().sendSuccess(() -> net.minecraft.network.chat.Component.literal(
                                                    "Set wetness to " + v), false);
                                            return 1;
                                        })))
                        .then(Commands.literal("affection")
                                .then(Commands.argument("targets", com.mojang.brigadier.arguments.StringArgumentType.word())
                                        .suggests((c, b) -> {
                                            b.suggest("hot");
                                            b.suggest("cold");
                                            return b.buildFuture();
                                        })
                                        .then(Commands.argument("value", com.mojang.brigadier.arguments.BoolArgumentType.bool())
                                                .executes(ctx -> {
                                                    ServerPlayer p = ctx.getSource().getPlayerOrException();
                                                    String target = com.mojang.brigadier.arguments.StringArgumentType.getString(ctx, "targets");
                                                    boolean value = com.mojang.brigadier.arguments.BoolArgumentType.getBool(ctx, "value");
                                                    HearthwindSurvivalTemperature.State s = HearthwindSurvivalTemperature.getState(p);
                                                    boolean hot = target.equals("hot") ? value : s.hotAffected();
                                                    boolean cold = target.equals("cold") ? value : s.coldAffected();
                                                    HearthwindSurvivalTemperature.setEnvironmentAffection(p, hot, cold);
                                                    ctx.getSource().sendSuccess(() -> net.minecraft.network.chat.Component.literal(
                                                            "Set " + target + " affection to " + value), false);
                                                    return 1;
                                                }))))
                        .then(Commands.literal("get")
                                .executes(ctx -> {
                                    ServerPlayer p = ctx.getSource().getPlayerOrException();
                                    HearthwindSurvivalTemperature.State s = HearthwindSurvivalTemperature.getState(p);
                                    ctx.getSource().sendSuccess(() -> net.minecraft.network.chat.Component.literal(
                                            "Body: " + s.body() + " wetness: " + s.wetness()
                                                    + " thermometer: " + s.thermometer()
                                                    + " coldProt: " + s.coldProtection()
                                                    + " heatProt: " + s.heatProtection()), false);
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
