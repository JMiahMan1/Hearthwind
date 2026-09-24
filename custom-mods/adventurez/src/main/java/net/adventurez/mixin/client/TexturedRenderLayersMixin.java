package net.adventurez.mixin.client;

import net.adventurez.block.entity.ShadowChestEntity;
import net.adventurez.init.BlockInit;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.blockentity.ChestRenderer;
import net.minecraft.client.renderer.blockentity.state.ChestRenderState;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Environment(EnvType.CLIENT)
@Mixin(ChestRenderer.class)
public class TexturedRenderLayersMixin {
    private static final SpriteId SHADOW = new SpriteId(TextureAtlas.LOCATION_BLOCKS, Identifier.fromNamespaceAndPath("minecraft", "entity/chest/shadow_chest"));

    @ModifyVariable(method = "submit", at = @At("STORE"), ordinal = 0)
    private SpriteId selectShadowTexture(SpriteId original, ChestRenderState state) {
        return state.blockEntityType == BlockInit.SHADOW_CHEST_ENTITY ? SHADOW : original;
    }
}
