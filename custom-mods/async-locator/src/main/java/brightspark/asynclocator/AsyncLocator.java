package brightspark.asynclocator;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.Structure;

import java.text.NumberFormat;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class AsyncLocator {
	private static ExecutorService LOCATING_EXECUTOR_SERVICE = null;

	private AsyncLocator() {}

	public static void setupExecutorService() {
		shutdownExecutorService();

		int threads = ALConfig.locatorThreads();
		ALConstants.logInfo("Starting locating executor service with thread pool size of {}", threads);
		LOCATING_EXECUTOR_SERVICE = Executors.newFixedThreadPool(
			threads,
			new ThreadFactory() {
				private static final AtomicInteger poolNum = new AtomicInteger(1);
				private final AtomicInteger threadNum = new AtomicInteger(1);
				private final String namePrefix = ALConstants.MOD_ID + "-" + poolNum.getAndIncrement() + "-thread-";

				@Override
				public Thread newThread(Runnable r) {
					return new Thread(r, namePrefix + threadNum.getAndIncrement());
				}
			}
		);
	}

	public static void shutdownExecutorService() {
		if (LOCATING_EXECUTOR_SERVICE != null) {
			ALConstants.logInfo("Shutting down locating executor service");
			LOCATING_EXECUTOR_SERVICE.shutdown();
			LOCATING_EXECUTOR_SERVICE = null;
		}
	}

	/**
	 * Queues a task to locate a feature using {@link ServerLevel#findNearestMapStructure(TagKey, BlockPos, int, boolean)}
	 * and returns a {@link LocateTask} with the futures for it.
	 */
	public static LocateTask<BlockPos> locate(
		ServerLevel level,
		TagKey<Structure> structureTag,
		BlockPos pos,
		int searchRadius,
		boolean skipKnownStructures
	) {
		ALConstants.logDebug(
			"Creating locate task for {} in {} around {} within {} chunks",
			structureTag, level, pos, searchRadius
		);
		CompletableFuture<BlockPos> completableFuture = new CompletableFuture<>();
		Future<?> future = LOCATING_EXECUTOR_SERVICE.submit(
			() -> doLocateLevel(completableFuture, level, structureTag, pos, searchRadius, skipKnownStructures)
		);
		return new LocateTask<>(level.getServer(), completableFuture, future);
	}

	/**
	 * Queues a task to locate a feature using
	 * {@link ChunkGenerator#findNearestMapStructure(ServerLevel, HolderSet, BlockPos, int, boolean)} and returns a
	 * {@link LocateTask} with the futures for it.
	 */
	public static LocateTask<Pair<BlockPos, Holder<Structure>>> locate(
		ServerLevel level,
		HolderSet<Structure> structureSet,
		BlockPos pos,
		int searchRadius,
		boolean skipKnownStructures
	) {
		ALConstants.logDebug(
			"Creating locate task for {} in {} around {} within {} chunks",
			structureSet, level, pos, searchRadius
		);
		CompletableFuture<Pair<BlockPos, Holder<Structure>>> completableFuture = new CompletableFuture<>();
		Future<?> future = LOCATING_EXECUTOR_SERVICE.submit(
			() -> doLocateChunkGenerator(completableFuture, level, structureSet, pos, searchRadius, skipKnownStructures)
		);
		return new LocateTask<>(level.getServer(), completableFuture, future);
	}

	private static void doLocateLevel(
		CompletableFuture<BlockPos> completableFuture,
		ServerLevel level,
		TagKey<Structure> structureTag,
		BlockPos pos,
		int searchRadius,
		boolean skipExistingChunks
	) {
		ALConstants.logInfo(
			"Trying to locate {} in {} around {} within {} chunks",
			structureTag, level, pos, searchRadius
		);
		long start = System.nanoTime();
		BlockPos foundPos = level.findNearestMapStructure(structureTag, pos, searchRadius, skipExistingChunks);
		String time = NumberFormat.getNumberInstance().format(TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start));
		if (foundPos == null)
			ALConstants.logInfo("No {} found (took {}ms)", structureTag, time);
		else
			ALConstants.logInfo("Found {} at {} (took {}ms)", structureTag, foundPos, time);
		completableFuture.complete(foundPos);
	}

	private static void doLocateChunkGenerator(
		CompletableFuture<Pair<BlockPos, Holder<Structure>>> completableFuture,
		ServerLevel level,
		HolderSet<Structure> structureSet,
		BlockPos pos,
		int searchRadius,
		boolean skipExistingChunks
	) {
		ALConstants.logInfo(
			"Trying to locate {} in {} around {} within {} chunks",
			structureSet, level, pos, searchRadius
		);
		long start = System.nanoTime();
		Pair<BlockPos, Holder<Structure>> foundPair = level.getChunkSource().getGenerator()
			.findNearestMapStructure(level, structureSet, pos, searchRadius, skipExistingChunks);
		String time = NumberFormat.getNumberInstance().format(TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start));
		if (foundPair == null)
			ALConstants.logInfo("No {} found (took {}ms)", structureSet, time);
		else
			ALConstants.logInfo("Found {} at {} (took {}ms)",
				foundPair.getSecond().value().getClass().getSimpleName(), foundPair.getFirst(), time
			);
		completableFuture.complete(foundPair);
	}

	/**
	 * Holder of the futures for an async locate task as well as providing some helper functions.
	 */
	public record LocateTask<T>(MinecraftServer server, CompletableFuture<T> completableFuture, Future<?> taskFuture) {
		public LocateTask<T> then(Consumer<T> action) {
			completableFuture.thenAccept(action);
			return this;
		}

		public LocateTask<T> thenOnServerThread(Consumer<T> action) {
			completableFuture.thenAccept(pos -> server.submit(() -> action.accept(pos)));
			return this;
		}

		public void cancel() {
			taskFuture.cancel(true);
			completableFuture.cancel(false);
		}
	}
}
