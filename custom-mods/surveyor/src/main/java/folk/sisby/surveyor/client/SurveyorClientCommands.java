package folk.sisby.surveyor.client;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import folk.sisby.surveyor.Surveyor;
import folk.sisby.surveyor.SurveyorExploration;
import folk.sisby.surveyor.WorldSummary;
import folk.sisby.surveyor.config.SystemMode;
import folk.sisby.surveyor.landmark.Landmark;
import folk.sisby.surveyor.landmark.WorldLandmarks;
import folk.sisby.surveyor.landmark.component.LandmarkComponentTypes;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.server.permissions.PermissionSet;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.arguments.IdentifierArgument;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.Identifier;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import static folk.sisby.surveyor.SurveyorCommands.indent;
import static folk.sisby.surveyor.SurveyorCommands.prefix;

public class SurveyorClientCommands {

	private static int getLandmarks(WorldSummary summary, SurveyorExploration exploration, Consumer<Component> feedback, boolean global) {
		WorldLandmarks landmarks = summary.landmarks();
		if (landmarks == null) {
			feedback.accept(prefix().append(Component.literal("The landmark system is dynamically disabled!").withStyle(ChatFormatting.YELLOW)));
			return 0;
		}
		Map<Identifier, Landmark> landmarkMap = landmarks.asMap(global ? WorldLandmarks.GLOBAL : SurveyorClient.getClientUuid(), SurveyorClient.getExploration());
		if (landmarkMap.isEmpty()) {
			feedback.accept(prefix().append(Component.literal("There are no landmarks in this world!").withStyle(ChatFormatting.YELLOW)));
			return 0;
		}
		feedback.accept(prefix().append(Component.literal("Level %s:".formatted(global ? "Landmarks" : "Waypoints"))));
		for (Landmark landmark : landmarkMap.values()) {
			feedback.accept(
				indent()
					.append(Component.literal("%s:".formatted(landmark.id().getNamespace())).withStyle(ChatFormatting.GRAY))
					.append(Component.literal(landmark.id().getPath()))
					.append(!landmark.components().contains(LandmarkComponentTypes.NAME) ? Component.literal("") :
						Component.literal(": \"")
							.append(landmark.components().get(LandmarkComponentTypes.NAME).copy().styled(s -> s.withColor(landmark.components().contains(LandmarkComponentTypes.COLOR) ? 0xFFFFFF & landmark.components().get(LandmarkComponentTypes.COLOR) : ChatFormatting.GREEN.getColorValue())))
							.append(Component.literal("\""))
					)
			);
		}
		return landmarkMap.size();
	}


	private static int viewLandmark(WorldSummary summary, Consumer<Component> feedback, Identifier id, boolean global) {
		WorldLandmarks landmarks = summary.landmarks();
		if (landmarks == null) {
			feedback.accept(prefix().append(Component.literal("The landmark system is dynamically disabled!").withStyle(ChatFormatting.YELLOW)));
			return 0;
		}
		if (!landmarks.contains(global ? WorldLandmarks.GLOBAL : SurveyorClient.getClientUuid(), id)) {
			feedback.accept(prefix().append(Component.literal("No landmark exists of that id!").withStyle(ChatFormatting.YELLOW)));
			return 0;
		}
		Landmark landmark = landmarks.get(global ? WorldLandmarks.GLOBAL : SurveyorClient.getClientUuid(), id);
		feedback.accept(prefix().append(Component.literal(landmark.owner().equals(WorldLandmarks.GLOBAL) ? "Landmark " : "Waypoint ").withStyle(ChatFormatting.GRAY)).append(Component.literal(id.toString())).append(Component.literal(": ")));
		landmark.toText().forEach(t -> feedback.accept(indent().append(t)));
		return 1;
	}

