package folk.sisby.surveyor;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Table;
import folk.sisby.surveyor.config.NetworkMode;
import folk.sisby.surveyor.landmark.Landmark;
import folk.sisby.surveyor.landmark.WorldLandmarks;
import folk.sisby.surveyor.packet.C2SKnownLandmarksPacket;
import folk.sisby.surveyor.packet.C2SKnownStructuresPacket;
import folk.sisby.surveyor.packet.C2SKnownTerrainPacket;
import folk.sisby.surveyor.packet.C2SPacket;
import folk.sisby.surveyor.packet.S2CGroupAmendedPacket;
import folk.sisby.surveyor.packet.S2CUpdateRegionPacket;
import folk.sisby.surveyor.packet.SyncLandmarksRequestedPacket;
import folk.sisby.surveyor.packet.S2CGroupChangedPacket;
import folk.sisby.surveyor.packet.S2CGroupUpdatedPacket;
import folk.sisby.surveyor.packet.S2CStructuresAddedPacket;
import folk.sisby.surveyor.packet.SyncLandmarksAddedPacket;
import folk.sisby.surveyor.packet.SyncLandmarksRemovedPacket;
import folk.sisby.surveyor.structure.WorldStructures;
import folk.sisby.surveyor.terrain.WorldTerrain;
import folk.sisby.surveyor.util.MapUtil;
import folk.sisby.surveyor.util.RegionPos;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.Structure;

