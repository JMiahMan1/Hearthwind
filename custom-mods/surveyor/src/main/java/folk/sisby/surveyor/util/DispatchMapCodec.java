package folk.sisby.surveyor.util;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.mojang.datafixers.util.Pair;
import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

public class DispatchMapCodec<K, V> implements Codec<Map<K, V>> {
   private final Codec<K> keyCodec;
   private final Function<K, Codec<V>> valueCodec;

   public DispatchMapCodec(Codec<K> keyCodec, Function<K, Codec<V>> valueCodec) {
      this.keyCodec = keyCodec;
      this.valueCodec = valueCodec;
   }

   public static <A, B> DispatchMapCodec<A, B> of(Codec<A> keyCodec, Function<A, Codec<B>> valueCodec) {
      return new DispatchMapCodec<>(keyCodec, valueCodec);
   }

   private <T> DataResult<Map<K, V>> decode(DynamicOps<T> ops, MapLike<T> input) {
      Builder<K, V> read = ImmutableMap.builder();
      com.google.common.collect.ImmutableList.Builder<Pair<T, T>> failed = ImmutableList.builder();
      DataResult<Unit> result = input.entries().reduce(DataResult.success(Unit.INSTANCE, Lifecycle.stable()), (r, pair) -> {
         DataResult<K> key = this.keyCodec.parse(ops, pair.getFirst());
         DataResult<V> value = key.map(this.valueCodec).flatMap(codec -> codec.parse(ops, pair.getSecond()));
         DataResult<Pair<K, V>> entry = key.apply2stable(Pair::of, value);
         entry.error().ifPresent(e -> failed.add(pair));
         return r.apply2stable((u, p) -> {
            read.put(p.getFirst(), p.getSecond());
            return u;
         }, entry);
      }, (r1, r2) -> r1.apply2stable((u1, u2) -> u1, r2));
      Map<K, V> elements = new HashMap<>(read.build());
      T errors = (T)ops.createMap(failed.build().stream());
      return result.map(unit -> elements).setPartial(elements).mapError(e -> e + " missed input: " + errors);
   }

   private <T> RecordBuilder<T> encode(Map<K, V> input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
      for (Entry<K, V> entry : input.entrySet()) {
         prefix.add(this.keyCodec.encodeStart(ops, entry.getKey()), this.valueCodec.apply(entry.getKey()).encodeStart(ops, entry.getValue()));
      }

      return prefix;
   }

   public <T> DataResult<Pair<Map<K, V>, T>> decode(DynamicOps<T> ops, T input) {
      return ops.getMap(input).setLifecycle(Lifecycle.stable()).flatMap(map -> this.decode(ops, map)).map(r -> Pair.of(r, input));
   }

   public <T> DataResult<T> encode(Map<K, V> input, DynamicOps<T> ops, T prefix) {
      return this.encode(input, ops, ops.mapBuilder()).build(prefix);
   }
}
