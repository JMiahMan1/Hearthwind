package net.adventurez.mixin.client;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.PoseStack;
import net.adventurez.entity.BlackstoneGolemEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.IronGolemCrackinessLayer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Crackiness;
import net.minecraft.client.renderer.entity.state.IronGolemRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.Map;

@Environment(EnvType.CLIENT)
@Mixin(IronGolemCrackinessLayer.class)
public class IronGolemCrackFeatureRendererMixin {
    private static final Map<Crackiness.Level, Identifier> BLACKSTONED_TEXTURES = ImmutableMap.of(
        Crackiness.Level.LOW, Identifier.fromNamespaceAndPath("adventurez", "textures/entity/feature/iron_golem_blackstoned_low.png"),
        Crackiness.Level.MEDIUM, Identifier.fromNamespaceAndPath("adventurez", "textures/entity/feature/iron_golem_blackstoned_medium.png"),
        Crackiness.Level.HIGH, Identifier.fromNamespaceAndPath("adventurez", "textures/entity/feature/iron_golem_blackstoned_high.png")
    );

    @ModifyVariable(method = "submit", at = @At("STORE"), ordinal = 0)
    private Identifier modifyTexture(Identifier original, PoseStack poseStack, SubmitNodeCollector collector, int lightCoords, IronGolemRenderState state, float yRot, float xRot) {
        if (IronGolemRenderStateHelper.isBlackstoned()) {
            return BLACKSTONED_TEXTURES.get(state.crackiness);
        }
        return original;
    }
}
