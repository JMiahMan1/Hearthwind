package net.satisfy.meadow.client.renderer.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.Vec3;
import net.satisfy.meadow.core.block.WardrobeBlock;
import net.satisfy.meadow.core.block.entity.WardrobeBlockEntity;

// 26.2: BlockEntityRenderer is now (entity, state) with submit() through a
// collector. Armor still renders worn (same invisible-stand trick, same poses
// and scales), but each piece delegates to the entity dispatcher so equipment
// assets resolve through vanilla's ArmorStandRenderer instead of a bespoke
// HumanoidArmorLayer (whose 26.2 constructor needs an EquipmentLayerRenderer
// the block-entity context does not provide).
public class WardrobeRenderer implements BlockEntityRenderer<WardrobeBlockEntity, WardrobeRenderer.State> {
    public static class State extends BlockEntityRenderState {
        public Level level;
        public Direction facing = Direction.NORTH;
        public boolean skip;
        public float partialTick;
        public ItemStack chest = ItemStack.EMPTY;
        public ItemStack legs = ItemStack.EMPTY;
        public ItemStack feet = ItemStack.EMPTY;
    }

    public static float CHEST_TX = -0.2f;
    public static float CHEST_TY = 1.2f;
    public static float CHEST_TZ = 0.0f;
    public static float CHEST_YAW = 90.0f;
    public static float CHEST_PITCH = 0.0f;
    public static float CHEST_ROLL = 0.0f;
    public static float CHEST_SCALE = 1.7f;

    public static float LEGS_TX = 0.2f;
    public static float LEGS_TY = 1.6f;
    public static float LEGS_TZ = 0.0f;
    public static float LEGS_YAW = 90.0f;
    public static float LEGS_PITCH = 0.0f;
    public static float LEGS_ROLL = 0.0f;
    public static float LEGS_SCALE = 1.7f;

    public static float FEET_TX = 0.2f;
    public static float FEET_TY = 1.4f;
    public static float FEET_TZ = 0.0f;
    public static float FEET_YAW = 45.0f;
    public static float FEET_PITCH = 0.0f;
    public static float FEET_ROLL = 0.0f;
    public static float FEET_SCALE = 1.7f;

    public WardrobeRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(WardrobeBlockEntity be, State state, float partialTick, Vec3 cameraPos,
            ModelFeatureRenderer.CrumblingOverlay crumbling) {
        BlockEntityRenderState.extractBase(be, state, crumbling);
        state.partialTick = partialTick;
        state.level = be.getLevel();
        if (state.level == null) {
            state.skip = true;
            return;
        }
        if (be.getBlockState().getValue(WardrobeBlock.HALF) == DoubleBlockHalf.UPPER) {
            state.skip = true;
            return;
        }
        state.skip = false;
        state.facing = be.getBlockState().getValue(WardrobeBlock.FACING);
        state.chest = be.getItem(WardrobeBlockEntity.SLOT_CHEST).copy();
        state.legs = be.getItem(WardrobeBlockEntity.SLOT_LEGS).copy();
        state.feet = be.getItem(WardrobeBlockEntity.SLOT_FEET).copy();
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraState) {
        if (state.skip || state.level == null) return;

        poseStack.pushPose();
        poseStack.translate(0.5, 1.6, 0.5);
        switch (state.facing) {
            case SOUTH -> poseStack.mulPose(Axis.YP.rotationDegrees(180));
            case WEST -> poseStack.mulPose(Axis.YP.rotationDegrees(90));
            case EAST -> poseStack.mulPose(Axis.YP.rotationDegrees(270));
            default -> {
            }
        }

        renderArmorPiece(poseStack, collector, cameraState, state,
                state.chest, EquipmentSlot.CHEST,
                CHEST_TX, CHEST_TY, CHEST_TZ, CHEST_YAW, CHEST_PITCH, CHEST_ROLL, CHEST_SCALE);

        renderArmorPiece(poseStack, collector, cameraState, state,
                state.legs, EquipmentSlot.LEGS,
                LEGS_TX, LEGS_TY, LEGS_TZ, LEGS_YAW, LEGS_PITCH, LEGS_ROLL, LEGS_SCALE);

        renderArmorPiece(poseStack, collector, cameraState, state,
                state.feet, EquipmentSlot.FEET,
                FEET_TX, FEET_TY, FEET_TZ, FEET_YAW, FEET_PITCH, FEET_ROLL, FEET_SCALE);

        poseStack.popPose();
    }

    private void renderArmorPiece(PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraState,
            State state, ItemStack stack, EquipmentSlot slot, float tx, float ty, float tz,
            float yaw, float pitch, float roll, float scale) {
        if (stack.isEmpty()) return;

        ArmorStand partDummy = new ArmorStand(EntityTypes.ARMOR_STAND, state.level);
        partDummy.setPos(state.blockPos.getX() + 0.5, state.blockPos.getY(), state.blockPos.getZ() + 0.5);
        partDummy.setNoBasePlate(true);
        partDummy.setInvisible(true);
        partDummy.setItemSlot(slot, stack);

        poseStack.pushPose();
        poseStack.translate(tx, ty, tz);
        poseStack.mulPose(Axis.YP.rotationDegrees(yaw));
        poseStack.mulPose(Axis.XP.rotationDegrees(pitch));
        poseStack.mulPose(Axis.ZP.rotationDegrees(roll));
        poseStack.scale(scale, -scale, -scale);

        EntityRenderDispatcher dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        EntityRenderState renderState = dispatcher.extractEntity(partDummy, state.partialTick);
        dispatcher.submit(renderState, cameraState, 0.0, 0.0, 0.0, poseStack, collector);

        poseStack.popPose();
    }
}
