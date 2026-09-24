package net.adventurez.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.adventurez.entity.AmethystGolemEntity;
import net.adventurez.init.ConfigInit;
import net.adventurez.init.EntityInit;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.GeodeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.GeodeConfiguration;

@Mixin(GeodeFeature.class)
public class GeodeFeatureMixin {

    @Inject(method = "place", at = @At(value = "RETURN"))
    private void generateMixin(FeaturePlaceContext<GeodeConfiguration> context, CallbackInfoReturnable<Boolean> info) {
        if (!context.level().isClientSide() && info.getReturnValue()) {
            int amethystSpawnChance = ConfigInit.CONFIG.amethyst_golem_spawn_chance;
            if (amethystSpawnChance != 0) {
                int spawnChanceInt = context.random().nextInt(amethystSpawnChance) + 1;
                BlockPos spawnPos = context.origin().south(4).east(4);
                if (spawnChanceInt == 1 && (context.level().getBlockState(spawnPos).isAir() || context.level().getBlockState(spawnPos.above()).isAir())) {
                    AmethystGolemEntity amethystGolemEntity = (AmethystGolemEntity) EntityInit.AMETHYST_GOLEM.create(context.level().getLevel(), net.minecraft.world.entity.EntitySpawnReason.COMMAND);
                    if (amethystGolemEntity != null) {
                        amethystGolemEntity.getEntityData().set(AmethystGolemEntity.DEEPSLATE_VARIANT, spawnPos.getY() <= 0);
                        if (!context.level().getBlockState(spawnPos).isAir()) {
                            spawnPos = spawnPos.above();
                        }
                        amethystGolemEntity.setPos(spawnPos.getX() + 0.5D, spawnPos.getY(), spawnPos.getZ() + 0.5D);
                        amethystGolemEntity.setYRot(context.random().nextFloat() * 360F);
                        amethystGolemEntity.setXRot(0.0F);
                        context.level().addFreshEntity(amethystGolemEntity);
                    }
                }
            }
        }
    }
}
