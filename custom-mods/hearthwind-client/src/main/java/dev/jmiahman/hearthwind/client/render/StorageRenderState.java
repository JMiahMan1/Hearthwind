package dev.jmiahman.hearthwind.client.render;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;

public class StorageRenderState extends BlockEntityRenderState {
    public Direction facing = Direction.NORTH;
    public Identifier storageType;
    public final List<ItemStackRenderState> itemStates = new ArrayList<>();
}
