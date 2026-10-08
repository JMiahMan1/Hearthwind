package folk.sisby.surveyor.terrain;

import folk.sisby.surveyor.mixin.AccessAbstractBlock;
import folk.sisby.surveyor.util.ChunkUtil;
import folk.sisby.surveyor.util.RegistryPalette;
import folk.sisby.surveyor.util.uints.UInts;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainerFactory;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.MapColor;
import org.jetbrains.annotations.Nullable;

public class ChunkSummary {
   public static final int MINIMUM_AIR_DEPTH = 2;
   public static final String KEY_AIR_COUNT = "air";
   public static final String KEY_LAYERS = "layers";
   protected final Integer airCount;
   protected final TreeMap<Integer, LayerSummary> layers = new TreeMap<>();

   public ChunkSummary(
      Level world, LevelChunk chunk, int[] layerHeights, RegistryPalette<Biome> biomePalette, RegistryPalette<Block> blockPalette, boolean countAir
   ) {
      this.airCount = countAir ? ChunkUtil.airCount(chunk) : null;
      LayerSummary.FloorSummary[][] layerFloors = new LayerSummary.FloorSummary[layerHeights.length - 1][256];
      LevelChunkSection[] rawSections = chunk.getSections();
      SectionSummary[] sections = new SectionSummary[rawSections.length];
      PalettedContainerFactory palettesFactory = world.palettedContainerFactory();

      for (int i = 0; i < rawSections.length; i++) {
         sections[i] = SectionSummary.ofSection(palettesFactory, rawSections[i]);
      }

      int chunkX = chunk.getPos().getMinBlockX();
      int chunkZ = chunk.getPos().getMinBlockZ();

      for (int x = 0; x < 16; x++) {
         for (int z = 0; z < 16; z++) {
            int walkspaceHeight = 2;
            int waterDepth = 0;
            Block carpetBlock = null;
            BlockPos carpetPos = new BlockPos(chunkX + x, Integer.MAX_VALUE, chunkZ + z);

            for (int layerIndex = 0; layerIndex < layerHeights.length - 1; layerIndex++) {
               LayerSummary.FloorSummary foundFloor = null;

               for (int y = layerHeights[layerIndex]; y > layerHeights[layerIndex + 1]; y--) {
                  int sectionIndex = chunk.getSectionIndex(y);
                  SectionSummary section = sections[sectionIndex];
                  if (section == null) {
                     int sectionBottom = SectionPos.sectionToBlockCoord(chunk.getSectionYFromSectionIndex(sectionIndex));
                     walkspaceHeight += y - sectionBottom + 1;
                     waterDepth = 0;
                     y = sectionBottom;
                  } else {
                     BlockPos pos = new BlockPos(chunkX + x, y, chunkZ + z);
                     BlockState state = section.getBlockState(palettesFactory, x, y, z);
                     Fluid fluid = state.getFluidState().getType();
                     if (!((AccessAbstractBlock)state.getBlock()).isCollidable() && fluid.isSame(Fluids.EMPTY)) {
                        walkspaceHeight++;
                        waterDepth = 0;
                        if (walkspaceHeight >= 2 && state.getMapColor(world, pos) != MapColor.NONE) {
                           carpetPos = pos;
                           carpetBlock = state.getBlock();
                        }
                     } else if (!fluid.isSame(Fluids.WATER) && !fluid.isSame(Fluids.FLOWING_WATER)) {
                        if (foundFloor == null) {
                           if (carpetPos.getY() == y + 1) {
                              foundFloor = new LayerSummary.FloorSummary(
                                 carpetPos.getY(),
                                 biomePalette.findOrAdd(
                                    (Biome)section.getBiomeEntry(palettesFactory, x, carpetPos.getY(), z, world.getMinY(), world.getMaxY()).value()
                                 ),
                                 blockPalette.findOrAdd(carpetBlock),
                                 world.getBrightness(LightLayer.BLOCK, carpetPos),
                                 waterDepth,
                                 waterDepth == 0 ? 0 : world.getBrightness(LightLayer.BLOCK, pos.above().above(waterDepth))
                              );
                              if (carpetPos.getY() > layerHeights[layerIndex]) {
                                 if (layerFloors[layerIndex - 1][x * 16 + z] == null) {
                                    layerFloors[layerIndex - 1][x * 16 + z] = foundFloor;
                                 }

                                 foundFloor = null;
                              }

                              walkspaceHeight = 0;
                              waterDepth = 0;
                           } else if (walkspaceHeight >= 2 && state.getMapColor(world, pos) != MapColor.NONE) {
                              int biome = biomePalette.findOrAdd(
                                 (Biome)section.getBiomeEntry(palettesFactory, x, y, z, world.getMinY(), world.getMaxY()).value()
                              );
                              int block = blockPalette.findOrAdd(state.getBlock());
                              foundFloor = new LayerSummary.FloorSummary(
                                 y,
                                 biome,
                                 block,
                                 world.getBrightness(LightLayer.BLOCK, pos.above()),
                                 waterDepth,
                                 waterDepth == 0 ? 0 : world.getBrightness(LightLayer.BLOCK, pos.above().above(waterDepth))
                              );
                           }
                        }

                        if (state.getMapColor(world, pos) != MapColor.NONE) {
                           walkspaceHeight = 0;
                           waterDepth = 0;
                        }
                     } else {
                        waterDepth++;
                     }
                  }
               }

               layerFloors[layerIndex][x * 16 + z] = foundFloor;
            }
         }
      }

      for (int i = 0; i < layerFloors.length; i++) {
         this.layers.put(layerHeights[i], LayerSummary.fromSummaries(layerFloors[i], layerHeights[i]));
      }
   }

