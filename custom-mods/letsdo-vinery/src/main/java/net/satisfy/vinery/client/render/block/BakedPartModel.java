package net.satisfy.vinery.client.render.block;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;

// 26.2: block-entity renderers submit baked ModelParts through the Model path
// (submitModel). This wrapper exposes a single part as a Model with identical
// geometry; pose transforms stay on the PoseStack exactly as before.
public class BakedPartModel extends Model<BlockEntityRenderState> {
    public BakedPartModel(ModelPart part) {
        super(part, id -> RenderTypes.entityCutout(id));
    }

    @Override
    public void setupAnim(BlockEntityRenderState state) {
    }
}
