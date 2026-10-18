package com.starfish_studios.another_furniture.client.renderer.blockentity;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.core.Direction;

public class ShelfRenderState extends BlockEntityRenderState {
   public Direction facing = Direction.NORTH;
   public final List<RenderItem> items = new ArrayList<>();

   public record RenderItem(ItemStackRenderState model, int slot, int index) {
   }
}
