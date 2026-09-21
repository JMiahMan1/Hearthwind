package net.satisfy.meadow.fabric.core.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.satisfy.meadow.core.entity.WoolyCowEntity;
import net.satisfy.meadow.core.entity.WoolyCowVariant;
import net.satisfy.meadow.core.registry.EntityTypeRegistry;
import net.satisfy.meadow.core.registry.TagRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NetherPortalBlock.class)
public class NetherPortalBlockMixin {

    @Inject(method = "randomTick", at = @At("HEAD"))
    private void checkCannotConnect(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource, CallbackInfo ci) {
        // 26.2: RULE_DOMOBSPAWNING split up; SPAWN_MOBS is the general mob-spawning gate.
        if (serverLevel.dimension() == net.minecraft.world.level.Level.NETHER && serverLevel.getRandom().nextFloat() < 0.005F && serverLevel.getBiome(blockPos).is(TagRegistry.SPAWNS_WARPED_COW) && serverLevel.getGameRules().get(GameRules.SPAWN_MOBS)) {
            while (serverLevel.getBlockState(blockPos).is(Blocks.NETHER_PORTAL)) {
                blockPos = blockPos.below();
            }

            if (net.minecraft.world.entity.SpawnPlacements.checkSpawnRules(EntityTypeRegistry.WOOLY_COW.get(), serverLevel, net.minecraft.world.entity.EntitySpawnReason.STRUCTURE, blockPos, serverLevel.getRandom())) {                WoolyCowEntity entity = EntityTypeRegistry.WOOLY_COW.get().spawn(serverLevel, blockPos.above(), EntitySpawnReason.STRUCTURE);
                if (entity != null) {
                    entity.setVariant(WoolyCowVariant.WARPED);
                    entity.setPortalCooldown();
                }
            }
        }
    }
}
