package ht.tuber.graph;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Stream;

public class SupportGraph<T> extends AbstractGraph<T> {
   public static <T> SupportGraph<T> create(DirectedGraph<T> sourceGraph, T base) {
      return create(sourceGraph, Collections.singleton(base));
   }

   public static <T> SupportGraph<T> create(DirectedGraph<T> sourceGraph, Collection<T> bases) {
      SupportGraph<T> graph = new SupportGraph<>();
      GraphUtil.supportStream(sourceGraph, bases).forEach(link -> graph.add(link.node, link.neighbor));
      return graph;
   }

   public Stream<T> getSupport(T node) {
      return this.getLocalSupport(node).distinct();
   }

   private Stream<T> getLocalSupport(T node) {
      return Stream.concat(Stream.of(node), this.getNeighbors(node).flatMap(this::getSupport));
   }

   @Override
   public boolean contains(T node) {
      return false;
   }

   @Override
   public Stream<T> getNeighbors(T node) {
      return null;
   }
}
