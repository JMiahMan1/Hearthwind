package earth.terrarium.chipped.common.menus;

import net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;

public record WorkbenchMenuProvider(BlockPos pos, Component name) implements ExtendedMenuProvider<BlockPos> {
    @Override
    public Component getDisplayName() {
        return name;
    }

    @Override
    public BlockPos getScreenOpeningData(ServerPlayer player) {
        return pos;
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new WorkbenchMenu(id, inventory, pos);
    }
}
