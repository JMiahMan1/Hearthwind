package draylar.inmis.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import draylar.inmis.client.InmisRenderStateHolder;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.item.ItemStack;

@Mixin(AvatarRenderState.class)
public class AvatarRenderStateMixin implements InmisRenderStateHolder {
    @Unique
    private ItemStack inmis$chestItem = ItemStack.EMPTY;

    @Override
    public ItemStack inmis$chestItem() {
        return this.inmis$chestItem;
    }

    @Override
    public void inmis$setChestItem(ItemStack stack) {
        this.inmis$chestItem = stack == null ? ItemStack.EMPTY : stack;
    }
}
