package net.satisfy.farm_and_charm.client.renderer.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.satisfy.farm_and_charm.FarmAndCharm;
import net.satisfy.farm_and_charm.client.model.CraftingBowlModel;
import net.satisfy.farm_and_charm.client.util.ClientUtil;
import net.satisfy.farm_and_charm.core.block.CraftingBowlBlock;
import net.satisfy.farm_and_charm.core.block.entity.CraftingBowlBlockEntity;

// 26.2: same bowl/whisk transforms, full-bowl texture swap and item layout,
// submitted through BakedPartModel wrappers + ClientUtil item submits.
public class CraftingBowlRenderer implements BlockEntityRenderer<CraftingBowlBlockEntity, CraftingBowlRenderer.State> {
    private final ModelPart bowl;
    private final ModelPart swing;
    private final BakedPartModel bowlModel;
    private final BakedPartModel swingModel;
    private final ItemModelResolver itemResolver;

    public static class State extends BlockEntityRenderState {
        public boolean full;
        public float whiskAngle;
        public NonNullList<ItemStack> items = NonNullList.create();
        public Level level;
    }

    public CraftingBowlRenderer(BlockEntityRendererProvider.Context context) {
        ModelPart root = context.bakeLayer(CraftingBowlModel.LAYER_LOCATION);
        this.bowl = root.getChild("bowl");
        this.swing = root.getChild("swing");
        this.bowlModel = new BakedPartModel(this.bowl);
        this.swingModel = new BakedPartModel(this.swing);
        this.itemResolver = context.itemModelResolver();
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(CraftingBowlBlockEntity be, State state, float partialTick, Vec3 cameraPos,
            ModelFeatureRenderer.CrumblingOverlay crumbling) {
        BlockEntityRenderState.extractBase(be, state, crumbling);
        Level level = be.getLevel();
        state.level = level;
        if (level == null) return;
        BlockState blockState = level.getBlockState(be.getBlockPos());
        if (!(blockState.getBlock() instanceof CraftingBowlBlock)) return;
        state.full = be.getStirringProgress() > CraftingBowlBlock.STIRS_NEEDED;
        state.whiskAngle = be.getInterpolatedWhiskAngle(partialTick);
        state.items = NonNullList.create();
        for (ItemStack stack : be.getItems()) state.items.add(stack.copy());
    }

    @Override
    public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState cameraState) {
        if (state.level == null) return;

        pose.pushPose();
        pose.mulPose(Axis.XP.rotationDegrees(180));
        pose.translate(0.5f, -1.5f, -0.5f);

        Identifier tex = state.full
                ? Identifier.fromNamespaceAndPath(FarmAndCharm.MOD_ID, "textures/entity/crafting_bowl_full.png")
                : Identifier.fromNamespaceAndPath(FarmAndCharm.MOD_ID, "textures/entity/crafting_bowl.png");

        collector.submitModel(bowlModel, state, pose, RenderTypes.entityTranslucent(tex),
                state.lightCoords, OverlayTexture.NO_OVERLAY, EntityRenderState.NO_OUTLINE, state.breakProgress);
        pose.mulPose(Axis.YP.rotation(state.whiskAngle));
        collector.submitModel(swingModel, state, pose, RenderTypes.entityTranslucent(tex),
                state.lightCoords, OverlayTexture.NO_OVERLAY, EntityRenderState.NO_OUTLINE, state.breakProgress);

        renderItems(state, pose, collector);
        pose.popPose();
    }

    private void renderItems(State state, PoseStack poseStack, SubmitNodeCollector collector) {
        if (state.items.size() < 4) return;
        poseStack.translate(0f, 1.25f, 0f);
        poseStack.scale(0.35f, 0.35f, 0.35f);

        float offset = 0.26f;

        poseStack.translate(-offset, 0.1f, -offset);
        poseStack.mulPose(Axis.XP.rotationDegrees(90));
        ClientUtil.renderItem(state.items.get(0), poseStack, collector, itemResolver, state.level, state.blockPos, state.lightCoords);
        poseStack.mulPose(Axis.XP.rotationDegrees(-90));

        poseStack.translate(2 * offset, 0.1f, 0f);
        poseStack.mulPose(Axis.XP.rotationDegrees(90));
        ClientUtil.renderItem(state.items.get(1), poseStack, collector, itemResolver, state.level, state.blockPos, state.lightCoords);
        poseStack.mulPose(Axis.XP.rotationDegrees(-90));

        poseStack.translate(0f, 0.1f, 2 * offset);
        poseStack.mulPose(Axis.XP.rotationDegrees(90));
        ClientUtil.renderItem(state.items.get(2), poseStack, collector, itemResolver, state.level, state.blockPos, state.lightCoords);
        poseStack.mulPose(Axis.XP.rotationDegrees(-90));

        poseStack.translate(-2 * offset, 0.1f, 0f);
        poseStack.mulPose(Axis.XP.rotationDegrees(90));
        ClientUtil.renderItem(state.items.get(3), poseStack, collector, itemResolver, state.level, state.blockPos, state.lightCoords);
        poseStack.mulPose(Axis.XP.rotationDegrees(-90));
    }
}
