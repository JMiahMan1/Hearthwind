package com.talhanation.smallships.client.renderer.entity;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Axis;
import com.talhanation.smallships.SmallShipsMod;
import com.talhanation.smallships.client.model.ShipModel;
import com.talhanation.smallships.client.renderer.entity.state.ShipRenderState;
import com.talhanation.smallships.world.entity.ship.*;
import com.talhanation.smallships.world.entity.ship.abilities.*;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.stream.Stream;

public abstract class ShipRenderer<T extends Ship> extends EntityRenderer<T, ShipRenderState> {
    protected final Map<Ship.Type, Pair<Identifier, ShipModel<T>>> boatResources;

    public ShipRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.8F;

        this.boatResources = Stream.of(Ship.Type.values()).collect(ImmutableMap.toImmutableMap(
                (type) -> type,
                (type) -> Pair.of(
                        this.getTextureLocation(type),
                        this.createBoatModel(context, type))));
    }

    @Override
    public @NotNull ShipRenderState createRenderState() {
        return new ShipRenderState();
    }

    @Override
    public void extractRenderState(T entity, ShipRenderState state, float f) {
        super.extractRenderState(entity, state, f);

        state.shipAttributes = entity.getAttributes();
        state.hurtTime = entity.getHurtTime() - f;
        state.hurtDir = entity.getHurtDir();
        state.damage = entity.getDamage() - f;
        state.level = entity.level();
        state.bubbleAngle = entity.getBubbleAngle(f);
        state.waveAngle = entity.getWaveAngle(f);
        state.variant = entity.getVariant();
        state.sunken = entity.isSunken();
        state.yRot = entity.getYRot(f);
        state.rotationSpeed = entity.getRotSpeed();
        state.partialTicks = f;

        state.hasContainer = entity instanceof ContainerShip;
        if (entity instanceof ContainerShip containerShip) {
            state.invFillState = containerShip.getInvFillState();
        }

        if (entity instanceof Cannonable cannonShip) {
            state.cannonable = cannonShip;
        }
        if (entity instanceof Bannerable bannerShip) {
            state.bannerable = bannerShip;
        }
        if (entity instanceof Paddleable) {
            state.isPaddleShip = true;
            state.rowingTimeLeft = entity.getRowingTime(0, f);
            state.rowingTimeRight = entity.getRowingTime(1, f);
        }
        if (entity instanceof Sailable sailShip) {
            state.sailable = sailShip;
        }
        if (entity instanceof Shieldable shieldShip) {
            state.shieldable = shieldShip;
        }
    }

    protected abstract ShipModel<T> createBoatModel(EntityRendererProvider.Context context, Ship.Type type);

    protected Identifier getTextureLocation(Ship.Type type) {
        return Identifier.fromNamespaceAndPath(SmallShipsMod.MOD_ID, "textures/entity/ship/" + ShipRenderer.getNameFromType(type) + ".png");
    }

    @Override
    public void submit(ShipRenderState state, PoseStack poseStack, SubmitNodeCollector nodeCollector, CameraRenderState cameraRenderState) {
        Attributes shipAttributes = state.shipAttributes;
        if (shipAttributes != null) {
            float h = state.hurtTime / ((shipAttributes.maxHealth * state.boundingBoxWidth) / 40.0F);
            float j = state.damage;
            if (h > 0.0F) {
                poseStack.mulPose(Axis.XP.rotationDegrees(Mth.sin(h) * h * j / 10.0F * (float) state.hurtDir));
            }
        }

        Pair<Identifier, ShipModel<T>> pair = this.boatResources.get(state.variant);
        if (pair != null) {
            Identifier identifier = pair.getFirst();
            ShipModel<T> shipModel = pair.getSecond();
            nodeCollector.submitModel(shipModel, state, poseStack, identifier, 15728880, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF, null);
        }

        super.submit(state, poseStack, nodeCollector, cameraRenderState);
    }

    public static String getNameFromType(Ship.Type type) {
        return type.getName();
    }
}
