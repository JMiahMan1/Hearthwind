package dev.jmiahman.hearthwind.primitive.mixin;

import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Read access to furnace cook timing for gametests. */
@Mixin(AbstractFurnaceBlockEntity.class)
public interface AbstractFurnaceBlockEntityTimingsAccessor {

    @Accessor("cookingTimer")
    int hearthwind$getCookingTimer();

    @Accessor("cookingTotalTime")
    int hearthwind$getCookingTotalTime();

    @Accessor("litTotalTime")
    int hearthwind$getLitTotalTime();
}
