package folk.sisby.surveyor;

import folk.sisby.surveyor.config.NetworkMode;
import folk.sisby.surveyor.config.SurveyorConfig;
import folk.sisby.surveyor.landmark.WorldLandmarks;
import folk.sisby.surveyor.landmark.component.LandmarkComponentTypes;
import folk.sisby.surveyor.mixin.AccessServerPlayerEntity;
import folk.sisby.surveyor.structure.StructureStartSummary;
import folk.sisby.surveyor.structure.WorldStructures;
import folk.sisby.surveyor.terrain.WorldTerrain;
import folk.sisby.surveyor.util.RaycastUtil;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.structure.Structure;
import org.apache.commons.lang3.text.WordUtils;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Map;
import java.util.UUID;

public class Surveyor implements ModInitializer {
	public static final String ID = "surveyor";
	public static final Logger LOGGER = LoggerFactory.getLogger(ID);
	public static final String DATA_SUBFOLDER = "data";
	public static final SurveyorConfig CONFIG = SurveyorConfig.createToml(FabricLoader.getInstance().getConfigDir(), "", "surveyor", SurveyorConfig.class);

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(ID, path);
	}

	public static File getWorldPath(ResourceKey<Level> dimension, MinecraftServer server) {
		// 26.2 has no DimensionType.getSaveDirectory; keep surveyor's own
		// per-dimension layout under dimensions/<ns>/<path>/data/surveyor
		return server.getWorldPath(LevelResource.ROOT).resolve("dimensions").resolve(dimension.identifier().getNamespace()).resolve(dimension.identifier().getPath()).resolve(DATA_SUBFOLDER).resolve(Surveyor.ID).toFile();
	}

	public static void checkStructureExploration(ServerLevel world, ServerPlayer player, BlockPos pos) {
		if (!world.hasChunk(pos.getX() >> 4, pos.getZ() >> 4)) return;
		WorldStructures structures = WorldStructures.of(world);
		if (structures == null) return;
		Registry<Structure> structureRegistry = world.registryAccess().lookupOrThrow(Registries.STRUCTURE);
		SurveyorExploration exploration = SurveyorExploration.of(player);
		Map<Structure, LongSet> structureReferences = world.getChunk(pos.getX() >> 4, pos.getZ() >> 4, ChunkStatus.STRUCTURE_REFERENCES, true).getAllReferences();
		if (!structureReferences.isEmpty()) {
			for (Structure structure : structureReferences.keySet()) {
				ResourceKey<Structure> structureKey = structureRegistry.getResourceKey(structure).orElseThrow();
				for (Long longPos : structureReferences.get(structure)) {
					ChunkPos startPos = ChunkPos.unpack(longPos);
					if (exploration.exploredStructure(world.dimension(), structureKey, startPos)) continue;
					StructureStartSummary start = structures.get(structureKey, startPos);
					if (start == null) continue;
					if (start.getBoundingBox().inflatedBy(2).isInside(pos)) {
						for (StructurePiece piece : start.getPieces()) {
							if (piece.getBoundingBox().inflatedBy(2).isInside(pos)) {
								exploration.addStructure(world.dimension(), structureKey, startPos);
								if (CONFIG.discoveryMessages) {
									player.sendSystemMessage(Component.literal("Discovered ").append(Component.literal(WordUtils.capitalize(structureKey.identifier().getPath().replace("_", " "))).withStyle(ChatFormatting.GREEN)).append(Component.literal(" at ")).append(Component.literal("[%s,%s]".formatted(startPos.x() << 4, startPos.z() << 4)).withStyle(ChatFormatting.GOLD)).withStyle(ChatFormatting.GRAY), true);
								}
								break;
							}
						}
					}
				}
			}
		}
	}

	public static boolean canModify(UUID landmarkOwner, @Nullable ServerPlayer player) {
		return player == null || player.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER) || landmarkOwner.equals(Surveyor.getUuid(player)) || !landmarkOwner.equals(WorldLandmarks.GLOBAL) && (Surveyor.CONFIG.networking.waypoints.atLeast(NetworkMode.SERVER) || (Surveyor.CONFIG.networking.waypoints.atLeast(NetworkMode.GROUP) && ServerSummary.of(((AccessServerPlayerEntity) player).getServer()).getGroup(Surveyor.getUuid(player)).contains(landmarkOwner)));
	}

	public static UUID getUuid(ServerPlayer player) {
		return ((AccessServerPlayerEntity) player).getServer() != null && ((AccessServerPlayerEntity) player).getServer().isSingleplayerOwner(player.nameAndId()) ? ServerSummary.HOST : player.getUUID();
	}

	public static ServerPlayer getPlayer(MinecraftServer server, UUID uuid) {
		if (uuid == ServerSummary.HOST) {
			for (ServerPlayer candidate : server.getPlayerList().getPlayers()) {
				if (server.isSingleplayerOwner(candidate.nameAndId())) return candidate;
			}
			return null;
		}
		return server.getPlayerList().getPlayer(uuid);
	}

	static SurveyorExploration explorationForMode(NetworkMode mode, ServerPlayer player) {
		return mode.atLeast(NetworkMode.SERVER) ? null : mode.atLeast(NetworkMode.GROUP) ? SurveyorExploration.ofShared(player) : mode.atLeast(NetworkMode.SOLO) ? SurveyorExploration.of(player) : PlayerSummary.OfflinePlayerSummary.OfflinePlayerExploration.empty(player.getUUID());
	}

	@Override
	public void onInitialize() {
		SurveyorNetworking.init();
		LandmarkComponentTypes.touch();
		CommandRegistrationCallback.EVENT.register(SurveyorCommands::registerCommands);
		ServerPlayConnectionEvents.JOIN.register(ServerSummary::onPlayerJoin);
		ServerChunkEvents.CHUNK_LOAD.register(WorldTerrain::onChunkLoad);
		ServerChunkEvents.CHUNK_LOAD.register(WorldStructures::onChunkLoad);
		ServerChunkEvents.CHUNK_UNLOAD.register(WorldTerrain::onChunkUnload);
		ServerTickEvents.END_SERVER_TICK.register(ServerSummary::onTick);
		ServerTickEvents.END_LEVEL_TICK.register(WorldTerrain::onTick);
		ServerTickEvents.END_LEVEL_TICK.register((world -> {
			if ((world.getGameTime() & 7) != 0) return;
			for (ServerPlayer player : world.players()) {
				checkStructureExploration(world, player, player.blockPosition());
				checkStructureExploration(world, player, BlockPos.containing(RaycastUtil.playerViewRaycast(player, PlayerSummary.of(player).viewDistance()).getLocation()));
			}
		}));
		LOGGER.info("[Surveyor] is not a map mod");
	}
}
