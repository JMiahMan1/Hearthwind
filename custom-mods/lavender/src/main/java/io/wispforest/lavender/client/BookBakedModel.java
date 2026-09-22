package io.wispforest.lavender.client;

import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class BookBakedModel implements ItemModel {

    private final ItemModel defaultModel;

    public BookBakedModel(ItemModel defaultModel) {
        this.defaultModel = defaultModel;
    }

    @Override
    public void update(ItemStackRenderState state, ItemStack stack, ItemModelResolver resolver, ItemDisplayContext transformationMode, @Nullable ClientLevel world, @Nullable ItemOwner owner, int seed) {
        this.defaultModel.update(state, stack, resolver, transformationMode, world, owner, seed);
    }
}
