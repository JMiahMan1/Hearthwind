package net.satisfy.farm_and_charm.fabric.core.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.animal.feline.Cat;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.satisfy.farm_and_charm.core.block.entity.PetBowlBlockEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.attribute.EnvironmentAttribute;
import net.minecraft.world.attribute.EnvironmentAttributeSystem;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets = "net.minecraft.world.entity.animal.feline.Cat$CatRelaxOnOwnerGoal")
public class CatGiftChanceMixin {

    @Shadow @Final
    private Cat cat;

    // 26.2: the 0.7 morning-gift chance is now the CAT_WAKING_UP_GIFT_CHANCE
    // environment attribute. Same fed-from-bowl behavior: guarantee the gift.
    @Redirect(method = "stop", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/attribute/EnvironmentAttributeSystem;getValue(Lnet/minecraft/world/attribute/EnvironmentAttribute;Lnet/minecraft/world/phys/Vec3;)Ljava/lang/Object;"))
    private Object increaseGiftChanceIfFed(EnvironmentAttributeSystem system, EnvironmentAttribute attribute, Vec3 pos) {
        Object base = system.getValue(attribute, pos);
        if (attribute != EnvironmentAttributes.CAT_WAKING_UP_GIFT_CHANCE) return base;
        if (!(cat.level() instanceof ServerLevel serverLevel)) return base;

        BlockPos catPos = cat.blockPosition();
        for (BlockPos check : BlockPos.betweenClosed(catPos.offset(-4, -2, -4), catPos.offset(4, 2, 4))) {
            BlockEntity be = serverLevel.getBlockEntity(check);
            if (be instanceof PetBowlBlockEntity bowl && bowl.wasCatFed()) {
                bowl.resetFedFlags();
                return 1.0F;
            }
        }

        return base;
    }
}