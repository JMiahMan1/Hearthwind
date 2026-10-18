package net.jmb19905.niftycarts.client.renderer.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.level.Level;

@Environment(EnvType.CLIENT)
public class SeedDrillRenderState extends CartRenderState {
   public net.minecraft.core.NonNullList<net.minecraft.world.item.ItemStack> seeds;
   public net.minecraft.core.NonNullList<net.minecraft.client.renderer.item.ItemStackRenderState> itemStates;
   public Level level;
}
