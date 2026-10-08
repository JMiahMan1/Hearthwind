package folk.sisby.surveyor.structure;

import folk.sisby.surveyor.util.ListTags;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Multimap;
import com.google.common.collect.Table;
import com.google.common.collect.Tables;
import folk.sisby.surveyor.Surveyor;
import folk.sisby.surveyor.config.SystemMode;
import folk.sisby.surveyor.util.MapUtil;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;

public class RegionStructureSummary {
   public static final String KEY_STRUCTURES = "structures";
   public static final String KEY_STARTS = "starts";
   public static final String KEY_PIECES = "pieces";
   protected final Table<ResourceKey<Structure>, ChunkPos, StructureStartSummary> starts = Tables.synchronizedTable(HashBasedTable.create());
   protected boolean dirty = false;

   RegionStructureSummary() {
   }

   RegionStructureSummary(Table<ResourceKey<Structure>, ChunkPos, StructureStartSummary> starts) {
      this.starts.putAll(starts);
   }

   protected static StructureStartSummary summarisePieces(StructurePieceSerializationContext context, StructureStart start) {
      List<StructurePieceSummary> pieces = new ArrayList<>();

      for (StructurePiece piece : start.getPieces()) {
         if (piece.getType().equals(StructurePieceType.JIGSAW)) {
            pieces.addAll(JigsawPieceSummary.tryFromPiece(piece));
         } else {
            pieces.add(StructurePieceSummary.fromPiece(context, piece, start.getPieces().size() <= 10));
         }
      }

      return new StructureStartSummary(pieces);
   }

   public static StructurePieceSummary readStructurePieceNbt(CompoundTag nbt) {
      return (StructurePieceSummary)(nbt.getString("id").equals(BuiltInRegistries.STRUCTURE_PIECE.getKey(StructurePieceType.JIGSAW).toString())
         ? new JigsawPieceSummary(nbt)
         : new StructurePieceSummary(nbt));
   }

   protected static RegionStructureSummary readNbt(CompoundTag nbt) {
      Table<ResourceKey<Structure>, ChunkPos, StructureStartSummary> starts = HashBasedTable.create();
      CompoundTag structuresCompound = nbt.getCompound("structures").orElse(new CompoundTag());

      for (String structureId : structuresCompound.keySet()) {
         ResourceKey<Structure> key = ResourceKey.create(Registries.STRUCTURE, Identifier.parse(structureId));
         CompoundTag structureCompound = structuresCompound.getCompound(structureId).orElse(new CompoundTag());
         CompoundTag startsCompound = structureCompound.getCompound("starts").orElse(new CompoundTag());

         for (String posKey : startsCompound.keySet()) {
            int x = Integer.parseInt(posKey.split(",")[0]);
            int z = Integer.parseInt(posKey.split(",")[1]);
            CompoundTag startCompound = startsCompound.getCompound(posKey).orElse(new CompoundTag());
            List<StructurePieceSummary> pieces = new ArrayList<>();

            for (Tag pieceElement : startCompound.getList("pieces").orElse(new ListTag())) {
               pieces.add(readStructurePieceNbt((CompoundTag)pieceElement));
            }

            starts.put(key, new ChunkPos(x, z), new StructureStartSummary(pieces));
         }
      }

      return new RegionStructureSummary(starts);
   }

   public boolean contains(Level world, StructureStart start) {
      ResourceKey<Structure> key = (ResourceKey<Structure>)world.registryAccess()
         .lookupOrThrow(Registries.STRUCTURE)
         .getResourceKey(start.getStructure())
         .orElse(null);
      if (key == null) {
         Surveyor.LOGGER.error("Encountered an unregistered structure! {} | {}", start, start.getStructure());
         return true;
      } else {
         return this.contains(key, start.getChunkPos());
      }
   }

   public boolean contains(ResourceKey<Structure> key, ChunkPos pos) {
      return this.starts.contains(key, pos);
   }

   public StructureStartSummary get(ResourceKey<Structure> key, ChunkPos pos) {
      return (StructureStartSummary)this.starts.get(key, pos);
   }

   public Multimap<ResourceKey<Structure>, ChunkPos> keySet() {
      return MapUtil.keyMultiMap(this.starts);
   }

   public void put(ServerLevel world, StructureStart start) {
      ResourceKey<Structure> key = (ResourceKey<Structure>)world.registryAccess()
         .lookupOrThrow(Registries.STRUCTURE)
         .getResourceKey(start.getStructure())
         .orElseThrow();
      StructureStartSummary summary = summarisePieces(StructurePieceSerializationContext.fromLevel(world), start);
      this.put(key, start.getChunkPos(), summary);
   }

   public void put(ResourceKey<Structure> key, ChunkPos pos, StructureStartSummary summary) {
      this.starts.put(key, pos, summary);
      this.dirty();
   }

   protected CompoundTag writeNbt(CompoundTag nbt) {
      CompoundTag structuresCompound = new CompoundTag();
      this.starts.rowMap().forEach((key, inner) -> {
         CompoundTag structureCompound = new CompoundTag();
         CompoundTag startsCompound = new CompoundTag();
         inner.forEach((pos, summary) -> {
            ListTag pieceList = ListTags.of(summary.getChildren().stream().map(p -> (Tag) p.toNbt()).toList());
            CompoundTag startCompound = new CompoundTag();
            startCompound.put("pieces", pieceList);
            startsCompound.put("%s,%s".formatted(pos.x(), pos.z()), startCompound);
         });
         structureCompound.put("starts", startsCompound);
         structuresCompound.put(key.identifier().toString(), structureCompound);
      });
      nbt.put("structures", structuresCompound);
      return nbt;
   }

   public boolean isDirty() {
      return this.dirty && Surveyor.CONFIG.structures != SystemMode.FROZEN;
   }

   private void dirty() {
      this.dirty = true;
   }
}