	private static int rawLandmark(WorldSummary summary, Consumer<Component> feedback, Identifier id, boolean global) {
		WorldLandmarks landmarks = summary.landmarks();
		if (landmarks == null) {
			feedback.accept(prefix().append(Component.literal("The landmark system is dynamically disabled!").withStyle(ChatFormatting.YELLOW)));
			return 0;
		}
		if (!landmarks.contains(global ? WorldLandmarks.GLOBAL : SurveyorClient.getClientUuid(), id)) {
			feedback.accept(prefix().append(Component.literal("No landmark exists of that id!").withStyle(ChatFormatting.YELLOW)));
			return 0;
		}
		Landmark landmark = landmarks.get(global ? WorldLandmarks.GLOBAL : SurveyorClient.getClientUuid(), id);
		feedback.accept(prefix().append(Component.literal(landmark.owner().equals(WorldLandmarks.GLOBAL) ? "Landmark " : "Waypoint ").withStyle(ChatFormatting.GRAY)).append(Component.literal(id.toString())).append(Component.literal(": ")).append(Component.literal(landmark.toNbt().toString()).withStyle(ChatFormatting.AQUA)));
		return 1;
	}

	private static int removeLandmark(WorldSummary summary, Consumer<Component> feedback, Identifier id, boolean global) {
		WorldLandmarks landmarks = summary.landmarks();
		if (landmarks == null) {
			feedback.accept(prefix().append(Component.literal("The landmark system is dynamically disabled!").withStyle(ChatFormatting.YELLOW)));
			return 0;
		}
		if (!landmarks.contains(global ? WorldLandmarks.GLOBAL : SurveyorClient.getClientUuid(), id)) {
			feedback.accept(prefix().append(Component.literal("No landmark exists of that id!").withStyle(ChatFormatting.YELLOW)));
			return 0;
		}
		Landmark landmark = landmarks.get(global ? WorldLandmarks.GLOBAL : SurveyorClient.getClientUuid(), id);
		landmarks.remove(global ? WorldLandmarks.GLOBAL : SurveyorClient.getClientUuid(), id);
		feedback.accept(prefix().append(Component.literal("%s %s removed successfully!".formatted(landmark.owner().equals(WorldLandmarks.GLOBAL) ? "Landmark" : "Waypoint", id)).withStyle(ChatFormatting.GREEN)));
		return 1;
	}

	private static int addBlockLandmark(WorldSummary summary, Level world, Consumer<Component> feedback, BlockPos pos, boolean global) {
		WorldLandmarks landmarks = summary.landmarks();
		if (landmarks == null) {
			feedback.accept(prefix().append(Component.literal("The landmark system is dynamically disabled!").withStyle(ChatFormatting.YELLOW)));
			return 0;
		}
		Identifier id = Surveyor.id("block/%s/%s/%s".formatted(pos.getX(), pos.getY(), pos.getZ()));
		if (landmarks.contains(global ? WorldLandmarks.GLOBAL : SurveyorClient.getClientUuid(), id)) {
			feedback.accept(prefix().append(Component.literal("A landmark with this ID already exists! Replacing...").withStyle(ChatFormatting.YELLOW)));
		}
		landmarks.put(Landmark.create(global ? WorldLandmarks.GLOBAL : SurveyorClient.getClientUuid(), id, builder -> LandmarkComponentTypes.forBlock(builder, world, pos)));
		feedback.accept(prefix().append(Component.literal("Added new %s %s!".formatted(global ? "Landmark" : "Waypoint", id)).withStyle(ChatFormatting.GREEN)));
		return 1;
	}

	public static <T> T map(CommandContext<FabricClientCommandSource> context, SurveyorCommandExecutor<T> executor, boolean feedback) {
		LocalPlayer player = context.getSource().getPlayer();
		SurveyorExploration exploration = SurveyorClient.getExploration();
		try {
			return executor.execute(WorldSummary.of(context.getSource().getLevel()), player, context.getSource().getLevel(), exploration, t -> context.getSource().sendFeedback(t));
		} catch (Exception e) {
			if (feedback) context.getSource().sendFeedback(Component.literal("Command failed! Check log for details.").withStyle(ChatFormatting.RED));
			if (feedback) Surveyor.LOGGER.error("[Surveyor] Error while executing command: {}", context.getInput(), e);
			return null;
		}
	}

