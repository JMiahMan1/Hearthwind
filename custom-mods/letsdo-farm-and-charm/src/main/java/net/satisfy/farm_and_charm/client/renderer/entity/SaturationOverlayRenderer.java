package net.satisfy.farm_and_charm.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.chicken.Chicken;
import net.minecraft.world.entity.animal.cow.Cow;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.satisfy.farm_and_charm.core.registry.ObjectRegistry;
import net.satisfy.farm_and_charm.core.util.SaturationTracker;

// 26.2: the three saturation icons submit as items through
// ItemModelResolver; same billboard, thresholds, progress alpha and
// per-animal icons as before.
public class SaturationOverlayRenderer {

    public static void submit(PoseStack poseStack, SubmitNodeCollector collector, LivingEntity entity, int level, int foodCounter) {
        if (entity.isBaby()) return;

        EntityType<?> type = entity.getType();
        if (!(type == EntityTypes.COW || type == EntityTypes.PIG || type == EntityTypes.SHEEP || type == EntityTypes.CHICKEN)) return;

        Minecraft mc = Minecraft.getInstance();
        ItemModelResolver itemResolver = mc.getItemModelResolver();

        double yOffset = entity.getBbHeight() + 0.6;
        if (entity.hasCustomName()) {
            yOffset += 0.3;
        }

        poseStack.pushPose();
        poseStack.translate(0.0, yOffset, 0.0);
        poseStack.mulPose(mc.gameRenderer.mainCamera().rotation());
        poseStack.scale(0.4f, 0.4f, 0.4f);

        ItemStack icon = resolveIcon(entity);
        int[] thresholds = {5, 10, 15, 20};

        for (int i = 0; i < 3; i++) {
            poseStack.pushPose();
            poseStack.translate((i - 1) * 1.1, 0.0, 0.0);

            float alpha;

            if (level > 0) {
                if (i < level) {
                    alpha = 0.9f;
                } else if (i == level) {
                    float progress = (float) foodCounter / thresholds[level];
                    alpha = Mth.clamp(0.4f + 0.5f * progress, 0.4f, 0.9f);
                } else {
                    alpha = 0.4f;
                }
            } else {
                float progress = getProgress(foodCounter, i, thresholds);
                alpha = 0.4f + (0.5f * progress);
            }

            int light = Mth.floor(240 * alpha);
            ItemStackRenderState renderState = new ItemStackRenderState();
            itemResolver.updateForNonLiving(renderState, icon, ItemDisplayContext.FIXED, entity);
            renderState.submit(poseStack, collector, light, OverlayTexture.NO_OVERLAY, EntityRenderState.NO_OUTLINE);

            poseStack.popPose();
        }

        poseStack.popPose();
    }

    private static float getProgress(int foodCounter, int i, int[] thresholds) {
        int start = switch (i) {
            case 1 -> thresholds[0];
            case 2 -> thresholds[0] + thresholds[1];
            default -> 0;
        };
        int end = switch (i) {
            case 0 -> thresholds[0];
            case 1 -> thresholds[0] + thresholds[1];
            case 2 -> thresholds[0] + thresholds[1] + thresholds[2];
            default -> 1;
        };
        return Mth.clamp((float)(foodCounter - start) / (end - start), 0f, 1f);
    }

    private static ItemStack resolveIcon(LivingEntity entity) {
        if (entity instanceof Cow) return new ItemStack(Items.WHEAT);
        if (entity instanceof Pig) return new ItemStack(Items.APPLE);
        if (entity instanceof Sheep) return new ItemStack(Items.WHEAT);
        if (entity instanceof Chicken) return new ItemStack(Items.WHEAT_SEEDS);
        return new ItemStack(ObjectRegistry.HORSE_FODDER.get());
    }

    public static void renderIfApplicable(PoseStack poseStack, SubmitNodeCollector collector, LivingEntity entity) {
        if (!(entity instanceof SaturationTracker.SaturatedAnimal saturated)) return;

        SaturationTracker tracker = saturated.farm_and_charm$getSaturationTracker();
        submit(poseStack, collector, entity, tracker.level(), tracker.foodCounter());
    }
}
