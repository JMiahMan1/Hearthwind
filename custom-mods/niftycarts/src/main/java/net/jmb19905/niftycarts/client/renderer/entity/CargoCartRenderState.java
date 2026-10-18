package net.jmb19905.niftycarts.client.renderer.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.state.ArmorStandRenderState;
import net.minecraft.world.level.Level;

@Environment(EnvType.CLIENT)
public class CargoCartRenderState extends CartRenderState {
   public net.minecraft.core.NonNullList<net.minecraft.world.item.ItemStack> cargo;
   public net.minecraft.core.NonNullList<net.minecraft.client.renderer.item.ItemStackRenderState> cargoStates;
   public long rngSeed;
   public Level level;
   public boolean extraWheel;
   public boolean flowerBasket;
   public ArmorStandRenderState armorRenderState;
   public ArmorStandRenderState armorRenderState2;
}
