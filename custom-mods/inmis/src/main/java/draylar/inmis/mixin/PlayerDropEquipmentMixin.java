package draylar.inmis.mixin;

import draylar.inmis.Inmis;
import draylar.inmis.item.BackpackItem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.gamerules.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * Spills backpack contents on death when the (default-off) upstream config
 * asks for it. Both flags are false in Aged, so this is inert by default.
 */
@Mixin(Player.class)
public abstract class PlayerDropEquipmentMixin {

    @Inject(method = "dropEquipment", at = @At("HEAD"))
    private void inmis$spillBackpacks(ServerLevel level, CallbackInfo ci) {
        if (level.getGameRules().get(GameRules.KEEP_INVENTORY)) {
            return;
        }
        Player player = (Player) (Object) this;

        if (Inmis.CONFIG.spillArmorBackpacksOnDeath) {
            ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
            if (chest.getItem() instanceof BackpackItem) {
                spill(player, chest);
                player.setItemSlot(EquipmentSlot.CHEST, ItemStack.EMPTY);
            }
        }

        if (Inmis.CONFIG.spillMainBackpacksOnDeath) {
            Inventory inventory = player.getInventory();
            for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
                ItemStack stack = inventory.getItem(slot);
                if (stack.getItem() instanceof BackpackItem) {
                    spill(player, stack);
                    inventory.setItem(slot, ItemStack.EMPTY);
                }
            }
        }
    }

    private static void spill(Player player, ItemStack backpack) {
        List<ItemStack> contents = Inmis.getBackpackContents(backpack);
        if (contents != null) {
            for (ItemStack stack : contents) {
                if (!stack.isEmpty()) {
                    player.drop(stack.copy(), true, false);
                }
            }
        }
        Inmis.wipeBackpack(backpack);
        player.drop(backpack.copy(), true, false);
    }
}