	public static int execute(CommandContext<FabricClientCommandSource> context, SurveyorCommandExecutor<Integer> executor) {
		return Objects.requireNonNullElse(map(context, executor, true), 0);
	}

	private static CommandSourceStack sourceForPos(FabricClientCommandSource source) {
		return new CommandSourceStack(null, source.getPosition(), source.getRotation(), null, PermissionSet.NO_PERMISSIONS, null, null, null, source.getEntity());
	}

	public static void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext registryAccess) {
		dispatcher.register(
			ClientCommands.literal("waypointsc")
				.requires(c -> !c.getClient().isInSingleplayer() && c.getClient().getNetworkHandler().getCommandDispatcher().findNode(List.of("surveyor")) == null)
				.requires(c -> Surveyor.CONFIG.landmarks != SystemMode.DISABLED)
				.executes(c -> execute(c, (w, p, sw, e, f) -> getLandmarks(w, e, f, false)))
				.then(ClientCommands.literal("new")
					.requires(c -> Surveyor.CONFIG.landmarks != SystemMode.FROZEN)
					.then(ClientCommands.literal("block")
						.then(ClientCommands.argument("pos", BlockPosArgument.blockPos())
							.executes(c -> execute(c, (w, p, sw, e, f) -> addBlockLandmark(w, sw, f, c.getArgument("pos", DefaultPosArgument.class).toAbsoluteBlockPos(sourceForPos(c.getSource())), false)))
						)
					)
				)
				.then(ClientCommands.literal("view")
					.then(ClientCommands.argument("id", IdentifierArgument.id())
						.suggests((c, b) -> SharedSuggestionProvider.suggestResource((Iterable<Identifier>) map(c, (w, p, sw, e, f) -> w.landmarks() == null ? new HashSet<Identifier>() : w.landmarks().asMap(SurveyorClient.getClientUuid(), p.canUseGameMasterBlocks() ? null : e).keySet(), false), b))
						.executes(c -> execute(c, (w, p, sw, e, f) -> viewLandmark(w, f, c.getArgument("id", Identifier.class), false)))
					)
				)
				.then(ClientCommands.literal("raw")
					.requires(c -> Surveyor.CONFIG.debugCommands)
					.then(ClientCommands.argument("id", IdentifierArgument.id())
						.suggests((c, b) -> SharedSuggestionProvider.suggestResource((Iterable<Identifier>) map(c, (w, p, sw, e, f) -> w.landmarks() == null ? new HashSet<Identifier>() : w.landmarks().asMap(SurveyorClient.getClientUuid(), p.canUseGameMasterBlocks() ? null : e).keySet(), false), b))
						.executes(c -> execute(c, (w, p, sw, e, f) -> rawLandmark(w, f, c.getArgument("id", Identifier.class), false)))
					)
				)
				.then(ClientCommands.literal("remove")
					.requires(c -> Surveyor.CONFIG.landmarks != SystemMode.FROZEN)
					.then(ClientCommands.argument("id", IdentifierArgument.id())
						.suggests((c, b) -> SharedSuggestionProvider.suggestResource((Iterable<Identifier>) map(c, (w, p, sw, e, f) -> w.landmarks() == null ? new HashSet<Identifier>() : w.landmarks().asMap(SurveyorClient.getClientUuid(), p.canUseGameMasterBlocks() ? null : e).keySet(), false), b))
						.executes(c -> execute(c, (w, p, sw, e, f) -> removeLandmark(w, f, c.getArgument("id", Identifier.class), false)))
					)
				)
		);
	}

	public interface SurveyorCommandExecutor<T> {
		T execute(WorldSummary currentWorldSummary, LocalPlayer player, ClientLevel world, SurveyorExploration exploration, Consumer<Component> feedback);
	}
}
