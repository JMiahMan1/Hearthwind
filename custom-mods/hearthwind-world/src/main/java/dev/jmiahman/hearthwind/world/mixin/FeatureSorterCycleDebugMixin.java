package dev.jmiahman.hearthwind.world.mixin;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.world.level.biome.FeatureSorter;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Worldgen hardening + diagnostic for feature-order cycles.
 *
 * The pack's mod set (Terralith biome overrides + Tectonic lithostitched
 * add_features + Natures Spirit's DispatchAlternateLayout biome injectors)
 * contains static feature-order conflicts that make the union feature graph
 * cyclic. Vanilla FeatureSorter treats any cycle as fatal: it throws
 * IllegalStateException during chunk feature sorting, c2me surfaces it as
 * "Error upgrading chunk", and affected chunks never finish loading ("loading
 * terrain" hang). Natures Spirit's biome injectors make lithostitched re-sort
 * over the FULL biome set, which is why the hang appears only with it enabled.
 *
 * ALWAYS-ON BEHAVIOR (no flag needed): replaces Graph#depthFirstSearch inside
 * FeatureSorter#buildFeaturesPerStep with a cycle-tolerant DFS that drops
 * back-edges (with a warning) instead of reporting a cycle. The sort then
 * yields a complete, exception-free order and worldgen proceeds. Dropped edges
 * only affect which of two mutually-conflicting features decorates first in
 * the rare overlapping biomes - cosmetic, versus a hard hang.
 *
 * DIAGNOSTIC BEHAVIOR (gated behind {@code -Dhearthwind.debug.featurecycle}),
 * keeps the original fatal DFS and, on first cycle detection:
 *  1. replays featuresPerSource.apply(source) for every source to rebuild the
 *     exact runtime flattened feature sequences,
 *  2. maps every placed feature instance to its registry key,
 *  3. maps every consecutive edge (a -> b) to the source(s) whose list has a
 *     then b adjacent,
 *  4. writes a full graph + per-source dump to feature_cycle_dump.txt and
 *     logs the cycle path with per-edge contributing source names.
 * Dumps at most once per JVM.
 */
@Mixin(FeatureSorter.class)
public abstract class FeatureSorterCycleDebugMixin {

	@Unique
	private static final boolean HEARTHWIND_CYCLE_DEBUG = Boolean.getBoolean("hearthwind.debug.featurecycle");

	@Unique
	private static final AtomicBoolean HEARTHWIND_DUMPED = new AtomicBoolean(false);

	@Unique
	private static final AtomicLong HEARTHWIND_DROPPED_EDGES = new AtomicLong();

	@Unique
	private static final Logger HEARTHWIND_CYCLE_LOG = LogUtils.getLogger();

	/**
	 * Cycle-tolerant replacement of the vanilla DFS. In normal (ungated) runs
	 * we never let vanilla's fatal cycle detection fire; the debug flag keeps
	 * vanilla behavior for reproducing/diagnosing cycles.
	 */
	@WrapOperation(
		method = "buildFeaturesPerStep(Ljava/util/List;Ljava/util/function/Function;Z)Ljava/util/List;",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/util/Graph;depthFirstSearch(Ljava/util/Map;Ljava/util/Set;Ljava/util/Set;Ljava/util/function/Consumer;Ljava/lang/Object;)Z"
		)
	)
	private static <T> boolean hearthwind$dumpCycleOnDetect(
			Map<T, Set<T>> graph, Set<T> visited, Set<T> inProgress, java.util.function.Consumer<T> order, T start,
			Operation<Boolean> original,
			List<T> sources, Function<T, List<HolderSet<PlacedFeature>>> featuresPerSource, boolean withCycleInfo) {
		if (!HEARTHWIND_CYCLE_DEBUG) {
			return hearthwind$tolerantDfs(graph, visited, inProgress, order, start);
		}
		boolean cyclic = original.call(graph, visited, inProgress, order, start);
		if (cyclic && HEARTHWIND_DUMPED.compareAndSet(false, true)) {
			hearthwind$dumpCycle(graph, start, sources, featuresPerSource);
		}
		return cyclic;
	}

	/**
	 * Mirrors net.minecraft.util.Graph#depthFirstSearch exactly, except that a
	 * back-edge (successor still in progress) is DROPPED with a warning instead
	 * of returning true - so the sort always completes without throwing.
	 */
	@Unique
	private static <T> boolean hearthwind$tolerantDfs(Map<T, Set<T>> graph, Set<T> visited, Set<T> inProgress,
			java.util.function.Consumer<T> order, T start) {
		if (visited.contains(start)) {
			return false;
		}
		if (inProgress.contains(start)) {
			// Only reachable from the recursive calls below; the outer caller
			// never sees true, so buildFeaturesPerStep cannot throw.
			return true;
		}
		inProgress.add(start);
		for (T next : graph.getOrDefault(start, Set.of())) {
			if (hearthwind$tolerantDfs(graph, visited, inProgress, order, next)) {
				long dropped = HEARTHWIND_DROPPED_EDGES.incrementAndGet();
				if (dropped <= 20) {
					HEARTHWIND_CYCLE_LOG.warn(
						"[hearthwind worldgen] dropping cyclic feature-order edge: {} -> {}",
						start, next);
				} else if (dropped % 200 == 0) {
					HEARTHWIND_CYCLE_LOG.warn(
						"[hearthwind worldgen] dropped {} cyclic feature-order edges so far (sorting continues)", dropped);
				}
			}
		}
		inProgress.remove(start);
		visited.add(start);
		order.accept(start);
		return false;
	}

	@Unique
	private static void hearthwind$dumpCycle(Map<?, ?> graphRaw, Object start, List<?> sources,
			Function<?, ? extends List<? extends Iterable<? extends Holder<? extends PlacedFeature>>>> featuresPerSource) {
		Map<Object, Set<Object>> graph = new HashMap<>();
		for (Map.Entry<?, ?> e : graphRaw.entrySet()) {
			Set<Object> targets = new HashSet<>();
			for (Object t : (Set<?>) e.getValue()) {
				targets.add(t);
			}
			graph.put(e.getKey(), targets);
		}

		// Replay the runtime per-source sequences to build:
		//  - placed instance -> registry key string
		//  - edge (fromPlaced -> toPlaced) -> set of contributing source names
		Map<Object, String> placedKeys = new IdentityHashMap<>();
		Map<Object, Map<Object, Set<String>>> edgeSources = new IdentityHashMap<>();
		for (Object src : sources) {
			String srcName = String.valueOf(src);
			List<? extends Iterable<? extends Holder<? extends PlacedFeature>>> steps;
			try {
				Object applied = ((Function<Object, ?>) featuresPerSource).apply(src);
				@SuppressWarnings("unchecked")
				List<? extends Iterable<? extends Holder<? extends PlacedFeature>>> cast =
					(List<? extends Iterable<? extends Holder<? extends PlacedFeature>>>) applied;
				steps = cast;
			} catch (Throwable t) {
				HEARTHWIND_CYCLE_LOG.warn("[hearthwind cycle-debug] could not replay features for source {}: {}", srcName, t);
				continue;
			}
			Object prev = null;
			for (Iterable<? extends Holder<? extends PlacedFeature>> step : steps) {
				for (Holder<? extends PlacedFeature> h : step) {
					PlacedFeature pf;
					try {
						pf = h.value();
					} catch (Throwable t) {
						continue;
					}
					String key = h.unwrapKey().map(k -> k.identifier().toString()).orElseGet(() -> "direct@" + System.identityHashCode(pf));
					placedKeys.putIfAbsent(pf, key);
					if (prev != null && prev != pf) {
						edgeSources.computeIfAbsent(prev, x -> new IdentityHashMap<>())
							.computeIfAbsent(pf, x -> new HashSet<>())
							.add(srcName);
					}
					prev = pf;
				}
			}
		}

		Map<Object, Object> parent = new HashMap<>();
		HEARTHWIND_CYCLE_LOG.error("[hearthwind cycle-debug] replay stats: sources={} placedKeys={} edgeSources={}",
			sources.size(), placedKeys.size(), edgeSources.size());
		hearthwind$writeDump(graph, sources, featuresPerSource, placedKeys, edgeSources);
		Deque<Object> stack = new ArrayDeque<>();
		Set<Object> onStack = new HashSet<>();
		Set<Object> done = new HashSet<>();
		stack.push(start);
		onStack.add(start);
		while (!stack.isEmpty()) {
			Object node = stack.peek();
			boolean advanced = false;
			for (Object next : graph.getOrDefault(node, Set.of())) {
				if (done.contains(next)) {
					continue;
				}
				if (onStack.contains(next)) {
					hearthwind$printCycle(parent, node, next, placedKeys, edgeSources);
					return;
				}
				parent.put(next, node);
				stack.push(next);
				onStack.add(next);
				advanced = true;
				break;
			}
			if (!advanced) {
				done.add(node);
				onStack.remove(node);
				stack.pop();
			}
		}
		HEARTHWIND_CYCLE_LOG.error(
			"[hearthwind cycle-debug] DFS reported a cycle but the diagnostic walk found none from start={}", start);
	}

	@Unique
	private static void hearthwind$writeDump(Map<Object, Set<Object>> graph, List<?> sources,
			Function<?, ? extends List<? extends Iterable<? extends Holder<? extends PlacedFeature>>>> featuresPerSource,
			Map<Object, String> placedKeys, Map<Object, Map<Object, Set<String>>> edgeSources) {
		StringBuilder sb = new StringBuilder();
		sb.append("=== GRAPH EDGES (node records) ===\n");
		for (Map.Entry<Object, Set<Object>> e : graph.entrySet()) {
			Object pa = hearthwind$placedOf(e.getKey());
			for (Object t : e.getValue()) {
				Object pb = hearthwind$placedOf(t);
				sb.append(placedKeys.getOrDefault(pa, String.valueOf(pa))).append(" -> ")
					.append(placedKeys.getOrDefault(pb, String.valueOf(pb))).append('\n');
			}
		}
		sb.append("=== SOURCE SEQUENCES ===\n");
		for (Object src : sources) {
			sb.append("--- SOURCE: ").append(src).append('\n');
			try {
				Object applied = ((Function<Object, ?>) featuresPerSource).apply(src);
				int step = 0;
				for (Object stepObj : (List<?>) applied) {
					sb.append("  step ").append(step).append(':');
					for (Object hObj : (Iterable<?>) stepObj) {
						Object pf = ((Holder<?>) hObj).value();
						sb.append(' ').append(placedKeys.getOrDefault(pf, String.valueOf(pf)));
					}
					sb.append('\n');
					step++;
				}
			} catch (Throwable t) {
				sb.append("  REPLAY FAILED: ").append(t).append('\n');
			}
		}
		try {
			java.nio.file.Files.writeString(java.nio.file.Path.of("feature_cycle_dump.txt"), sb.toString());
			HEARTHWIND_CYCLE_LOG.error("[hearthwind cycle-debug] full dump written to feature_cycle_dump.txt (cwd of server process)");
		} catch (Throwable t) {
			HEARTHWIND_CYCLE_LOG.error("[hearthwind cycle-debug] dump write failed", t);
		}
	}

	@Unique
	private static Object hearthwind$placedOf(Object node) {
		try {
			return node.getClass().getMethod("feature").invoke(node);
		} catch (Throwable t) {
			return null;
		}
	}

	@Unique
	private static void hearthwind$printCycle(Map<Object, Object> parent, Object from, Object backTo,
			Map<Object, String> placedKeys, Map<Object, Map<Object, Set<String>>> edgeSources) {
		List<Object> path = new ArrayList<>();
		Object cur = from;
		int guard = 0;
		while (cur != backTo && guard++ < 10000) {
			path.add(cur);
			Object up = parent.get(cur);
			if (up == null) {
				break;
			}
			cur = up;
		}
		path.add(backTo);
		// The path was built by walking parent links (child -> parent), so its
		// entries are in REVERSE edge order. Reverse it so consecutive entries
		// (i -> i+1) correspond to real graph edges, closing back to path[0].
		java.util.Collections.reverse(path);
		StringBuilder sb = new StringBuilder();
		sb.append("\n[hearthwind cycle-debug] FEATURE ORDER CYCLE (").append(path.size())
			.append(" nodes). Per node: placedKey (index, step, innerFeature). Per edge: sources whose runtime list has that adjacency.\n");
		for (int i = 0; i < path.size(); i++) {
			Object a = path.get(i);
			Object b = path.get((i + 1) % path.size());
			Object pa = hearthwind$placedOf(a);
			Object pb = hearthwind$placedOf(b);
			String ka = pa != null ? placedKeys.get(pa) : null;
			String kb = pb != null ? placedKeys.get(pb) : null;
			sb.append("  ").append(ka != null ? ka : "?").append("  node: ").append(a).append('\n');
			Set<String> srcs = (pa != null && pb != null && edgeSources.get(pa) != null)
				? edgeSources.get(pa).get(pb) : null;
			sb.append("    -> ").append(kb != null ? kb : "?")
				.append("   EDGE FROM SOURCES: ").append(srcs != null ? srcs : "(unattributed)").append('\n');
		}
		HEARTHWIND_CYCLE_LOG.error("{}", sb);
	}
}
