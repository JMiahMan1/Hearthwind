package dev.jmiahman.hearthwind.primitive.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.jmiahman.hearthwind.primitive.tiered.ReforgeRegistry;
import dev.jmiahman.hearthwind.primitive.tiered.TierDefinition;
import dev.jmiahman.hearthwind.primitive.tiered.TierRegistry;
import dev.jmiahman.hearthwind.primitive.tiered.TieredData;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

@Mixin(AnvilMenu.class)
public abstract class AnvilMenuTieredMixin extends AbstractContainerMenu {

    @Shadow
    @Final
    private DataSlot cost;

    protected AnvilMenuTieredMixin(MenuType<?> type, int containerId) {
        super(type, containerId);
    }

    @Inject(method = "createResult", at = @At("RETURN"))
    private void hearthwind$reforgeEquipment(CallbackInfo ci) {
        if (this.slots.size() < 3) {
            return;
        }
        ItemStack base = this.getSlot(0).getItem();
        ItemStack ingredient = this.getSlot(1).getItem();
        ItemStack currentResult = this.getSlot(2).getItem();

        if (base.isEmpty() || ingredient.isEmpty()) {
            return;
        }

        if (currentResult.isEmpty() && ReforgeRegistry.canReforge(base, ingredient)) {
            ItemStack reforged = base.copy();
            RandomSource random = RandomSource.create();

            TierDefinition newTier = TierRegistry.rollTier(reforged, random);
            if (newTier != null) {
                String currentTierId = TieredData.getTierId(base);
                if (currentTierId != null && newTier.id().toString().equals(currentTierId)) {
                    TierDefinition secondRoll = TierRegistry.rollTier(reforged, random);
                    if (secondRoll != null) {
                        newTier = secondRoll;
                    }
                }
                TieredData.setTier(reforged, newTier);
                this.cost.set(2); // 2 XP levels
                this.getSlot(2).set(reforged);
            }
        }
    }
}
