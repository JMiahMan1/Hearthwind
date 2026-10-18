package ht.tuber.graph;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class GraphUtil {
   public static <T> FloodFill<T> flood(DirectedGraph<T> graph, T start, Function<T, Integer> heuristic) {
      return flood(graph, Collections.singletonList(start), heuristic);
   }

   public static <T> FloodFill<T> flood(DirectedGraph<T> graph, Collection<T> start, Function<T, Integer> heuristic) {
      return new FloodFillImpl<>(start, graph, heuristic);
   }

   public static <T> FloodFill<T> flood(DirectedGraph<T> graph, T start) {
      return flood(graph, Collections.singleton(start), a -> 0);
   }

   public static <T> FloodFill<T> flood(DirectedGraph<T> graph, Collection<T> start) {
      return flood(graph, start, a -> 0);
   }

   public static <T> DirectedGraph<T> filter(DirectedGraph<T> graph, Predicate<T> condition) {
      return new FilteredGraph<>(graph, condition, condition);
   }

   public static <T> DirectedGraph<T> filter(DirectedGraph<T> graph, Predicate<T> originFilter, Predicate<T> neighborFilter) {
      return new FilteredGraph<>(graph, originFilter, neighborFilter);
   }

   public static <T> DirectedGraph<T> filterSources(DirectedGraph<T> graph, Predicate<T> condition) {
      return new FilteredGraph<>(graph, condition, a -> true);
   }

   public static <T> DirectedGraph<T> filterNeighbors(DirectedGraph<T> graph, Predicate<T> condition) {
      return new FilteredGraph<>(graph, a -> true, condition);
   }

   public static <T> Stream<Link<T>> supportStream(DirectedGraph<T> sourceGraph, T bases) {
      return supportStream(sourceGraph, Collections.singleton(bases));
   }

   public static <T> Stream<Link<T>> supportStream(DirectedGraph<T> sourceGraph, Collection<T> bases) {
      Map<T, Integer> nodeValues = new HashMap<>();
      bases.forEach(sink -> nodeValues.put((T)sink, 0));
      Queue<T> nextNodes = new ArrayDeque<>(bases);
      Link<T> recycledLink = new Link<>();
      return Stream.<Object>iterate(nextNodes.poll(), Objects::nonNull, t -> nextNodes.poll()).flatMap(lowerNode -> {
         recycledLink.node = (T)lowerNode;
         int hops = nodeValues.get(lowerNode) + 1;
         return sourceGraph.getNeighbors((T)lowerNode).map(upperNode -> {
            Integer upperHops = nodeValues.getOrDefault(upperNode, hops + 1);
            if (upperHops >= hops) {
               if (upperHops > hops) {
                  nodeValues.put((T)upperNode, hops);
                  nextNodes.add((T)upperNode);
               }

               recycledLink.neighbor = (T)upperNode;
               return recycledLink;
            } else {
               return null;
            }
         }).takeWhile(Objects::nonNull);
      });
   }
}
