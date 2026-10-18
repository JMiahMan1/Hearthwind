package ht.tuber.graph;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

public class FloodFillImpl<T> implements FloodFill<T> {
   private final DirectedGraph<T> graph;
   private final Set<T> memory = new HashSet<>();
   private final Queue<T> nextNodes;
   private T nextNode;

   public FloodFillImpl(Collection<T> starts, DirectedGraph<T> graph, Function<T, Integer> heuristic) {
      this.graph = graph;
      this.nextNodes = new PriorityQueue<>(Comparator.comparing(heuristic));
      this.addNodes(starts);
   }

   private Stream<T> fill(Consumer<T> visitor) {
      return Stream.<T>iterate(this.nextNode, Objects::nonNull, ignored -> this.nextNode).peek(node -> {
         this.graph.getNeighbors((T)node).forEach(neighbor -> {
            if (!this.memory.contains(neighbor)) {
               visitor.accept((T)neighbor);
            }
         });
         this.nextNode = this.nextNodes.poll();
      });
   }

   @Override
   public Stream<T> fill() {
      return this.fill(this::visitNode);
   }

   @Override
   public Stream<T> fill(Collection<T> starts) {
      this.addNodes(starts);
      return this.fill();
   }

   public void fillOnce(Consumer<T> forEach) {
      List<T> nextNextNodes = new LinkedList<>();
      this.fill(node -> {
         this.memory.add(node);
         nextNextNodes.add(node);
      }).forEach(forEach);
      this.addNodes(nextNextNodes);
   }

   private void visitNode(T node) {
      this.memory.add(node);
      this.nextNodes.add(node);
   }

   public Stream<T> getNextNodes() {
      return this.nextNodes.stream();
   }

   private void addNodes(Collection<T> nodes) {
      nodes.forEach(this::visitNode);
      if (this.nextNode != null) {
         this.nextNodes.add(this.nextNode);
      }

      this.nextNode = this.nextNodes.poll();
   }
}
