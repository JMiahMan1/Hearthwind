package dev.jmiahman.hearthwind.skills.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ItemCombinerMenu;
import net.minecraft.world.inventory.ResultContainer;

@Mixin(ItemCombinerMenu.class)
public interface ItemCombinerMenuAccessor {
    @Accessor("player")
    Player hearthwind$getPlayer();

    @Accessor("resultSlots")
    ResultContainer hearthwind$getResultSlots();
}