import java.util.BitSet;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public class SurveyorNetworking {

	public static BiConsumer<RegistryAccess, C2SPacket> C2S_SENDER = (r, p) -> {
	};

	public static void init() {
		PayloadTypeRegistry.serverboundPlay().register(C2SKnownTerrainPacket.ID, C2SKnownTerrainPacket.CODEC);
		PayloadTypeRegistry.serverboundPlay().register(C2SKnownStructuresPacket.ID, C2SKnownStructuresPacket.CODEC);
		PayloadTypeRegistry.serverboundPlay().register(C2SKnownLandmarksPacket.ID, C2SKnownLandmarksPacket.CODEC);
		PayloadTypeRegistry.serverboundPlay().register(SyncLandmarksAddedPacket.ID, SyncLandmarksAddedPacket.CODEC);
		PayloadTypeRegistry.serverboundPlay().register(SyncLandmarksRemovedPacket.ID, SyncLandmarksRemovedPacket.CODEC);
		PayloadTypeRegistry.serverboundPlay().register(SyncLandmarksRequestedPacket.ID, SyncLandmarksRequestedPacket.CODEC);

		PayloadTypeRegistry.clientboundPlay().register(S2CUpdateRegionPacket.ID, S2CUpdateRegionPacket.CODEC);
		PayloadTypeRegistry.clientboundPlay().register(S2CStructuresAddedPacket.ID, S2CStructuresAddedPacket.CODEC);
		PayloadTypeRegistry.clientboundPlay().register(S2CGroupChangedPacket.ID, S2CGroupChangedPacket.CODEC);
		PayloadTypeRegistry.clientboundPlay().register(S2CGroupAmendedPacket.ID, S2CGroupAmendedPacket.CODEC);
		PayloadTypeRegistry.clientboundPlay().register(S2CGroupUpdatedPacket.ID, S2CGroupUpdatedPacket.CODEC);
		PayloadTypeRegistry.clientboundPlay().register(SyncLandmarksAddedPacket.ID, SyncLandmarksAddedPacket.CODEC);
		PayloadTypeRegistry.clientboundPlay().register(SyncLandmarksRemovedPacket.ID, SyncLandmarksRemovedPacket.CODEC);
		PayloadTypeRegistry.clientboundPlay().register(SyncLandmarksRequestedPacket.ID, SyncLandmarksRequestedPacket.CODEC);

		ServerPlayNetworking.registerGlobalReceiver(C2SKnownTerrainPacket.ID, (packet, context) -> handleServer(packet, context, SurveyorNetworking::handleKnownTerrain));
		ServerPlayNetworking.registerGlobalReceiver(C2SKnownStructuresPacket.ID, (packet, context) -> handleServer(packet, context, SurveyorNetworking::handleKnownStructures));
		ServerPlayNetworking.registerGlobalReceiver(C2SKnownLandmarksPacket.ID, (packet, context) -> handleServer(packet, context, SurveyorNetworking::handleKnownLandmarks));
		ServerPlayNetworking.registerGlobalReceiver(SyncLandmarksAddedPacket.ID, (packet, context) -> handleServer(packet, context, SurveyorNetworking::handleLandmarksAdded));
		ServerPlayNetworking.registerGlobalReceiver(SyncLandmarksRemovedPacket.ID, (packet, context) -> handleServer(packet, context, SurveyorNetworking::handleLandmarksRemoved));
		ServerPlayNetworking.registerGlobalReceiver(SyncLandmarksRequestedPacket.ID, (packet, context) -> handleServer(packet, context, SurveyorNetworking::handleLandmarksRequested));
	}

	private static void handleKnownTerrain(MinecraftServer server, ServerPlayer player, C2SKnownTerrainPacket packet) {
		if (Surveyor.CONFIG.networking.terrain.atMost(NetworkMode.NONE)) return;
		int regions = 0;
		int chunks = 0;
		for (ServerLevel world : server.getAllLevels()) {
			WorldTerrain terrain = WorldTerrain.of(world);
			if (terrain == null) continue;
			Map<RegionPos, BitSet> serverBits = terrain.bitSet(Surveyor.explorationForMode(Surveyor.CONFIG.networking.terrain, player));
			Map<RegionPos, BitSet> clientBits = packet.chunks().row(world.dimension());
			for (RegionPos regionPos : serverBits.keySet()) {
				BitSet set = serverBits.get(regionPos);
				if (clientBits.containsKey(regionPos)) set.andNot(clientBits.get(regionPos));
				if (!set.isEmpty()) {
					regions++;
					chunks += set.cardinality();
					terrain.queueUpdate(regionPos, set, player);
				}
			}
		}
		if (regions > 0) Surveyor.LOGGER.info("[Surveyor] Syncing {} missing chunks over {} regions to player {}.", chunks, regions, player.getGameProfile().name());
	}

	private static void handleKnownStructures(MinecraftServer server, ServerPlayer player, C2SKnownStructuresPacket packet) {
		if (Surveyor.CONFIG.networking.structures.atMost(NetworkMode.NONE)) return;
		for (ServerLevel world : server.getAllLevels()) {
			WorldStructures structures = WorldStructures.of(world);
			if (structures == null) continue;
			Multimap<ResourceKey<Structure>, ChunkPos> starts = structures.keySet(Surveyor.explorationForMode(Surveyor.CONFIG.networking.structures, player));
			packet.starts().get(world.dimension()).forEach(starts::remove);
			if (starts.isEmpty()) continue;
			SurveyorExploration personalExploration = SurveyorExploration.of(player);
			Multimap<ResourceKey<Structure>, ChunkPos> personalStarts = personalExploration.limit(world.dimension(), HashMultimap.create(starts));
			if (!personalStarts.isEmpty()) S2CStructuresAddedPacket.of(false, personalStarts, structures).send(player);
			personalStarts.forEach(starts::remove);
			if (!starts.isEmpty()) S2CStructuresAddedPacket.of(true, starts, structures).send(player);
			if (!personalStarts.isEmpty() || !starts.isEmpty()) Surveyor.LOGGER.info("[Surveyor] Syncing {} personal and {} shared structures to player {} for {}", personalStarts.size(), starts.size(), player.getGameProfile().name(), world.dimension().identifier());
		}
	}

	private static void handleKnownLandmarks(MinecraftServer server, ServerPlayer player, C2SKnownLandmarksPacket packet) {
		if (Surveyor.CONFIG.networking.landmarks.atMost(NetworkMode.NONE)) return;
		UUID uuid = Surveyor.getUuid(player);
		for (ServerLevel world : server.getAllLevels()) {
			WorldSummary summary = WorldSummary.of(world);
			WorldLandmarks landmarks = WorldLandmarks.of(world);
			if (summary == null || landmarks == null) continue;
			Multimap<UUID, Identifier> keySet = landmarks.keySet(Surveyor.explorationForMode(Surveyor.CONFIG.networking.landmarks, player));
			Multimap<UUID, Identifier> addLandmarks = HashMultimap.create(keySet);
			if (!Surveyor.CONFIG.forceUpdateLandmarks) packet.landmarks().get(summary.dimension()).forEach(addLandmarks::remove);
			if (!addLandmarks.isEmpty()) SyncLandmarksAddedPacket.of(addLandmarks, landmarks).send(player);
			Multimap<UUID, Identifier> removeLandmarks = HashMultimap.create(packet.landmarks().get(summary.dimension()));
			Multimap<UUID, Identifier> removedLandmarks = landmarks.removed();
			removeLandmarks.entries().removeIf(e -> !(removedLandmarks.containsEntry(e.getKey(), e.getValue()) || (!keySet.containsEntry(e.getKey(), e.getValue()) && !e.getKey().equals(WorldLandmarks.GLOBAL) && !e.getKey().equals(uuid))));
			if (!removeLandmarks.isEmpty()) new SyncLandmarksRemovedPacket(summary.dimension(), removeLandmarks).send(player);
			Multimap<UUID, Identifier> unknownWaypoints = HashMultimap.create();
			unknownWaypoints.putAll(uuid, packet.landmarks().get(summary.dimension()).get(uuid));
			landmarks.keySet(null).get(uuid).forEach(id -> unknownWaypoints.remove(uuid, id));
			removedLandmarks.get(uuid).forEach(id -> unknownWaypoints.remove(uuid, id));
			if (!unknownWaypoints.isEmpty()) new SyncLandmarksRequestedPacket(summary.dimension(), unknownWaypoints).send(player);
			if (!addLandmarks.isEmpty() || !removeLandmarks.isEmpty() || !unknownWaypoints.isEmpty()) Surveyor.LOGGER.info("[Surveyor] Syncing {} landmarks and {} removals and {} unknowns from player {}", addLandmarks.size(), removeLandmarks.size(), unknownWaypoints.size(), player.getGameProfile().name());
		}
	}

	private static void handleLandmarksAdded(MinecraftServer server, ServerPlayer player, SyncLandmarksAddedPacket packet) {
		if (Surveyor.CONFIG.networking.landmarks.atMost(NetworkMode.NONE)) return;
		ServerLevel world = server.getLevel(packet.dimension());
		if (world == null) return;
		WorldLandmarks landmarks = WorldLandmarks.of(world);
		if (landmarks == null) return;
		Multimap<UUID, Identifier> keys = MapUtil.keyMultiMap(landmarks.readUpdatePacket(packet, player));
		if (!keys.isEmpty()) Surveyor.LOGGER.info("[Surveyor] Adding landmark(s) from player {} - {}", player.getGameProfile().name(), keys.values().stream().map(Identifier::toString).collect(Collectors.joining(", ")));
	}

	private static void handleLandmarksRemoved(MinecraftServer server, ServerPlayer sender, SyncLandmarksRemovedPacket packet) {
		if (Surveyor.CONFIG.networking.landmarks.atMost(NetworkMode.NONE)) return;
		ServerLevel world = server.getLevel(packet.dimension());
		if (world == null) return;
		WorldLandmarks landmarks = WorldLandmarks.of(world);
		if (landmarks == null) return;
		Table<UUID, Identifier, Landmark> changed = landmarks.readUpdatePacket(packet, sender);
		if (!changed.isEmpty()) {
			Multimap<UUID, Identifier> keys = MapUtil.keyMultiMap(changed);
			Surveyor.LOGGER.info("[Surveyor] Removing landmark(s) for player {} - {}", sender.getGameProfile().name(), keys.values().stream().map(Identifier::toString).collect(Collectors.joining(", ")));
		}
	}

	private static void handleLandmarksRequested(MinecraftServer server, ServerPlayer player, SyncLandmarksRequestedPacket packet) {
		if (Surveyor.CONFIG.networking.landmarks.atMost(NetworkMode.NONE)) return;
		ServerLevel world = server.getLevel(packet.dimension());
		if (world == null) return;
		WorldLandmarks landmarks = WorldLandmarks.of(world);
		if (landmarks == null) return;
		Multimap<UUID, Identifier> allowed = SurveyorExploration.ofShared(player).limit(world.dimension(), landmarks, HashMultimap.create(packet.landmarks()));
		if (!allowed.isEmpty()) {
			landmarks.createUpdatePacket(allowed).send(player);
			Surveyor.LOGGER.info("[Surveyor] Sending requested landmark(s) to player {} - {}", player.getGameProfile().name(), allowed.values().stream().map(Identifier::toString).collect(Collectors.joining(", ")));
		}
	}

	private static <T extends C2SPacket> void handleServer(T packet, ServerPlayNetworking.Context context, ServerPacketHandler<T> handler) {
		handler.handle(context.server(), context.player(), packet);
	}

	public interface ServerPacketHandler<T> {
		void handle(MinecraftServer server, ServerPlayer player, T packet);
	}
}
