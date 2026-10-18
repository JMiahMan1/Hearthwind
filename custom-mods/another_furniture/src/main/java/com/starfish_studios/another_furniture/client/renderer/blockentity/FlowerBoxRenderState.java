package com.starfish_studios.another_furniture.client.renderer.blockentity;

import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.core.Direction;

public class FlowerBoxRenderState extends BlockEntityRenderState {
   public Direction facing = Direction.NORTH;
   public boolean attached;
   public final BlockModelRenderState[] lower = new BlockModelRenderState[]{new BlockModelRenderState(), new BlockModelRenderState()};
   public final BlockModelRenderState[] upper = new BlockModelRenderState[]{new BlockModelRenderState(), new BlockModelRenderState()};
   public final boolean[] hasLower = new boolean[2];
   public final boolean[] hasUpper = new boolean[2];
}
