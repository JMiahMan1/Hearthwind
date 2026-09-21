package net.satisfy.candlelight.client.renderer.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.model.geom.ModelPart;
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
import net.minecraft.world.phys.Vec3;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.satisfy.candlelight.Candlelight;
import net.satisfy.candlelight.client.model.TypewriterModel;
import net.satisfy.candlelight.core.block.TypewriterBlock;
import net.satisfy.candlelight.core.block.entity.TypewriterEntity;
import net.satisfy.candlelight.core.registry.ObjectRegistry;

// 26.2: submit through the collector; per-frame key/roller/flag offsets are
// still applied to the live parts before the single submitModel call.
public class TypewriterRenderer implements BlockEntityRenderer<TypewriterEntity, TypewriterRenderer.State> {
    private static final Identifier IRON_TEXTURE = Candlelight.identifier("textures/entity/typewriter_iron.png");
    private static final Identifier GOLD_TEXTURE = Candlelight.identifier("textures/entity/typewriter_gold.png");
    private final ModelPart typewriter;
    private final ModelPart space;
    private final ModelPart enter;
    private final ModelPart roller;
    private final ModelPart paper;
    private final ModelPart paperWritten;
    private final List<ModelPart> keyParts;
    private final BakedPartModel typewriterModel;

    public static class State extends BlockEntityRenderState {
        public Direction facing = Direction.NORTH;
        public int full = 0;
        public float lineProgress = 0f;
        public int spaceTicks = 0;
        public int enterTicks = 0;
        public int keyBounceTicks = 0;
        public int bouncingKeyIndex = -1;
        public int rollerSnapTicks = 0;
        public Identifier texture = TypewriterRenderer.IRON_TEXTURE;
    }

    public TypewriterRenderer(BlockEntityRendererProvider.Context context) {
        ModelPart root = context.bakeLayer(TypewriterModel.LAYER_LOCATION);
        this.typewriter = root.getChild("typewriter");
        ModelPart keyboard = this.typewriter.getChild("keyboard");
        ModelPart sp;
        try {
            sp = keyboard.getChild("space");
        } catch (Exception e) {
            sp = keyboard.getChild("spacebar");
        }
        this.space = sp;
        ModelPart en;
        try {
            en = keyboard.getChild("enter");
        } catch (Exception e) {
            en = keyboard.getChild("return");
        }
        this.enter = en;
        ModelPart rl;
        try {
            rl = this.typewriter.getChild("roller");
        } catch (Exception e) {
            rl = this.typewriter.getChild("carriage");
        }
        this.roller = rl;
        ModelPart pp;
        try {
            pp = this.typewriter.getChild("paper");
        } catch (Exception e) {
            pp = this.typewriter.getChild("sheet");
        }
        this.paper = pp;
        ModelPart pw;
        try {
            pw = this.typewriter.getChild("paper_written");
        } catch (Exception e) {
            pw = this.typewriter.getChild("sheet_written");
        }
        this.paperWritten = pw;
        List<ModelPart> keys = new ArrayList<>();
        for (int i = 1; i <= 7; i++) {
            try {
                keys.add(keyboard.getChild("button_" + i));
            } catch (Exception ignored) {
            }
        }
        if (keys.isEmpty()) {
            for (int i = 0; i < 8; i++) {
                try {
                    keys.add(keyboard.getChild("key" + i));
                } catch (Exception ignored) {
                }
            }
        }
        this.keyParts = keys;
        this.typewriterModel = new BakedPartModel(this.typewriter);
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(TypewriterEntity be, State state, float partialTick, Vec3 cameraPos,
            ModelFeatureRenderer.CrumblingOverlay crumbling) {
        BlockEntityRenderState.extractBase(be, state, crumbling);
        Level level = be.getLevel();
        if (level == null) return;
        BlockState s = level.getBlockState(be.getBlockPos());
        if (!(s.getBlock() instanceof TypewriterBlock)) return;
        state.facing = s.getValue(TypewriterBlock.FACING);
        state.texture = s.getBlock() == ObjectRegistry.TYPEWRITER_GOLD.get() ? GOLD_TEXTURE : IRON_TEXTURE;
        state.full = s.getValue(TypewriterBlock.FULL);
        state.lineProgress = be.getLineProgress();
        state.spaceTicks = be.getSpaceTicks();
        state.enterTicks = be.getEnterTicks();
        state.keyBounceTicks = be.getKeyBounceTicks();
        state.rollerSnapTicks = be.getRollerSnapTicks();
        state.bouncingKeyIndex = be.getBouncingKeyIndex();
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraState) {
        poseStack.pushPose();

        Direction facing = state.facing;

        float rotY = switch (facing) {
            case EAST -> 270f;
            case SOUTH -> 0f;
            case WEST -> 90f;
            default -> 180f;
        };

        float offsetX;
        float offsetZ;

        switch (facing) {
            case SOUTH -> {
                offsetX = 0.5f;
                offsetZ = -0.5f;
            }
            case EAST -> {
                offsetX = -0.5f;
                offsetZ = -0.5f;
            }
            case WEST -> {
                offsetX = 0.5f;
                offsetZ = 0.5f;
            }
            default -> {
                offsetX = -0.5f;
                offsetZ = 0.5f;
            }
        }

        poseStack.mulPose(Axis.XP.rotationDegrees(180f));
        poseStack.mulPose(Axis.YP.rotationDegrees(rotY));
        poseStack.translate(offsetX, -1.5f, offsetZ);

        float prog = state.rollerSnapTicks > 0 ? 0f : state.lineProgress;
        if (prog < 0f) prog = 0f;
        if (prog > 1f) prog = 1f;
        float rx = 0.15f + prog * 4.5f;

        float sy = state.spaceTicks > 0 ? 0.5f : 0f;
        float ey = state.enterTicks > 0 ? 0.5f : 0f;

        if (this.space != null) this.space.y += sy;
        if (this.enter != null) this.enter.y += ey;

        int idx = state.bouncingKeyIndex;
        boolean bounced = false;
        if (state.keyBounceTicks > 0 && state.spaceTicks == 0 && state.enterTicks == 0 && !this.keyParts.isEmpty()) {
            if (idx < 0 || idx >= this.keyParts.size()) {
                idx = RandomSource.create(state.blockPos.asLong()).nextInt(this.keyParts.size());
            }
            this.keyParts.get(idx).y += 0.5f;
            bounced = true;
        }

        if (this.roller != null) this.roller.x += rx;

        if (this.paper != null) this.paper.visible = false;
        if (this.paperWritten != null) this.paperWritten.visible = false;
        if (state.full == 1 && this.paper != null) this.paper.visible = true;
        else if (state.full == 2 && this.paperWritten != null) this.paperWritten.visible = true;

        collector.submitModel(typewriterModel, state, poseStack, RenderTypes.entityCutout(getTexture(state)),
                state.lightCoords, OverlayTexture.NO_OVERLAY, EntityRenderState.NO_OUTLINE, state.breakProgress);

        if (this.space != null) this.space.y -= sy;
        if (this.enter != null) this.enter.y -= ey;
        if (bounced && idx < this.keyParts.size()) {
            this.keyParts.get(idx).y -= 0.5f;
        }
        if (this.roller != null) this.roller.x -= rx;

        poseStack.popPose();
    }

    private Identifier getTexture(State state) {
        return state.texture;
    }
}