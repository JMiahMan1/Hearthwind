package net.satisfy.vinery.client.render.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.satisfy.vinery.core.Vinery;
import net.satisfy.vinery.core.block.LatticeBlock;
import net.satisfy.vinery.core.block.entity.LatticeBlockEntity;
import net.satisfy.vinery.core.registry.GrapeTypeRegistry;
import net.satisfy.vinery.core.util.GeneralUtil;
import net.satisfy.vinery.core.util.GrapeType;

import static net.satisfy.vinery.core.registry.ObjectRegistry.*;

import java.util.HashMap;
import java.util.Map;

// 26.2: same poses, growth-stage parts and texture as before; parts submit
// through BakedPartModel wrappers.
public class LatticeRenderer implements BlockEntityRenderer<LatticeBlockEntity, LatticeRenderer.State> {
    private static Map<Block, Identifier> textureMap;

    private static Map<Block, Identifier> getTextureMap() {
        if (textureMap == null) {
            textureMap = new HashMap<>();
            textureMap.put(OAK_LATTICE.get(), Vinery.identifier("textures/block/lattice/oak_lattice.png"));
            textureMap.put(SPRUCE_LATTICE.get(), Vinery.identifier("textures/block/lattice/spruce_lattice.png"));
            textureMap.put(CHERRY_LATTICE.get(), Vinery.identifier("textures/block/lattice/cherry_lattice.png"));
            textureMap.put(BIRCH_LATTICE.get(), Vinery.identifier("textures/block/lattice/birch_lattice.png"));
            textureMap.put(DARK_OAK_LATTICE.get(), Vinery.identifier("textures/block/lattice/dark_oak_lattice.png"));
            textureMap.put(ACACIA_LATTICE.get(), Vinery.identifier("textures/block/lattice/acacia_lattice.png"));
            textureMap.put(BAMBOO_LATTICE.get(), Vinery.identifier("textures/block/lattice/bamboo_lattice.png"));
            textureMap.put(JUNGLE_LATTICE.get(), Vinery.identifier("textures/block/lattice/jungle_lattice.png"));
            textureMap.put(MANGROVE_LATTICE.get(), Vinery.identifier("textures/block/lattice/mangrove_lattice.png"));
            textureMap.put(DARK_CHERRY_LATTICE.get(), Vinery.identifier("textures/block/lattice/dark_cherry_lattice.png"));
        }
        return textureMap;
    }

    private final ModelPart growing_red;
    private final ModelPart sprout;
    private final ModelPart growing_white;
    private final ModelPart mesh;
    private final ModelPart support_right;
    private final ModelPart corner_braces_right;
    private final ModelPart support_left;
    private final ModelPart corner_braces_left;
    private final ModelPart growing_red_floor;
    private final ModelPart sprout_floor;
    private final ModelPart growing_white_floor;
    private final ModelPart lattice_parts;
    private final ModelPart hanging_1_r1;
    private final ModelPart hanging_2_r1;

