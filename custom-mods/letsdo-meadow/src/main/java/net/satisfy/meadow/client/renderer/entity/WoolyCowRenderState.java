package net.satisfy.meadow.client.renderer.entity;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

public class WoolyCowRenderState extends LivingEntityRenderState {
    public boolean sheared;
    public String variant = "highland_cattle";
    public float neckAngle;
    public float headAngle;
}
