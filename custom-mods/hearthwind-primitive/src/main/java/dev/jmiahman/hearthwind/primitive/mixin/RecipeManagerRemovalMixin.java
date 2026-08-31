package dev.jmiahman.hearthwind.primitive.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeMap;

import dev.jmiahman.hearthwind.primitive.RecipeRemovals;

/**
 * Strips the removed recipes out of the map the recipe manager installs, so
 * they are gone from crafting, from the furnace, and from the recipe book on
 * both sides (the client keeps its own recipe manager).
 *
 * <p>The removal list is (re)loaded here rather than on a lifecycle event:
 * the recipe reload runs before SERVER_STARTING, and the handler receives the
 * resource manager the recipes came from, so there is no ordering hazard.
 */
@Mixin(RecipeManager.class)
public class RecipeManagerRemovalMixin {
    @ModifyVariable(
            method = "apply(Lnet/minecraft/world/item/crafting/RecipeMap;"
                    + "Lnet/minecraft/server/packs/resources/ResourceManager;"
                    + "Lnet/minecraft/util/profiling/ProfilerFiller;)V",
            at = @At("HEAD"),
            index = 1,
            argsOnly = true)
    private RecipeMap hearthwind$withoutRemovedRecipes(RecipeMap value, RecipeMap map,
            ResourceManager resources, ProfilerFiller profiler) {
        RecipeRemovals.load(resources);
        return RecipeRemovals.filter(value);
    }
}
