package net.adventurez.mixin.accessor;

import com.google.common.collect.ImmutableList;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.world.entity.Entity;

@Mixin(Entity.class)
public interface EntityAccessor {

    @Accessor("passengers")
    ImmutableList<Entity> getPassengers();
}
