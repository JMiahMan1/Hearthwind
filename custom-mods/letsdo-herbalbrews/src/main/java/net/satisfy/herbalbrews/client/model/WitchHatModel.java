package net.satisfy.herbalbrews.client.model;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.satisfy.herbalbrews.core.util.HerbalBrewsIdentifier;

public class WitchHatModel extends Model<HumanoidRenderState> implements HatModel {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(HerbalBrewsIdentifier.identifier("witch_hat"), "main");
    private final ModelPart root;
    private final ModelPart witchHat;

    public WitchHatModel(ModelPart root) {
        super(root, id -> RenderTypes.entityCutout(id));
        this.root = root;
        this.witchHat = root.getChild("witchHat");
    }

    @SuppressWarnings("unused")
    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition witchHat = partdefinition.addOrReplaceChild("witchHat", CubeListBuilder.create()
                .texOffs(-16, 32).addBox(-8.0F, -7.0F, -8.0F, 16.0F, 0.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(0, 20).addBox(-4.0F, -11.0F, -4.0F, 8.0F, 4.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(0, 8).addBox(-3.0F, -16.0F, -3.0F, 6.0F, 6.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(24, 20).addBox(-2.0F, -19.0F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(18, 9).addBox(-1.0F, -21.0F, -1.0F, 2.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -6.0F, 0.0F));

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
        copyPart(witchHat, model);
    }
}
