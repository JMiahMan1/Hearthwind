package draylar.inmis.item;

import draylar.inmis.Inmis;
import draylar.inmis.config.BackpackInfo;
import draylar.inmis.menu.BackpackMenu;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class BackpackItem extends Item {

    private final BackpackInfo backpack;

    public BackpackItem(BackpackInfo backpack, Item.Properties settings) {
        super(settings);
        this.backpack = backpack;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        // Aged keeps requireArmorTrinketToOpen false, so right-click opens.
        if (Inmis.CONFIG.requireArmorTrinketToOpen) {
            return InteractionResult.PASS;
        }

        if (Inmis.CONFIG.playSound && level.isClientSide()) {
            BuiltInRegistries.SOUND_EVENT.get(Identifier.parse(backpack.getOpenSound()))
                    .map(holder -> holder.value())
                    .ifPresent(sound -> level.playSound(player, player.blockPosition(), sound,
                            SoundSource.PLAYERS, 1.0F, 1.0F));
        }

        openScreen(player, player.getItemInHand(hand));
        return InteractionResult.SUCCESS;
    }

    public static void openScreen(Player player, ItemStack backpackStack) {
        if (player.level().isClientSide() || backpackStack.isEmpty()) {
            return;
        }
        player.openMenu(new ExtendedMenuProvider<ItemStack>() {
            @Override
            public Component getDisplayName() {
                return Component.translatable(backpackStack.getItem().getDescriptionId());
            }

            @Override
            public ItemStack getScreenOpeningData(ServerPlayer player) {
                return backpackStack;
            }

            @Override
            public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
                return new BackpackMenu(containerId, inventory, backpackStack);
            }
        });
    }

    public BackpackInfo getTier() {
        return backpack;
    }

    /** Item contents must not reset the stack animation when they change. */
    @Override
    public boolean allowComponentsUpdateAnimation(Player player, InteractionHand hand, ItemStack oldStack, ItemStack newStack) {
        return false;
    }
}
