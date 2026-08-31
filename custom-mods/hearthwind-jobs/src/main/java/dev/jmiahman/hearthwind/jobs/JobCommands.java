package dev.jmiahman.hearthwind.jobs;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public final class JobCommands {
    private JobCommands() {}

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, access, env) -> {
            SuggestionProvider<CommandSourceStack> jobSuggestions = (ctx, builder) -> {
                for (String id : JobDefs.all().keySet()) builder.suggest(id);
                return builder.buildFuture();
            };

            dispatcher.register(Commands.literal("job")
                .then(Commands.literal("join")
                    .then(Commands.argument("job", StringArgumentType.word())
                        .suggests(jobSuggestions)
                        .executes(ctx -> {
                            ServerPlayer p = ctx.getSource().getPlayerOrException();
                            String id = StringArgumentType.getString(ctx, "job");
                            if (JobDefs.byId(id) == null) {
                                ctx.getSource().sendFailure(Component.literal("Unknown job: " + id
                                    + ". Available: " + String.join(", ", JobDefs.all().keySet())));
                                return 0;
                            }
                            boolean ok = JobState.join(p, id);
                            return ok ? 1 : 0;
                        })))
                .then(Commands.literal("leave")
                    .executes(ctx -> {
                        ServerPlayer p = ctx.getSource().getPlayerOrException();
                        if (JobState.jobId(p).isEmpty()) {
                            ctx.getSource().sendFailure(Component.literal("You have no job to leave."));
                            return 0;
                        }
                        JobState.leave(p);
                        return 1;
                    }))
                .then(Commands.literal("info")
                    .executes(ctx -> {
                        ServerPlayer p = ctx.getSource().getPlayerOrException();
                        String jid = JobState.jobId(p);
                        if (jid.isEmpty()) {
                            ctx.getSource().sendSuccess(() -> Component.literal("Unemployed. XP 0"), false);
                        } else {
                            int lvl = JobState.level(p);
                            double xp = JobState.xp(p);
                            ctx.getSource().sendSuccess(() -> Component.literal(
                                "Job: " + jid + " Lv." + lvl + " (" + (int) xp + " xp)"), false);
                        }
                        return 1;
                    }))
                .then(Commands.literal("age")
                    .requires(source -> Commands.LEVEL_MODERATORS.check(source.permissions()))
                    .then(Commands.argument("age", com.mojang.brigadier.arguments.IntegerArgumentType.integer(0, 5))
                        .executes(ctx -> {
                            ServerPlayer p = ctx.getSource().getPlayerOrException();
                            int age = com.mojang.brigadier.arguments.IntegerArgumentType.getInteger(ctx, "age");
                            AgeState.set(p, age);
                            ctx.getSource().sendSuccess(() -> Component.literal(
                                "Age set to " + age + "."), false);
                            return 1;
                        }))));
        });
    }
}
