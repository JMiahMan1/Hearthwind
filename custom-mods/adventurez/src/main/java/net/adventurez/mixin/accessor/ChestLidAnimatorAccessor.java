package net.adventurez.mixin.accessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.world.level.block.entity.ChestLidController;

@Mixin(ChestLidController.class)
public interface ChestLidAnimatorAccessor {

    @Accessor("shouldBeOpen")
    boolean getOpen();
}
