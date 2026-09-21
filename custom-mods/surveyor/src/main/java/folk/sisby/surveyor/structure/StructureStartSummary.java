package folk.sisby.surveyor.structure;

import net.minecraft.world.level.levelgen.structure.BoundingBox;

import java.util.List;

public class StructureStartSummary {
	protected final List<StructurePieceSummary> children;
	protected BoundingBox boundingBox;

	public StructureStartSummary(List<StructurePieceSummary> children) {
		this.children = children;
	}

	public BoundingBox getBoundingBox() {
		if (boundingBox == null) {
			boundingBox = children.stream().map(StructurePieceSummary::getBoundingBox).reduce(BoundingBox::encapsulating).orElse(null);
			if (boundingBox == null) return new BoundingBox(0, 0, 0, 0, 0, 0);
		}
		return boundingBox;
	}

	public List<StructurePieceSummary> getPieces() {
		return children;
	}
}
