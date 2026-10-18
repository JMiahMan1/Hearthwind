package net.medievalweapons.entity.renderer;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;

@Environment(EnvType.CLIENT)
public class JavelinRenderState extends EntityRenderState {
   public float xRot;
   public float yRot;
   public final ItemStackRenderState item = new ItemStackRenderState();
}