    private final BakedPartModel growing_redModel;
    private final BakedPartModel sproutModel;
    private final BakedPartModel growing_whiteModel;
    private final BakedPartModel meshModel;
    private final BakedPartModel support_rightModel;
    private final BakedPartModel corner_braces_rightModel;
    private final BakedPartModel support_leftModel;
    private final BakedPartModel corner_braces_leftModel;
    private final BakedPartModel growing_red_floorModel;
    private final BakedPartModel sprout_floorModel;
    private final BakedPartModel growing_white_floorModel;
    private final BakedPartModel lattice_partsModel;
    private final BakedPartModel hanging_1_r1Model;
    private final BakedPartModel hanging_2_r1Model;

    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Vinery.identifier("lattice"), "main");

    public LatticeRenderer(BlockEntityRendererProvider.Context context) {
        ModelPart root = context.bakeLayer(LAYER_LOCATION);
        ModelPart lattice_wall = root.getChild("lattice_wall");
        ModelPart grape_cluster = lattice_wall.getChild("grape_cluster");
        this.growing_red = grape_cluster.getChild("growing_red");
        this.sprout = grape_cluster.getChild("sprout");
        this.growing_white = grape_cluster.getChild("growing_white");
        this.mesh = lattice_wall.getChild("mesh");
        this.support_right = lattice_wall.getChild("support_right");
        this.corner_braces_right = lattice_wall.getChild("corner_braces_right");
        this.support_left = lattice_wall.getChild("support_left");
        this.corner_braces_left = lattice_wall.getChild("corner_braces_left");
        ModelPart lattice_floor = root.getChild("lattice_floor");
        ModelPart grape_cluster_floor = lattice_floor.getChild("grape_cluster_floor");
        this.growing_red_floor = grape_cluster_floor.getChild("growing_red_floor");
        this.sprout_floor = grape_cluster_floor.getChild("sprout_floor");
        this.growing_white_floor = grape_cluster_floor.getChild("growing_white_floor");
        this.lattice_parts = lattice_floor.getChild("lattice_parts");
        this.hanging_1_r1 = grape_cluster_floor.getChild("hanging_1_r1");
        this.hanging_2_r1 = grape_cluster_floor.getChild("hanging_2_r1");
        this.growing_redModel = new BakedPartModel(this.growing_red);
        this.sproutModel = new BakedPartModel(this.sprout);
        this.growing_whiteModel = new BakedPartModel(this.growing_white);
        this.meshModel = new BakedPartModel(this.mesh);
        this.support_rightModel = new BakedPartModel(this.support_right);
        this.corner_braces_rightModel = new BakedPartModel(this.corner_braces_right);
        this.support_leftModel = new BakedPartModel(this.support_left);
        this.corner_braces_leftModel = new BakedPartModel(this.corner_braces_left);
        this.growing_red_floorModel = new BakedPartModel(this.growing_red_floor);
        this.sprout_floorModel = new BakedPartModel(this.sprout_floor);
        this.growing_white_floorModel = new BakedPartModel(this.growing_white_floor);
        this.lattice_partsModel = new BakedPartModel(this.lattice_parts);
        this.hanging_1_r1Model = new BakedPartModel(this.hanging_1_r1);
        this.hanging_2_r1Model = new BakedPartModel(this.hanging_2_r1);
    }

    public static class State extends BlockEntityRenderState {
        public Direction facing = Direction.NORTH;
        public boolean support;
        public boolean bottom;
        public GeneralUtil.LineConnectingType type = GeneralUtil.LineConnectingType.MIDDLE;
        public int age;
        public boolean hasGrapes;
        public boolean red;
        public boolean showHanging;
        public long seed;
        public Identifier texture = Vinery.identifier("textures/entity/lattice/default_lattice.png");
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(LatticeBlockEntity blockEntity, State state, float partialTick, Vec3 cameraPos,
            ModelFeatureRenderer.CrumblingOverlay crumbling) {
        BlockEntityRenderState.extractBase(blockEntity, state, crumbling);
        BlockState blockState = blockEntity.getBlockState();
        state.facing = blockState.getValue(LatticeBlock.FACING);
        state.support = blockState.getValue(LatticeBlock.SUPPORT);
        state.bottom = blockState.getValue(LatticeBlock.BOTTOM);
        state.type = blockState.getValue(LatticeBlock.TYPE);
        state.age = blockState.getValue(LatticeBlock.AGE);
        GrapeType grapeType = blockState.getValue(LatticeBlock.GRAPE);
        state.hasGrapes = !grapeType.equals(GrapeTypeRegistry.NONE);
        state.red = grapeType.isRed();
        state.showHanging = blockEntity.shouldShowHanging();
        state.seed = blockEntity.getBlockPos().asLong();
        state.texture = getTextureMap().getOrDefault(blockState.getBlock(), Vinery.identifier("textures/entity/lattice/default_lattice.png"));
    }

    private void submitPart(BakedPartModel model, State state, PoseStack poseStack, SubmitNodeCollector collector) {
        collector.submitModel(model, state, poseStack, RenderTypes.entityCutout(state.texture),
                state.lightCoords, OverlayTexture.NO_OVERLAY, EntityRenderState.NO_OUTLINE, state.breakProgress);
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraState) {
        poseStack.pushPose();
        poseStack.translate(0.5, 0, 0.5);
        poseStack.mulPose(Axis.YP.rotationDegrees(-state.facing.toYRot()));
        poseStack.scale(1.0f, -1.0f, -1.0f);
        if (state.bottom) {
            poseStack.mulPose(Axis.ZP.rotationDegrees(180f));
            poseStack.scale(1.0f, -1.0f, -1.0f);
            submitPart(lattice_partsModel, state, poseStack, collector);
            if (state.hasGrapes) {
                if (state.age < 4) {
                    submitPart(sprout_floorModel, state, poseStack, collector);
                } else if (state.red) {
                    submitPart(growing_red_floorModel, state, poseStack, collector);
                } else {
                    submitPart(growing_white_floorModel, state, poseStack, collector);
                }
            }
        } else {
            submitPart(meshModel, state, poseStack, collector);
            if (state.type != GeneralUtil.LineConnectingType.MIDDLE && state.type != GeneralUtil.LineConnectingType.LEFT) {
                submitPart(support_rightModel, state, poseStack, collector);
                if (state.support) {
                    submitPart(corner_braces_rightModel, state, poseStack, collector);
                }
            }
            if (state.type != GeneralUtil.LineConnectingType.MIDDLE && state.type != GeneralUtil.LineConnectingType.RIGHT) {
                submitPart(support_leftModel, state, poseStack, collector);
                if (state.support) {
                    submitPart(corner_braces_leftModel, state, poseStack, collector);
                }
            }
        }
        if (state.hasGrapes) {
            if (state.bottom) {
                if (state.age < 4) {
                    submitPart(sprout_floorModel, state, poseStack, collector);
                } else if (state.red) {
                    submitPart(growing_red_floorModel, state, poseStack, collector);
                } else {
                    submitPart(growing_white_floorModel, state, poseStack, collector);
                }
            } else {
                if (state.age < 4) {
                    submitPart(sproutModel, state, poseStack, collector);
                } else if (state.red) {
                    submitPart(growing_redModel, state, poseStack, collector);
                } else {
                    submitPart(growing_whiteModel, state, poseStack, collector);
                }
            }
        }
        if (state.bottom && state.showHanging) {
            poseStack.pushPose();
            poseStack.translate(0.0, 0, 0.0);
            poseStack.mulPose(Axis.YP.rotationDegrees(-state.facing.toYRot()));
            poseStack.scale(1.0f, -1.0f, -1.0f);
            poseStack.mulPose(Axis.ZP.rotationDegrees(180f));
            RandomSource random = RandomSource.create(state.seed);
            float offsetX = Mth.lerp(random.nextFloat(), -0.02f, 0.0f);
            float offsetZ = Mth.lerp(random.nextFloat(), -0.02f, 0.0f);
            poseStack.translate(offsetX, -0.2f, offsetZ);
            submitPart(hanging_1_r1Model, state, poseStack, collector);
            submitPart(hanging_2_r1Model, state, poseStack, collector);
            poseStack.popPose();
        }
        poseStack.popPose();
    }

    @SuppressWarnings("unused")
    public static LayerDefinition getTexturedModelData() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition lattice_wall = partdefinition.addOrReplaceChild("lattice_wall", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition grape_cluster = lattice_wall.addOrReplaceChild("grape_cluster", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, -10.0F));

        PartDefinition growing_red = grape_cluster.addOrReplaceChild("growing_red", CubeListBuilder.create().texOffs(46, 17).addBox(-15.0F, -16.0F, -10.5F, 16.0F, 16.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(7.0F, 0.0F, 17.0F));

        PartDefinition sprout = grape_cluster.addOrReplaceChild("sprout", CubeListBuilder.create().texOffs(46, 0).addBox(-15.0F, -16.0F, -10.5F, 16.0F, 16.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(7.0F, 0.0F, 17.0F));

        PartDefinition growing_white = grape_cluster.addOrReplaceChild("growing_white", CubeListBuilder.create().texOffs(46, 34).addBox(-15.0F, -16.0F, -10.5F, 16.0F, 16.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(7.0F, 0.0F, 17.0F));

        PartDefinition mesh = lattice_wall.addOrReplaceChild("mesh", CubeListBuilder.create().texOffs(48, 64).addBox(-30.0F, -12.0F, 2.0F, 16.0F, 16.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(22.0F, -4.0F, 5.0F));

        PartDefinition support_right = lattice_wall.addOrReplaceChild("support_right", CubeListBuilder.create().texOffs(0, 0).addBox(-8.0F, -9.0F, 7.0F, 2.0F, 16.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(14.0F, -7.0F, -1.0F));

        PartDefinition corner_braces_right = lattice_wall.addOrReplaceChild("corner_braces_right", CubeListBuilder.create().texOffs(8, 0).addBox(6.0F, -16.0F, -2.0F, 2.0F, 2.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition corner_braces_right_bottom_r1 = corner_braces_right.addOrReplaceChild("corner_braces_right_bottom_r1", CubeListBuilder.create().texOffs(8, 10).addBox(-7.99F, -4.0F, 6.0F, 1.98F, 9.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(14.0F, -7.0F, -1.0F, 0.7854F, 0.0F, 0.0F));

        PartDefinition corner_braces_right_top_r1 = corner_braces_right.addOrReplaceChild("corner_braces_right_top_r1", CubeListBuilder.create().texOffs(3, 10).addBox(-7.99F, -6.0F, -5.5F, 1.98F, 1.0F, 11.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(14.0F, -7.0F, -1.0F, -0.7854F, 0.0F, 0.0F));

        PartDefinition support_left = lattice_wall.addOrReplaceChild("support_left", CubeListBuilder.create().texOffs(0, 0).addBox(-16.0F, -16.0F, 14.0F, 2.0F, 16.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(8.0F, 0.0F, -8.0F));

        PartDefinition corner_braces_left = lattice_wall.addOrReplaceChild("corner_braces_left", CubeListBuilder.create().texOffs(8, 0).addBox(-8.0F, -16.0F, -2.0F, 2.0F, 2.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition corner_braces_left_top_r1 = corner_braces_left.addOrReplaceChild("corner_braces_left_top_r1", CubeListBuilder.create().texOffs(8, 10).addBox(-7.99F, -4.0F, 6.0F, 1.98F, 9.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -7.0F, -1.0F, 0.7854F, 0.0F, 0.0F));

        PartDefinition corner_braces_left_bottom_r1 = corner_braces_left.addOrReplaceChild("corner_braces_left_bottom_r1", CubeListBuilder.create().texOffs(3, 10).addBox(-7.99F, -6.0F, -5.5F, 1.98F, 1.0F, 11.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -7.0F, -1.0F, -0.7854F, 0.0F, 0.0F));

        PartDefinition lattice_floor = partdefinition.addOrReplaceChild("lattice_floor", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition grape_cluster_floor = lattice_floor.addOrReplaceChild("grape_cluster_floor", CubeListBuilder.create(), PartPose.offset(0.0F, -4.5F, 4.0F));

        PartDefinition hanging_2_r1 = grape_cluster_floor.addOrReplaceChild("hanging_2_r1", CubeListBuilder.create().texOffs(32, 14).addBox(-7.0F, -11.5F, -1.0F, 8.0F, 12.0F, 0.0F), PartPose.offsetAndRotation(2.8284F, 14.5F, -3.4142F, 0.0F, 0.7854F, 0.0F));

        PartDefinition hanging_1_r1 = grape_cluster_floor.addOrReplaceChild("hanging_1_r1", CubeListBuilder.create().texOffs(32, 0).addBox(-7.0F, -9.5F, -1.0F, 8.0F, 10.0F, 0.0F), PartPose.offsetAndRotation(1.4142F, 12.5F, 0.8284F, 0.0F, -0.7854F, 0.0F));

        PartDefinition growing_red_floor = grape_cluster_floor.addOrReplaceChild("growing_red_floor", CubeListBuilder.create(), PartPose.offset(0.0F, -3.5F, 1.0F));

        PartDefinition growing_red_floor_r1 = growing_red_floor.addOrReplaceChild("growing_red_floor_r1", CubeListBuilder.create().texOffs(2, 52).addBox(-18.0F, -30.5F, -3.0F, 16.0F, 1.0F, 12.0F), PartPose.offsetAndRotation(3.0F, 32.5F, 8.5F, 0.0F, -1.5708F, 0.0F));

        PartDefinition sprout_floor = grape_cluster_floor.addOrReplaceChild("sprout_floor", CubeListBuilder.create(), PartPose.offset(0.0F, -3.5F, 1.0F));

        PartDefinition sprouting_grapes_floor_r1 = sprout_floor.addOrReplaceChild("sprouting_grapes_floor_r1", CubeListBuilder.create().texOffs(2, 39).addBox(-18.0F, -30.5F, -3.0F, 16.0F, 1.0F, 12.0F), PartPose.offsetAndRotation(3.0F, 32.5F, 8.5F, 0.0F, -1.5708F, 0.0F));

        PartDefinition growing_white_floor = grape_cluster_floor.addOrReplaceChild("growing_white_floor", CubeListBuilder.create(), PartPose.offset(0.0F, -3.5F, 1.0F));

        PartDefinition growing_white_floor_r1 = growing_white_floor.addOrReplaceChild("growing_white_floor_r1", CubeListBuilder.create().texOffs(2, 65).addBox(-18.0F, -30.5F, -3.0F, 16.0F, 1.0F, 12.0F), PartPose.offsetAndRotation(3.0F, 32.5F, 8.5F, 0.0F, -1.5708F, 0.0F));

        PartDefinition lattice_parts = lattice_floor.addOrReplaceChild("lattice_parts", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition cross_brace_r1 = lattice_parts.addOrReplaceChild("cross_brace_r1", CubeListBuilder.create().texOffs(-12, 24).addBox(-18.0F, -29.0F, -3.0F, 16.0F, 0.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.0F, 28.0F, 10.0F, 0.0F, -1.5708F, 0.0F));

        PartDefinition support_floor_left_r1 = lattice_parts.addOrReplaceChild("support_floor_left_r1", CubeListBuilder.create().texOffs(0, 0).addBox(-16.0F, -16.0F, 14.0F, 2.0F, 16.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).mirror().addBox(-30.0F, -16.0F, 14.0F, 2.0F, 16.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(22.0F, -16.0F, -8.0F, -1.5708F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 80, 80);
    }
}
