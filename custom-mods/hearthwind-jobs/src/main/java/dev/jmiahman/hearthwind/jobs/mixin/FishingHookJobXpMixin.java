package dev.jmiahman.hearthwind.jobs.mixin;

import dev.jmiahman.hearthwind.jobs.JobEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Caught fish pay fisher job XP. The redirect sits on the tag check that
 * vanilla already performs for the FISH_CAUGHT statistic, so the hook only
 * fires on real catches and the return value is unchanged.
 */
@Mixin(FishingHook.class)
public abstract class FishingHookJobXpMixin {
    @Redirect(method = "retrieve", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/tags/TagKey;)Z"))
    private boolean hearthwind$fishingXp(ItemStack stack, TagKey<Item> tag) {
        boolean caught = stack.is(tag);
        if (caught && ((FishingHook) (Object) this).getPlayerOwner() instanceof ServerPlayer sp) {
            JobEvents.awardItem(sp, stack);
        }
        return caught;
    }
}
