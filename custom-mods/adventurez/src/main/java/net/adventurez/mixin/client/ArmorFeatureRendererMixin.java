package net.adventurez.mixin.client;

import net.adventurez.init.ItemInit;
import net.adventurez.item.GildedNetheriteArmor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.mojang.blaze3d.vertex.PoseStack;

@Environment(EnvType.CLIENT)
@Mixin(HumanoidArmorLayer.class)
public abstract class ArmorFeatureRendererMixin {
    private static final Identifier GILDED_NETHERITE_ARMOR = Identifier.fromNamespaceAndPath("adventurez", "textures/models/armor/gilded_netherite_layer_1_overlay.png");

    @Shadow
    protected abstract HumanoidModel getArmorModel(HumanoidRenderState state, EquipmentSlot slot);

    @Inject(method = "submit", at = @At("TAIL"))
    private void renderGlowingArmor(PoseStack poseStack, SubmitNodeCollector collector, int lightCoords, HumanoidRenderState state, float yRot, float xRot, CallbackInfo info) {
        ItemStack chest = state.chestEquipment;
        if (chest.is(ItemInit.GILDED_NETHERITE_CHESTPLATE) && GildedNetheriteArmor.isStoneGolemArmorActive(chest)) {
            HumanoidModel model = this.getArmorModel(state, EquipmentSlot.CHEST);
            collector.submitModel(model, state, poseStack, RenderTypes.armorCutoutNoCull(GILDED_NETHERITE_ARMOR), 220, OverlayTexture.NO_OVERLAY, state.outlineColor, null);
        }
    }
}
