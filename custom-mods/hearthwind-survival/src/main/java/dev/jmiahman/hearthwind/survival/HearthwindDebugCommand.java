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
                .requires(src -> true)
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
                        .then(Commands.literal("bowlfill")
                                .executes(ctx -> {
                                    ServerPlayer p = ctx.getSource().getPlayerOrException();
                                    // Find a bowl in inventory, consume one, give water_bowl
                                    var inv = p.getInventory();
                                    for (int i = 0; i < inv.getContainerSize(); i++) {
                                        var stack = inv.getItem(i);
                                        if (stack.is(net.minecraft.world.item.Items.BOWL)) {
                                            stack.shrink(1);
                                            var filled = new net.minecraft.world.item.ItemStack(DehydrationItems.WATER_BOWL);
                                            if (!p.getInventory().add(filled)) p.drop(filled, false);
                                            ctx.getSource().sendSuccess(() -> net.minecraft.network.chat.Component.literal(
                                                    "Bowl fill simulated: bowl -> water_bowl"), false);
                                            return 1;
                                        }
                                    }
                                    ctx.getSource().sendFailure(net.minecraft.network.chat.Component.literal("No bowl in inventory"));
                                    return 0;
                                }))
                        .then(Commands.literal("baresip")
                                .executes(ctx -> {
                                    ServerPlayer p = ctx.getSource().getPlayerOrException();
                                    double before = HearthwindSurvivalThirst.hydration(p);
                                    HearthwindSurvivalThirst.addHydration(p, 1.0);
                                    if (p.getRandom().nextFloat() < 0.60f) {
                                        p.addEffect(new net.minecraft.world.effect.MobEffectInstance(ThirstMobEffect.HOLDER, 400, 0));
                                    }
                                    p.getFoodData().addExhaustion(0.6f);
                                    double after = HearthwindSurvivalThirst.hydration(p);
                                    ctx.getSource().sendSuccess(() -> net.minecraft.network.chat.Component.literal(
                                            "Bare-hand sip: " + String.format("%.1f->%.1f", before, after) + " (90% thirst, tedious)"), false);
                                    return 1;
                                }))
                        .then(Commands.literal("drink")
                                .then(Commands.argument("type", com.mojang.brigadier.arguments.StringArgumentType.word())
                                        .suggests((c, b) -> {
                                            b.suggest("water");
                                            b.suggest("purified");
                                            b.suggest("hot");
                                            b.suggest("hot_purified");
                                            b.suggest("cold");
                                            b.suggest("cold_purified");
                                            return b.buildFuture();
                                        })
                                        .executes(ctx -> {
                                            ServerPlayer p = ctx.getSource().getPlayerOrException();
                                            String type = com.mojang.brigadier.arguments.StringArgumentType.getString(ctx, "type");
                                            double before = HearthwindSurvivalThirst.hydration(p);
                                            var inv = p.getInventory();
                                            net.minecraft.world.item.Item target = null;
                                            boolean isHot = false;
                                            boolean isCold = false;
                                            boolean isPurified = false;
                                            if (type.equals("water")) { target = DehydrationItems.WATER_BOWL; isHot=false; isCold=false; isPurified=false; }
                                            else if (type.equals("purified")) { target = DehydrationItems.PURIFIED_WATER_BOWL; isHot=false; isCold=false; isPurified=true; }
                                            else if (type.equals("hot")) { target = DehydrationItems.HOT_WATER_BOWL; isHot=true; isCold=false; isPurified=false; }
                                            else if (type.equals("hot_purified")) { target = DehydrationItems.HOT_PURIFIED_WATER_BOWL; isHot=true; isCold=false; isPurified=true; }
                                            else if (type.equals("cold")) { target = DehydrationItems.COLD_WATER_BOWL; isHot=false; isCold=true; isPurified=false; }
                                            else if (type.equals("cold_purified")) { target = DehydrationItems.COLD_PURIFIED_WATER_BOWL; isHot=false; isCold=true; isPurified=true; }
                                            else {
                                                ctx.getSource().sendFailure(net.minecraft.network.chat.Component.literal("Unknown type: use water/purified/hot/hot_purified"));
                                                return 0;
                                            }
                                            for (int i = 0; i < inv.getContainerSize(); i++) {
                                                var stack = inv.getItem(i);
                                                if (stack.is(target)) {
                                                    // Directly simulate drink without needing hand
                                                    if (isHot) {
                                                        // Hot always scalds in test (force hot) - direct health
                                                        p.setHealth(Math.max(0.1f, p.getHealth() - 2.0f));
                                                        p.setRemainingFireTicks(20);
                                                        p.addEffect(new net.minecraft.world.effect.MobEffectInstance(ThirstMobEffect.HOLDER, 400, 0));
                                                        HearthwindSurvivalThirst.addHydration(p, 3.0);
                                                        p.sendOverlayMessage(net.minecraft.network.chat.Component.literal("Test: drank hot! 2 dmg + thirst").withStyle(net.minecraft.ChatFormatting.RED));
                                                    } else if (isCold) {
                                                        HearthwindSurvivalThirst.addHydration(p, 6.0);
                                                        if (!isPurified && p.getRandom().nextDouble() < 0.30) {
                                                            p.addEffect(new net.minecraft.world.effect.MobEffectInstance(ThirstMobEffect.HOLDER, 300, 0));
                                                        }
                                                        HearthwindSurvivalTemperature.shift(p, -1.5);
                                                        HearthwindSurvivalTemperature.applyColdCooldown(p, 1200);
                                                        p.sendOverlayMessage(net.minecraft.network.chat.Component.literal("Test: drank cold! -1.5 temp, 60s cooling").withStyle(net.minecraft.ChatFormatting.AQUA));
                                                    } else {
                                                        HearthwindSurvivalThirst.addHydration(p, 6.0);
                                                        if (!isPurified && p.getRandom().nextDouble() < 0.30) {
                                                            p.addEffect(new net.minecraft.world.effect.MobEffectInstance(ThirstMobEffect.HOLDER, 300, 0));
                                                        }
                                                        HearthwindSurvivalTemperature.shift(p, -0.7);
                                                        HearthwindSurvivalTemperature.applyColdCooldown(p, 600);
                                                    }
                                                    p.getFoodData().addExhaustion(0.0f);
                                                    // Consume and return bowl
                                                    stack.shrink(1);
                                                    var bowl = new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.BOWL);
                                                    if (!p.getInventory().add(bowl)) p.drop(bowl, false);
                                                    double after = HearthwindSurvivalThirst.hydration(p);
                                                    ctx.getSource().sendSuccess(() -> net.minecraft.network.chat.Component.literal(
                                                            "Drank " + type + ": " + String.format("%.1f->%.1f", before, after)), false);
                                                    return 1;
                                                }
                                            }
                                            ctx.getSource().sendFailure(net.minecraft.network.chat.Component.literal("No " + type + " bowl in inventory"));
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