   public ChunkSummary(CompoundTag nbt) {
      this.airCount = nbt.getInt("air").isPresent() ? (Integer)nbt.getInt("air").get() : null;
      CompoundTag layersCompound = nbt.getCompound("layers").orElse(new CompoundTag());

      for (String key : layersCompound.keySet()) {
         int layerY = Integer.parseInt(key);
         this.layers.put(layerY, LayerSummary.fromNbt((CompoundTag)layersCompound.getCompound(key).orElseThrow()));
      }
   }

   public ChunkSummary(FriendlyByteBuf buf) {
      this.layers.putAll(buf.readMap(FriendlyByteBuf::readVarInt, b -> b.readByte() == 0 ? null : LayerSummary.fromBuf(buf)));
      this.airCount = -1;
   }

   public CompoundTag writeNbt(CompoundTag nbt) {
      if (this.airCount != null) {
         nbt.putInt("air", this.airCount);
      }

      CompoundTag layersCompound = new CompoundTag();
      this.layers.forEach((layerY, layerSummary) -> {
         CompoundTag layerCompound = new CompoundTag();
         if (layerSummary != null) {
            layerSummary.writeNbt(layerCompound);
         }

         layersCompound.put(String.valueOf(layerY), layerCompound);
      });
      nbt.put("layers", layersCompound);
      return nbt;
   }

   public void writeBuf(FriendlyByteBuf buf) {
      buf.writeMap(this.layers, FriendlyByteBuf::writeVarInt, (b, summary) -> {
         if (summary == null) {
            b.writeByte(0);
         } else {
            b.writeByte(1);
            summary.writeBuf(buf);
         }
      });
   }

   public void remap(Map<Integer, Integer> biomeRemap, Map<Integer, Integer> blockRemap) {
      Map<Integer, LayerSummary> newLayers = new HashMap<>();
      this.layers
         .forEach(
            (y, layer) -> newLayers.put(
               y,
               layer == null
                  ? null
                  : new LayerSummary(
                     layer.found,
                     layer.depth,
                     UInts.remap(layer.biome, biomeRemap::get, 0, layer.found.cardinality()),
                     UInts.remap(layer.block, blockRemap::get, 0, layer.found.cardinality()),
                     layer.light,
                     layer.water,
                     layer.glint
                  )
            )
         );
      this.layers.clear();
      this.layers.putAll(newLayers);
   }

   public Integer getAirCount() {
      return this.airCount;
   }

   @Nullable
   public LayerSummary.Raw toSingleLayer(Integer minY, Integer maxY, int worldHeight) {
      LayerSummary.Raw outRaw = new LayerSummary.Raw(new BitSet(256), new int[256], new int[256], new int[256], new int[256], new int[256], new int[256]);
      this.layers.descendingMap().forEach((y, layer) -> {
         if (layer != null) {
            layer.fillEmptyFloors(worldHeight - y, maxY == null ? Integer.MIN_VALUE : y - maxY, minY == null ? Integer.MAX_VALUE : y - minY, outRaw);
         }
      });
      return outRaw.exists().cardinality() == 0 ? null : outRaw;
   }

   @Nullable
   public LayerSummary.Raw toSingleLayerBelow(Integer minY, int[] depthsAbove, int worldHeight) {
      LayerSummary.Raw outRaw = new LayerSummary.Raw(new BitSet(256), new int[256], new int[256], new int[256], new int[256], new int[256], new int[256]);
      this.layers.descendingMap().forEach((y, layer) -> {
         if (layer != null) {
            layer.fillEmptyFloorsUnder(worldHeight - y, depthsAbove, minY == null ? Integer.MAX_VALUE : y - minY, outRaw);
         }
      });
      return outRaw.exists().cardinality() == 0 ? null : outRaw;
   }
}
