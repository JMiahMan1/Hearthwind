package draylar.inmis.mixin;

import draylar.inmis.Inmis;
import draylar.inmis.item.BackpackItem;
import draylar.inmis.item.component.BackpackComponent;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.ShapedRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Upgrading a backpack in a crafting table keeps its contents, resized to
 * the new tier (upstream ShapedRecipeMixin behaviour).
 */
@Mixin(ShapedRecipe.class)
public abstract class ShapedRecipeMixin {

    @Inject(method = "assemble", at = @At("RETURN"), cancellable = true)
    private void inmis$copyBackpackContents(CraftingInput input, CallbackInfoReturnable<ItemStack> cir) {
        ItemStack result = cir.getReturnValue();
        if (result.isEmpty() || !(result.getItem() instanceof BackpackItem newBackpack)) {
            return;
        }
        if (input.width() != 3 || input.height() != 3) {
            return;
        }

        ItemStack center = input.getItem(4);
        if (!(center.getItem() instanceof BackpackItem)) {
            return;
        }
        BackpackComponent old = center.get(Inmis.BACKPACK_COMPONENT);
        if (old == null) {
            return;
        }

        int size = newBackpack.getTier().getRowWidth() * newBackpack.getTier().getNumberOfRows();
        SimpleContainer container = new SimpleContainer(size);
        int existing = Math.min(size, old.getContainer().getContainerSize());
        for (int i = 0; i < existing; i++) {
            container.setItem(i, old.getContainer().getItem(i).copy());
        }
        result.set(Inmis.BACKPACK_COMPONENT, new BackpackComponent(container));
        cir.setReturnValue(result);
    }
}
