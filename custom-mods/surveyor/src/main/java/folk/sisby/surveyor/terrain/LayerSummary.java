package folk.sisby.surveyor.terrain;

import folk.sisby.surveyor.util.uints.UInts;
import java.util.BitSet;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LayerSummary {
   public static final String KEY_FOUND = "found";
   public static final String KEY_DEPTH = "depth";
   public static final String KEY_BIOME = "biome";
   public static final String KEY_BLOCK = "block";
   public static final String KEY_LIGHT = "light";
   public static final String KEY_WATER = "water";
   public static final String KEY_GLINT = "glint";
   public static final int DEPTH_DEFAULT = 0;
   public static final int BIOME_DEFAULT = 0;
   public static final int BLOCK_DEFAULT = 0;
   public static final int LIGHT_DEFAULT = 0;
   public static final int WATER_DEFAULT = 0;
   public static final int GLINT_DEFAULT = 0;
   @NotNull
   protected final BitSet found;
   @Nullable
   protected final UInts depth;
   @Nullable
   protected final UInts biome;
   @Nullable
   protected final UInts block;
   @Nullable
   protected final UInts light;
   @Nullable
   protected final UInts water;
   @Nullable
   protected final UInts glint;

   protected LayerSummary(
      @NotNull BitSet found,
      @Nullable UInts depth,
      @Nullable UInts biome,
      @Nullable UInts block,
      @Nullable UInts light,
      @Nullable UInts water,
      @Nullable UInts glint
   ) {
      this.found = found;
      this.depth = depth;
      this.biome = biome;
      this.block = block;
      this.light = light;
      this.water = water;
      this.glint = glint;
   }

   public static LayerSummary fromSummaries(LayerSummary.FloorSummary[] floorSummaries, int layerY) {
      BitSet found = new BitSet(256);

      for (int i = 0; i < floorSummaries.length; i++) {
         found.set(i, floorSummaries[i] != null);
      }

      int cardinality = found.cardinality();
      if (cardinality == 0) {
         return null;
      } else {
         int[] depth = new int[cardinality];
         int[] biome = new int[cardinality];
         int[] block = new int[cardinality];
         int[] light = new int[cardinality];
         int[] water = new int[cardinality];
         int[] glint = new int[cardinality];
         int c = 0;

         for (int i = 0; i < floorSummaries.length; i++) {
            if (found.get(i)) {
               LayerSummary.FloorSummary summary = floorSummaries[i];
               depth[c] = layerY - summary.y;
               biome[c] = summary.biome;
               block[c] = summary.block;
               light[c] = summary.lightLevel;
               water[c] = summary.fluidDepth;
               glint[c] = summary.waterLight;
               c++;
            }
         }

         return new LayerSummary(
            found,
            UInts.fromUInts(depth, 0),
            UInts.fromUInts(biome, 0),
            UInts.fromUInts(block, 0),
            UInts.fromUInts(light, 0),
            UInts.fromUInts(water, 0),
            UInts.fromUInts(glint, 0)
         );
      }
   }

   public static LayerSummary fromNbt(CompoundTag nbt) {
      if (nbt.getLongArray("found").isEmpty()) {
         return null;
      } else {
         BitSet found = BitSet.valueOf((long[])nbt.getLongArray("found").get());
         int cardinality = found.cardinality();
         UInts depth = UInts.readNbt(nbt.get("depth"), cardinality);
         UInts biome = UInts.readNbt(nbt.get("biome"), cardinality);
         UInts block = UInts.readNbt(nbt.get("block"), cardinality);
         UInts light = UInts.readNbt(nbt.get("light"), cardinality);
         UInts water = UInts.readNbt(nbt.get("water"), cardinality);
         UInts glint = UInts.readNbt(nbt.get("glint"), cardinality);
         return new LayerSummary(found, depth, biome, block, light, water, glint);
      }
   }

   public static LayerSummary fromBuf(FriendlyByteBuf buf) {
      BitSet found = buf.readFixedBitSet(256);
      int cardinality = found.cardinality();
      return new LayerSummary(
         found,
         UInts.readBuf(buf, cardinality),
         UInts.readBuf(buf, cardinality),
         UInts.readBuf(buf, cardinality),
         UInts.readBuf(buf, cardinality),
         UInts.readBuf(buf, cardinality),
         UInts.readBuf(buf, cardinality)
      );
   }

   public CompoundTag writeNbt(CompoundTag nbt) {
      nbt.putLongArray("found", this.found.toLongArray());
      if (this.depth != null) {
         this.depth.writeNbt(nbt, "depth");
      }

      if (this.biome != null) {
         this.biome.writeNbt(nbt, "biome");
      }

      if (this.block != null) {
         this.block.writeNbt(nbt, "block");
      }

      if (this.light != null) {
         this.light.writeNbt(nbt, "light");
      }

      if (this.water != null) {
         this.water.writeNbt(nbt, "water");
      }

      if (this.glint != null) {
         this.glint.writeNbt(nbt, "glint");
      }

      return nbt;
   }

   public void writeBuf(FriendlyByteBuf buf) {
      buf.writeFixedBitSet(this.found, 256);
      UInts.writeBuf(this.depth, buf);
      UInts.writeBuf(this.biome, buf);
      UInts.writeBuf(this.block, buf);
      UInts.writeBuf(this.light, buf);
      UInts.writeBuf(this.water, buf);
      UInts.writeBuf(this.glint, buf);
   }

   public void fillEmptyFloors(int depthOffset, int minDepth, int maxDepth, LayerSummary.Raw outLayer) {
      int i = 0;

      for (int j = 0; j < 256; j++) {
         if (this.found.get(j)) {
            int floorDepth = this.depth == null ? 0 : this.depth.get(i);
            if (!outLayer.exists.get(j) && floorDepth >= minDepth && floorDepth <= maxDepth) {
               outLayer.exists.set(j);
               outLayer.depths[j] = floorDepth + depthOffset;
               outLayer.biomes[j] = this.biome == null ? 0 : this.biome.get(i);
               outLayer.blocks[j] = this.block == null ? 0 : this.block.get(i);
               outLayer.lightLevels[j] = this.light == null ? 0 : this.light.get(i);
               outLayer.waterDepths[j] = this.water == null ? 0 : this.water.get(i);
               outLayer.waterLights[j] = this.glint == null ? 0 : this.glint.get(i);
            }

            i++;
         }
      }
   }

   public void fillEmptyFloorsUnder(int depthOffset, int[] minRawDepth, int maxDepth, LayerSummary.Raw outLayer) {
      int i = 0;

      for (int j = 0; j < 256; j++) {
         if (this.found.get(j)) {
            int floorDepth = this.depth == null ? 0 : this.depth.get(i);
            if (!outLayer.exists.get(j) && floorDepth > minRawDepth[j] - depthOffset && floorDepth <= maxDepth) {
               outLayer.exists.set(j);
               outLayer.depths[j] = floorDepth + depthOffset;
               outLayer.biomes[j] = this.biome == null ? 0 : this.biome.get(i);
               outLayer.blocks[j] = this.block == null ? 0 : this.block.get(i);
               outLayer.lightLevels[j] = this.light == null ? 0 : this.light.get(i);
               outLayer.waterDepths[j] = this.water == null ? 0 : this.water.get(i);
               outLayer.waterLights[j] = this.glint == null ? 0 : this.glint.get(i);
            }

            i++;
         }
      }
   }

   public record FloorSummary(int y, int biome, int block, int lightLevel, int fluidDepth, int waterLight) {
   }

   public record Raw(BitSet exists, int[] depths, int[] biomes, int[] blocks, int[] lightLevels, int[] waterDepths, int[] waterLights) {
   }
}
