package net.backslot.mixin.client;

import net.backslot.client.BackSlotRenderStateHolder;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(AvatarRenderState.class)
public class AvatarRenderStateMixin implements BackSlotRenderStateHolder {

    @Unique
    private ItemStack backslot$backItem = ItemStack.EMPTY;

    @Unique
    private ItemStack backslot$beltItem = ItemStack.EMPTY;

    @Override
    public ItemStack backslot$backItem() {
        return this.backslot$backItem;
    }

    @Override
    public void backslot$setBackItem(ItemStack stack) {
        this.backslot$backItem = stack == null ? ItemStack.EMPTY : stack;
    }

    @Override
    public ItemStack backslot$beltItem() {
        return this.backslot$beltItem;
    }

    @Override
    public void backslot$setBeltItem(ItemStack stack) {
        this.backslot$beltItem = stack == null ? ItemStack.EMPTY : stack;
    }
}
