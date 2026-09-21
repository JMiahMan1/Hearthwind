package net.satisfy.farm_and_charm.fabric.core.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.animal.chicken.Chicken;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.gameevent.GameEvent;
import net.satisfy.farm_and_charm.core.block.entity.StorageBlockEntity;
import net.satisfy.farm_and_charm.core.entity.ChickenCoopAccess;
import net.satisfy.farm_and_charm.core.entity.ai.ChickenGotoAndEnterCoopGoal;
import net.satisfy.farm_and_charm.core.entity.ai.ChickenLocateCoopGoal;
import net.satisfy.farm_and_charm.core.registry.ObjectRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Chicken.class)
public class ChickenMixin implements ChickenCoopAccess {
    @Unique
    private BlockPos farmAndCharm$coopTarget;
    @Unique
    private int farmAndCharm$coopCooldown = 0;

    @Override
    public BlockPos farmAndCharm$getCoopTarget() {
        return farmAndCharm$coopTarget;
    }

    @Override
    public void farmAndCharm$setCoopTarget(BlockPos pos) {
        this.farmAndCharm$coopTarget = pos;
    }

    @Override
    public void farmAndCharm$clearCoopTarget() {
        this.farmAndCharm$coopTarget = null;
    }

    @Override
    public boolean farmAndCharm$hasCoopTarget() {
        return this.farmAndCharm$coopTarget != null;
    }

    @Override
    public int farmAndCharm$getCoopCooldown() {
        return this.farmAndCharm$coopCooldown;
    }

    @Override
    public void farmAndCharm$setCoopCooldown(int cooldown) {
        this.farmAndCharm$coopCooldown = cooldown;
    }

    @Inject(method = "aiStep", at = @At("HEAD"))
    private void farmAndCharm$tickCoopCooldown(CallbackInfo ci) {
        if (farmAndCharm$coopCooldown > 0) farmAndCharm$coopCooldown--;
    }

    @Inject(method = "registerGoals", at = @At("TAIL"))
    private void addCustomGoals(CallbackInfo ci) {
        Chicken chicken = (Chicken) (Object) this;
        GoalSelector goalSelector = ((MobAccessor) chicken).farmAndCharm$getGoalSelector();
        goalSelector.addGoal(8, new ChickenLocateCoopGoal(chicken));
        goalSelector.addGoal(9, new ChickenGotoAndEnterCoopGoal(chicken));
    }

    // 26.2: egg laying goes through the CHICKEN_LAY gift loot table instead
    // of spawnAtLocation. Same nest-deposit behavior as before.
    @Redirect(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/animal/chicken/Chicken;dropFromGiftLootTable(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/resources/ResourceKey;Ljava/util/function/BiConsumer;)Z"))
    private boolean redirectEggLaying(Chicken instance, net.minecraft.server.level.ServerLevel level,
            net.minecraft.resources.ResourceKey<net.minecraft.world.level.storage.loot.LootTable> lootTable,
            java.util.function.BiConsumer<net.minecraft.server.level.ServerLevel, ItemStack> original) {
        if (!lootTable.equals(net.minecraft.world.level.storage.loot.BuiltInLootTables.CHICKEN_LAY))
            return instance.dropFromGiftLootTable(level, lootTable, original);

        if (instance.isBaby() || !instance.isAlive() || instance.isChickenJockey()) {
            return instance.dropFromGiftLootTable(level, lootTable, original);
        }

        BlockPos origin = instance.blockPosition();
        for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-6, -2, -6), origin.offset(6, 2, 6))) {
            if (level.getBlockState(pos).is(ObjectRegistry.CHICKEN_NEST.get())) {
                BlockEntity be = level.getBlockEntity(pos);
                if (be instanceof StorageBlockEntity storage) {
                    for (int i = 0; i < storage.getInventory().size(); i++) {
                        if (storage.getInventory().get(i).isEmpty()) {
                            storage.getInventory().set(i, new ItemStack(Items.EGG));
                            storage.setChanged();
                            level.getChunkAt(pos).markUnsaved();
                            level.sendBlockUpdated(pos, level.getBlockState(pos), level.getBlockState(pos), 3);
                            instance.gameEvent(GameEvent.ENTITY_PLACE);
                            return true;
                        }
                    }
                }
            }
        }

        return instance.dropFromGiftLootTable(level, lootTable, original);
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void farmAndCharm$saveCoopData(net.minecraft.world.level.storage.ValueOutput output, CallbackInfo ci) {
        output.putInt("CoopCooldown", farmAndCharm$coopCooldown);
        if (farmAndCharm$coopTarget != null) {
            output.putInt("CoopTargetX", farmAndCharm$coopTarget.getX());
            output.putInt("CoopTargetY", farmAndCharm$coopTarget.getY());
            output.putInt("CoopTargetZ", farmAndCharm$coopTarget.getZ());
        }
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void farmAndCharm$loadCoopData(net.minecraft.world.level.storage.ValueInput input, CallbackInfo ci) {
        farmAndCharm$coopCooldown = input.getIntOr("CoopCooldown", 0);
        int x = input.getIntOr("CoopTargetX", Integer.MIN_VALUE);
        int y = input.getIntOr("CoopTargetY", Integer.MIN_VALUE);
        int z = input.getIntOr("CoopTargetZ", Integer.MIN_VALUE);
        if (x != Integer.MIN_VALUE && y != Integer.MIN_VALUE && z != Integer.MIN_VALUE) {
            farmAndCharm$coopTarget = new BlockPos(x, y, z);
        }
    }
}
