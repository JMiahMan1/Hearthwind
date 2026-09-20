package net.dungeonz.mixin;

import net.dungeonz.init.DimensionInit;
import net.dungeonz.util.DungeonHelper;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.registries.BuiltInRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class PlayerEntityMixin extends LivingEntity {

    @Shadow
    public abstract boolean isCreative();

    public PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, Level world) {
        super(entityType, world);
    }

    @Inject(method = "dropEquipment", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/gamerules/GameRules;get(Lnet/minecraft/world/level/gamerules/GameRule;)Ljava/lang/Object;"), cancellable = true)
    protected void dropInventoryMixin(ServerLevel world, CallbackInfo info) {
        if (!this.level().isClientSide() && !this.isCreative() && this.level().dimension() == DimensionInit.DUNGEON_WORLD
                && DungeonHelper.getCurrentDungeon((ServerPlayer) (Object) this).isKeepInventory()) {
            info.cancel();
        }
    }

    @Inject(method = "blockActionRestricted", at = @At(value = "HEAD"), cancellable = true)
    private void isBlockBreakingRestrictedMixin(Level world, BlockPos pos, GameType gameMode, CallbackInfoReturnable<Boolean> info) {
        if (!world.isClientSide() && !this.isCreative() && this.level().dimension() == DimensionInit.DUNGEON_WORLD
                && !DungeonHelper.getCurrentDungeon((ServerPlayer) (Object) this).getBreakableBlockIdList().contains(BuiltInRegistries.BLOCK.getKey(world.getBlockState(pos).getBlock()))) {
            info.setReturnValue(true);

        }
    }

    @Inject(method = "mayUseItemAt", at = @At(value = "HEAD"), cancellable = true)
    public void canPlaceOnMixin(BlockPos pos, Direction facing, ItemStack stack, CallbackInfoReturnable<Boolean> info) {
        Player playerEntity = (Player) (Object) this;
        if (playerEntity != null && !playerEntity.isCreative() && this.level().dimension() == DimensionInit.DUNGEON_WORLD) {
            info.setReturnValue(false);
        }
    }

    @Override
    public boolean randomTeleport(double x, double y, double z, boolean particleEffects) {
        if (this.level().dimension() == DimensionInit.DUNGEON_WORLD) {
            return false;
        }
        return super.randomTeleport(x, y, z, particleEffects);
    }

    @Override
    public boolean addEffect(MobEffectInstance effect, Entity source) {
        if (!this.level().isClientSide() && effect.getEffect().value().isBeneficial() && this.level().dimension() == DimensionInit.DUNGEON_WORLD) {
            if (DungeonHelper.getCurrentDungeon((ServerPlayer) (Object) this) != null && !DungeonHelper.getCurrentDungeon((ServerPlayer) (Object) this).isPositiveEffectsAllowed()) {
                return false;
            }
        }
        return super.addEffect(effect, source);
    }

}
