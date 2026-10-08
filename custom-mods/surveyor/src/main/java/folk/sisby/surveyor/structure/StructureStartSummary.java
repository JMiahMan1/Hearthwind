package folk.sisby.surveyor.structure;

import java.util.List;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;

public class StructureStartSummary {
   protected final List<StructurePieceSummary> children;
   protected BoundingBox boundingBox;

   public StructureStartSummary(List<StructurePieceSummary> children) {
      this.children = children;
   }

   public BoundingBox getBoundingBox() {
      if (this.boundingBox == null) {
         this.boundingBox = (BoundingBox)BoundingBox.encapsulatingBoxes(this.children.stream().map(StructurePiece::getBoundingBox)::iterator).orElse(null);
         if (this.boundingBox == null) {
            return (BoundingBox)(new BoundingBox(0, 0, 0, 0, 0, 0));
         }
      }

      return (BoundingBox)this.boundingBox;
   }

   public List<StructurePieceSummary> getChildren() {
      return this.children;
   }
}
