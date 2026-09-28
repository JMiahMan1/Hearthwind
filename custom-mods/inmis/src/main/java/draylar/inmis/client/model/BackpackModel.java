package draylar.inmis.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.EntityRenderState;

/**
 * The 3D backpack worn on the player's back, ported from Inmis Addon
 * ({@code net.inmisaddon.model.BackpackModel}, MIT, Jack Bagel) to the 26.2
 * entity model API.
 *
 * <p>26.2 replaced the old {@code FeatureRenderer#render} + {@code
 * VertexConsumerProvider#getTexture} + {@code ModelPart#render} world with
 * submit-node rendering, so the addon could not be copied verbatim: the model
 * is now an {@link EntityModel} submitted through
 * {@code SubmitNodeCollector#submitModel}. The geometry, the 64x64 texture
 * layout and the per-part poses are unchanged.
 */
@Environment(EnvType.CLIENT)
public class BackpackModel extends EntityModel<EntityRenderState> {
    /** The single part carrying every cube; kept so callers can re-pose it. */
    private final ModelPart base;

    public BackpackModel(ModelPart rootPart) {
        super(rootPart);
        this.base = rootPart.getChild("base");
    }

    public ModelPart base() {
        return this.base;
    }

    public static LayerDefinition texturedModelData() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition base = root.addOrReplaceChild(
                "base", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

        base.addOrReplaceChild("cube_r1",
                CubeListBuilder.create()
                        .texOffs(50, 0)
                        .addBox(-5.0F, -5.0F, -4.0F, 5.0F, 10.0F, 2.0F, new CubeDeformation(0.0F))
                        .mirror(false),
                PartPose.offsetAndRotation(-1.0F, -5.0F, -1.0F, -3.1416F, -1.3963F, 3.1416F));

        base.addOrReplaceChild("cube_r2",
                CubeListBuilder.create()
                        .texOffs(50, 0)
                        .addBox(-7.0F, -5.0F, 0.0F, 5.0F, 10.0F, 2.0F, new CubeDeformation(0.0F))
                        .mirror(false),
                PartPose.offsetAndRotation(-1.0F, -5.0F, 1.0F, 0.0F, -1.3963F, 0.0F));

        base.addOrReplaceChild("cube_r3",
                CubeListBuilder.create()
                        .texOffs(38, 0)
                        .addBox(0.0F, -5.0F, 3.0F, 5.0F, 5.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(38, 0)
                        .addBox(0.0F, -5.0F, -2.0F, 5.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(1.0F, -11.0F, -2.5F, -1.5708F, -0.7854F, 1.5708F));

        base.addOrReplaceChild("cube_r4",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-1.0F, -4.0F, -5.0F, 4.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                        .mirror()
                        .addBox(-1.0F, -4.0F, 4.0F, 4.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                        .mirror(false)
                        .texOffs(54, 12)
                        .addBox(3.0F, -9.0F, -2.0F, 1.0F, 3.0F, 4.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 14)
                        .addBox(3.0F, -5.0F, -3.0F, 2.0F, 5.0F, 6.0F, new CubeDeformation(0.0F))
                        .texOffs(25, 6)
                        .mirror()
                        .addBox(-1.0F, -10.0F, -4.0F, 4.0F, 10.0F, 8.0F, new CubeDeformation(0.01F))
                        .mirror(false),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -1.5708F, 0.0F));

        base.addOrReplaceChild("cube_r5",
                CubeListBuilder.create()
                        .texOffs(1, 0)
                        .addBox(-3.75F, -0.25F, -4.0F, 4.0F, 4.0F, 10.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(1.0F, -11.0F, 3.5F, -1.5708F, -0.7854F, 1.5708F));

        return LayerDefinition.create(mesh, 64, 64);
    }
}
