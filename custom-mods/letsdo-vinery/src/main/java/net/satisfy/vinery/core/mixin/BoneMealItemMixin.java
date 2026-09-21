package net.satisfy.vinery.core.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.satisfy.vinery.core.Vinery;
import net.satisfy.vinery.core.registry.ArmorRegistry;
import net.satisfy.vinery.core.item.WinemakerBootsItem;
import net.satisfy.vinery.core.item.WinemakerChestItem;
import net.satisfy.vinery.core.item.WinemakerHelmetItem;
import net.satisfy.vinery.core.item.WinemakerLegsItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;
@Mixin(BoneMealItem.class)
public abstract class BoneMealItemMixin {

    @Inject(method = "useOn", at = @At("RETURN"), cancellable = true)
    public void useOnBlock(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {
        if (cir.getReturnValue() != InteractionResult.CONSUME) {
            return;
        }

        if (!(context.getLevel() instanceof ServerLevel)) {
            return;
        }

        Player player = context.getPlayer();
        if (player == null) return;

        ItemStack helmet = player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.HEAD);
        ItemStack chestplate = player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.CHEST);
        ItemStack leggings = player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.LEGS);
        ItemStack boots = player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.FEET);

        boolean hasFullSet = helmet.getItem() instanceof WinemakerHelmetItem &&
                chestplate.getItem() instanceof WinemakerChestItem &&
                leggings.getItem() instanceof WinemakerLegsItem &&
                boots.getItem() instanceof WinemakerBootsItem;

        if (hasFullSet) {
            System.out.println("YES");
            cir.setReturnValue(InteractionResult.PASS);
            ItemStack heldItem = context.getItemInHand();
            if (!heldItem.isEmpty()) {
                heldItem.grow(1);
            }

            for (net.minecraft.world.entity.EquipmentSlot armorSlot : new net.minecraft.world.entity.EquipmentSlot[]{net.minecraft.world.entity.EquipmentSlot.HEAD, net.minecraft.world.entity.EquipmentSlot.CHEST, net.minecraft.world.entity.EquipmentSlot.LEGS, net.minecraft.world.entity.EquipmentSlot.FEET}) {
                ItemStack armorPiece = player.getItemBySlot(armorSlot);
                if (!armorPiece.isEmpty()) {
                    armorPiece.hurtAndBreak(2, player, armorSlot);
                }
            }
        }
    }
}