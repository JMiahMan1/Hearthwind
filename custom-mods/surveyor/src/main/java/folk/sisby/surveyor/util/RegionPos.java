package folk.sisby.surveyor.util;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.BitSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.ChunkPos;
import org.jetbrains.annotations.NotNull;

public record RegionPos(int x, int z) {
   public static final Codec<RegionPos> CODEC = Codec.STRING.xmap(RegionPos::of, RegionPos::toString);
   public static final StreamCodec<ByteBuf, RegionPos> PACKET_CODEC = ByteBufCodecs.VAR_LONG.map(RegionPos::of, RegionPos::toLong);
   public static final int CHUNK_POWER = 5;
   public static final int CHUNK_SIZE = 32;
   public static final int CHUNK_AREA = 1024;
   public static final int BLOCK_POWER = 9;
   public static final int BLOCK_SIZE = 512;
   public static final int BLOCK_AREA = 262144;

   public static int regionRelative(int xz) {
      return xz & 31;
   }

   public static int regionToChunk(int xz) {
      return xz << 5;
   }

   public static int chunkToRegion(int xz) {
      return xz >> 5;
   }

   public static int regionToBlock(int xz) {
      return xz << 9;
   }

   public static int blockToRegion(int xz) {
      return xz >> 9;
   }

   public static int chunkToBit(int relativeChunkX, int relativeChunkZ) {
      return (relativeChunkX << 5) + relativeChunkZ;
   }

   public static int chunkToBit(ChunkPos pos) {
      return chunkToBit(regionRelative(pos.x()), regionRelative(pos.z()));
   }

   public static BitSet chunkToBitSet(ChunkPos pos) {
      BitSet chunks = new BitSet(1024);
      chunks.set(chunkToBit(pos));
      return chunks;
   }

   public static int bitToX(int bit) {
      return bit >> 5;
   }

   public static int bitToZ(int bit) {
      return bit & 31;
   }

   public static RegionPos of(BlockPos pos) {
      return new RegionPos(blockToRegion(pos.getX()), blockToRegion(pos.getZ()));
   }

   public static RegionPos of(ChunkPos pos) {
      return new RegionPos(chunkToRegion(pos.x()), chunkToRegion(pos.z()));
   }

   public static RegionPos of(long pos) {
      return new RegionPos((int)pos, (int)(pos >> 32));
   }

   public static RegionPos of(String pos) {
      return new RegionPos(Integer.parseInt(pos.split(",")[0]), Integer.parseInt(pos.split(",")[1]));
   }

   @NotNull
   @Override
   public String toString() {
      return this.x + "," + this.z;
   }

   public int chunkX() {
      return regionToChunk(this.x);
   }

   public int chunkZ() {
      return regionToChunk(this.z);
   }

   public int blockX() {
      return regionToBlock(this.x);
   }

   public int blockZ() {
      return regionToBlock(this.z);
   }

   public long toLong() {
      return ChunkPos.pack(this.x, this.z);
   }

   public ChunkPos toChunk() {
      return new ChunkPos(this.chunkX(), this.chunkZ());
   }

   public BlockPos toBlock(int y) {
      return new BlockPos(this.blockX(), y, this.blockZ());
   }

   public ChunkPos toChunk(int relativeChunkX, int relativeChunkZ) {
      return new ChunkPos(this.chunkX() + relativeChunkX, this.chunkZ() + relativeChunkZ);
   }

   public ChunkPos toChunk(int bit) {
      return this.toChunk(bitToX(bit), bitToZ(bit));
   }

   public Set<ChunkPos> toChunks(BitSet bits) {
      return bits.stream().mapToObj(this::toChunk).collect(Collectors.toSet());
   }

   public Set<ChunkPos> toChunks() {
      return IntStream.range(0, 256).mapToObj(this::toChunk).collect(Collectors.toSet());
   }

   public void forXZ(BiConsumer<Integer, Integer> action) {
      for (int x = 0; x < 32; x++) {
         for (int z = 0; z < 32; z++) {
            action.accept(x, z);
         }
      }
   }

   public static Map<RegionPos, BitSet> chunksToRegions(Iterable<ChunkPos> chunks) {
      Map<RegionPos, BitSet> map = new LinkedHashMap<>();
      chunks.forEach(chunk -> map.computeIfAbsent(of(chunk), r -> new BitSet(1024)).set(chunkToBit(chunk)));
      return map;
   }

   public static Set<ChunkPos> regionsToChunks(Map<RegionPos, BitSet> chunks) {
      Set<ChunkPos> outSet = new LinkedHashSet<>();
      chunks.entrySet().stream().flatMap(e -> e.getKey().toChunks(e.getValue()).stream()).forEach(outSet::add);
      return outSet;
   }
}
