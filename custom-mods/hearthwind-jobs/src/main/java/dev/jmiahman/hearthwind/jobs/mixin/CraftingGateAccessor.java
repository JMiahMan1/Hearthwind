package dev.jmiahman.hearthwind.jobs.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ResultSlot;

@Mixin(ResultSlot.class)
public interface CraftingGateAccessor {
    @Accessor("player")
    Player hearthwind_jobs$getPlayer();
}
