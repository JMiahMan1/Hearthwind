package net.satisfy.herbalbrews.client.model;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.satisfy.herbalbrews.core.util.HerbalBrewsIdentifier;

public class TopHatModel extends Model<HumanoidRenderState> implements HatModel {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(HerbalBrewsIdentifier.identifier("top_hat"), "main");
    private final ModelPart root;
    private final ModelPart topHat;

    public TopHatModel(ModelPart root) {
        super(root, id -> RenderTypes.entityCutout(id));
        this.root = root;
        this.topHat = root.getChild("topHat");
    }

    @SuppressWarnings("unused")
    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition topHat = partdefinition.addOrReplaceChild("topHat",
                CubeListBuilder.create()
                        .texOffs(-14, 16).addBox(-7.0F, -6.0F, -7.0F, 14.0F, 0.0F, 14.0F, new CubeDeformation(0.0F)) // Verschiebe um -6 Pixel
                        .texOffs(0, 0).addBox(-4.0F, -14.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)), // Verschiebe um -6 Pixel
                PartPose.offset(0.0F, 0.0F, 0.0F)
        );

        return LayerDefinition.create(meshdefinition, 48, 48);
    }

    public ArmorModelWrapper asModel() {
        return new ArmorModelWrapper(this.root);
    }

    @Override
    public void setupAnim(HumanoidRenderState state) {

    }

    protected static void copyPart(ModelPart dst, ModelPart src) {
        dst.x = src.x;
        dst.y = src.y;
        dst.z = src.z;
        dst.xRot = src.xRot;
        dst.yRot = src.yRot;
        dst.zRot = src.zRot;
    }

    @Override
    public void copyHead(ModelPart model) {
        copyPart(topHat, model);
    }
}
