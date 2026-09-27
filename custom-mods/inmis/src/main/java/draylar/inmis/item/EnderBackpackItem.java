package draylar.inmis.item;

import draylar.inmis.Inmis;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class EnderBackpackItem extends Item {

    public static final Component CONTAINER_NAME = Component.translatable("container.enderchest");

    public EnderBackpackItem(Item.Properties settings) {
        super(settings);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (Inmis.CONFIG.playSound && level.isClientSide()) {
            level.playSound(player, player.blockPosition(), SoundEvents.ENDER_CHEST_OPEN,
                    SoundSource.PLAYERS, 1.0F, 1.0F);
        }

        if (!level.isClientSide()) {
            player.openMenu(new SimpleMenuProvider(
                    (containerId, inventory, p) -> ChestMenu.threeRows(containerId, inventory, player.getEnderChestInventory()),
                    CONTAINER_NAME));
            player.awardStat(Stats.OPEN_ENDERCHEST);
        }

        return InteractionResult.SUCCESS;
    }
}
