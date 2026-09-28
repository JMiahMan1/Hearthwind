package draylar.inmis.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import draylar.inmis.Inmis;
import draylar.inmis.client.model.BabyBackpackModel;
import draylar.inmis.client.model.BackpackModel;
import draylar.inmis.item.BackpackItem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;

/**
 * Draws a chest-slot backpack on the player's back, ported from Inmis's
 * {@code BackpackFeature}: flip the item over, nudge it onto the back and
 * add a lean while sneaking.
 *
 * <p>Inmis Addon (MIT) replaced the flat item sprite with a hand-built 3D
 * backpack, and on 26.2 that model has to go through the submit-node
 * collector instead of {@code FeatureRenderer#render}. The model is baked
 * once here and submitted inside the same pose block as the item fallback,
 * which stays for the {@code trinketRendering = false} case.
 */
@Environment(EnvType.CLIENT)
public class InmisBackpackLayer extends RenderLayer<AvatarRenderState, PlayerModel> {
    private static final String TEXTURE_DIRECTORY = "textures/entity/";

    private final ItemStackRenderState renderState = new ItemStackRenderState();
    private final BackpackModel backpackModel = new BackpackModel(BackpackModel.texturedModelData().bakeRoot());
    private final BabyBackpackModel babyBackpackModel =
            new BabyBackpackModel(BabyBackpackModel.texturedModelData().bakeRoot());

    public InmisBackpackLayer(RenderLayerParent<AvatarRenderState, PlayerModel> parent) {
        super(parent);
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords,
            AvatarRenderState state, float yRot, float xRot) {
        ItemStack stack = ((InmisRenderStateHolder) state).inmis$chestItem();
        if (!(stack.getItem() instanceof BackpackItem item)) {
            return;
        }

        poseStack.pushPose();
        poseStack.mulPose(Axis.XP.rotationDegrees(180));
        poseStack.translate(0, -0.2, -0.25);
        if (state.isCrouching) {
            poseStack.mulPose(Axis.XP.rotationDegrees(25));
            poseStack.translate(0, -0.2, 0);
        }

        if (Inmis.CONFIG.trinketRendering && submitModel(poseStack, submitNodeCollector, lightCoords, state, item, stack)) {
            poseStack.popPose();
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        this.renderState.clear();
        minecraft.getItemModelResolver().updateForTopItem(this.renderState, stack,
                ItemDisplayContext.FIXED, minecraft.level, null, 0);
        this.renderState.submit(poseStack, submitNodeCollector, lightCoords,
                OverlayTexture.NO_OVERLAY, state.outlineColor);
        poseStack.popPose();
    }

    private boolean submitModel(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords,
            AvatarRenderState state, BackpackItem item, ItemStack stack) {
        Identifier texture = backpackTexture(item);
        if (texture == null) {
            return false;
        }

        // The addon rendered the baby model for the first registered tier.
        boolean baby = !Inmis.BACKPACKS.isEmpty() && item == Inmis.BACKPACKS.get(0);
        int[] tint = dyeTint(stack);
        if (baby) {
            this.babyBackpackModel.setupAnim(state);
            submitNodeCollector.submitModel(this.babyBackpackModel, state, poseStack, texture,
                    lightCoords, tint[0], tint[1], null);
        } else {
            this.backpackModel.setupAnim(state);
            submitNodeCollector.submitModel(this.backpackModel, state, poseStack, texture,
                    lightCoords, tint[0], tint[1], null);
        }
        return true;
    }

    /**
     * The addon's texture for this tier, or null when the model has no
     * artwork (then the caller falls back to the flat item sprite).
     */
    private static Identifier backpackTexture(BackpackItem item) {
        Identifier id = BuiltInRegistries.ITEM.getKey(item);
        if (id == null) {
            return null;
        }
        return Identifier.fromNamespaceAndPath(Inmis.MOD_ID, TEXTURE_DIRECTORY + id.getPath() + ".png");
    }

    /** The dyed colour of the backpack as RGB, or opaque white when undyed. */
    private static int[] dyeTint(ItemStack stack) {
        if (stack.getItem() instanceof BackpackItem item && item.getTier().isDyeable()) {
            int rgb = DyedItemColor.getOrDefault(stack, DyedItemColor.LEATHER_COLOR) & 0xFFFFFF;
            return new int[] {(rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF};
        }
        return new int[] {255, 255};
    }
}
