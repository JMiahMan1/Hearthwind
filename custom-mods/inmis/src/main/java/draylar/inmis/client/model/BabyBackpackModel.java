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
 * The smaller 3D backpack worn for the baby tier, ported from Inmis Addon
 * ({@code net.inmisaddon.model.BabyBackpackModel}, MIT, Jack Bagel).
 *
 * <p>Same 26.2 retyping as {@link BackpackModel}: the geometry below is the
 * addon's, with its {@code class_5603} pose builder replaced by
 * {@link PartPose} and its {@code class_5605} box deformation by
 * {@link CubeDeformation}.
 */
@Environment(EnvType.CLIENT)
public class BabyBackpackModel extends EntityModel<EntityRenderState> {
    private final ModelPart base;

    public BabyBackpackModel(ModelPart rootPart) {
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
                        .texOffs(0, 0)
                        .addBox(-1.0F, -4.0F, -4.0F, 3.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 0)
                        .mirror()
                        .addBox(-1.0F, -4.0F, 3.0F, 3.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                        .mirror(false)
                        .texOffs(54, 12)
                        .addBox(2.0F, -9.0F, -2.0F, 1.0F, 3.0F, 4.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 14)
                        .addBox(2.0F, -4.0F, -3.0F, 1.0F, 4.0F, 6.0F, new CubeDeformation(0.0F))
                        .texOffs(28, 8)
                        .mirror()
                        .addBox(-1.0F, -10.0F, -3.0F, 3.0F, 10.0F, 6.0F, new CubeDeformation(0.01F))
                        .mirror(false),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -1.5708F, 0.0F));

        return LayerDefinition.create(mesh, 64, 64);
    }
}
