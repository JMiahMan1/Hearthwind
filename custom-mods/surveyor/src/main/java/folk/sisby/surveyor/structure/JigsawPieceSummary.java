package folk.sisby.surveyor.structure;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.pools.FeaturePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.JigsawJunction;
import net.minecraft.world.level.levelgen.structure.pools.ListPoolElement;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElementType;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool.Projection;

public class JigsawPieceSummary extends StructurePieceSummary {
   public static final String KEY_POS = "pos";
   public static final String KEY_DELTA_Y = "deltaY";
   public static final String KEY_ROTATION = "rotation";
   public static final String KEY_JUNCTIONS = "junctions";
   public static final BiMap<String, StructurePoolElementType<?>> TYPE_KEYS = HashBiMap.create(
      Map.of("single", StructurePoolElementType.SINGLE, "feature", StructurePoolElementType.FEATURE)
   );
   protected final BlockPos pos;
   protected final int deltaY;
   protected final Rotation rotation;
   protected final List<JigsawJunction> junctions;
   protected final StructurePoolElementType<?> elementType;
   protected final Identifier id;

   public JigsawPieceSummary(
      BlockPos pos,
      int deltaY,
      Rotation rotation,
      StructurePoolElementType<?> elementType,
      Identifier id,
      int chainLength,
      BoundingBox boundingBox,
      List<JigsawJunction> junctions
   ) {
      super(StructurePieceType.JIGSAW, chainLength, boundingBox, new CompoundTag());
      this.pos = pos;
      this.deltaY = deltaY;
      this.rotation = rotation;
      this.elementType = elementType;
      this.id = id;
      this.junctions = junctions;
   }

   public JigsawPieceSummary(CompoundTag nbt) {
      super(nbt);
      this.pos = BlockPos.of((Long)nbt.getLong("pos").orElseThrow());
      this.deltaY = (Integer)nbt.getInt("deltaY").orElseThrow();
      this.rotation = Rotation.values()[nbt.getInt("rotation").orElseThrow()];
      this.junctions = new ArrayList<>();
      if (nbt.getIntArray("junctions").isPresent()) {
         int[] junctionArray = (int[])nbt.getIntArray("junctions").get();

         for (int i = 4; i <= junctionArray.length; i += 5) {
            this.junctions
               .add(
                  new JigsawJunction(
                     junctionArray[i - 4], junctionArray[i - 3], junctionArray[i - 2], junctionArray[i - 1], Projection.values()[junctionArray[i]]
                  )
               );
         }
      }

      String idKey = (String)TYPE_KEYS.keySet().stream().filter(nbt::contains).findFirst().orElseThrow();
      this.elementType = (StructurePoolElementType<?>)TYPE_KEYS.get(idKey);
      this.id = Identifier.parse((String)nbt.getString(idKey).orElseThrow());
   }

   public static List<StructurePieceSummary> tryFromElement(StructurePoolElement poolElement, PoolElementStructurePiece piece) {
      if (poolElement instanceof ListPoolElement listElement) {
         List<StructurePieceSummary> allSummaries = new ArrayList<>();
         listElement.elements.forEach(e -> allSummaries.addAll(tryFromElement(e, piece)));
         return allSummaries;
      } else if (poolElement instanceof SinglePoolElement singleElement && singleElement.template.left().isPresent()) {
         return List.of(
            new JigsawPieceSummary(
               piece.getPosition(),
               piece.getGroundLevelDelta(),
               piece.getRotation(),
               StructurePoolElementType.SINGLE,
               (Identifier)singleElement.template.left().orElseThrow(),
               piece.getGenDepth(),
               poolElement.getBoundingBox(piece.structureTemplateManager, piece.getPosition(), piece.getRotation()),
               piece.getJunctions()
            )
         );
      } else {
         return poolElement instanceof FeaturePoolElement featureElement && featureElement.feature.unwrapKey().isPresent()
            ? List.of(
               new JigsawPieceSummary(
                  piece.getPosition(),
                  piece.getGroundLevelDelta(),
                  piece.getRotation(),
                  StructurePoolElementType.FEATURE,
                  ((ResourceKey)featureElement.feature.unwrapKey().orElseThrow()).identifier(),
                  piece.getGenDepth(),
                  poolElement.getBoundingBox(piece.structureTemplateManager, piece.getPosition(), piece.getRotation()),
                  piece.getJunctions()
               )
            )
            : List.of();
      }
   }

   public static List<StructurePieceSummary> tryFromPiece(StructurePiece piece) {
      return piece instanceof PoolElementStructurePiece poolPiece ? tryFromElement(poolPiece.getElement(), poolPiece) : List.of();
   }

   @Override
   public void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag nbt) {
      super.addAdditionalSaveData(context, nbt);
      nbt.putLong("pos", this.pos.asLong());
      nbt.putInt("deltaY", this.deltaY);
      nbt.putInt("rotation", this.rotation.ordinal());
      String idKey = (String)TYPE_KEYS.inverse().get(this.elementType);
      nbt.putString(idKey, this.id.toString());
      if (!this.junctions.isEmpty()) {
         int[] junctionArray = new int[this.junctions.size() * 5];

         for (int i = 0; i < this.junctions.size(); i++) {
            JigsawJunction j = this.junctions.get(i);
            junctionArray[i * 5] = j.getSourceX();
            junctionArray[i * 5 + 1] = j.getSourceGroundY();
            junctionArray[i * 5 + 2] = j.getSourceZ();
            junctionArray[i * 5 + 3] = j.getDeltaY();
            junctionArray[i * 5 + 4] = j.getDestProjection().ordinal();
         }

         nbt.putIntArray("junctions", junctionArray);
      }
   }

   public BlockPos getPos() {
      return this.pos;
   }

   public int getDeltaY() {
      return this.deltaY;
   }

   public Rotation getRotation() {
      return this.rotation;
   }

   public StructurePoolElementType<?> getElementType() {
      return this.elementType;
   }

   public Identifier getId() {
      return this.id;
   }

   public List<JigsawJunction> getJunctions() {
      return this.junctions;
   }
}
