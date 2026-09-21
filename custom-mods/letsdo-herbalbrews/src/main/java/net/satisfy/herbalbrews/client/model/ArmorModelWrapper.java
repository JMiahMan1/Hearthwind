package net.satisfy.herbalbrews.client.model;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;

// 26.2: armor models submit through the submit path like everything else.
public class ArmorModelWrapper extends Model<HumanoidRenderState> {
    public ArmorModelWrapper(ModelPart part) {
        super(part, id -> RenderTypes.entityCutout(id));
    }

    @Override
    public void setupAnim(HumanoidRenderState state) {
    }
}
