package net.satisfy.farm_and_charm.fabric.core.mixin;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.satisfy.farm_and_charm.core.entity.ai.ApproachFeedingTroughGoal;
import net.satisfy.farm_and_charm.core.entity.ai.ApproachWaterTroughGoal;
import net.satisfy.farm_and_charm.core.network.PacketHandler;
import net.satisfy.farm_and_charm.core.network.packet.SyncSaturationPacket;
import net.satisfy.farm_and_charm.core.util.SaturationTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Animal.class)
public abstract class AnimalEntityMixin extends Mob implements SaturationTracker.SaturatedAnimal {

    @Unique
    private SaturationTracker farm_and_charm$saturation;

    @Unique
    private int farm_and_charm$lastSyncedSaturationLevel = -1;

    @Unique
    private int farm_and_charm$lastSyncedFoodCounter = -1;

    protected AnimalEntityMixin(EntityType<? extends Mob> entityType, Level world) {
        super(entityType, world);
    }

    @Override
    public SaturationTracker farm_and_charm$getSaturationTracker() {
        if (farm_and_charm$saturation == null) {
            farm_and_charm$saturation = new SaturationTracker();
        }
        return farm_and_charm$saturation;
    }

    @Override
    public void farm_and_charm$setSaturationTracker(SaturationTracker tracker) {
        this.farm_and_charm$saturation = tracker;
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void farm_and_charm$addSelfFeedingGoal(EntityType<? extends Animal> entityType, Level level, CallbackInfo ci) {
        if (!level.isClientSide()) {
            this.goalSelector.addGoal(3, new ApproachFeedingTroughGoal((Animal) (Object) this, 1.2D));
            this.goalSelector.addGoal(3, new ApproachWaterTroughGoal((Animal) (Object) this, 1.2D));
        }
    }

    @Inject(method = "aiStep", at = @At("HEAD"))
    private void farm_and_charm$tickSaturation(CallbackInfo ci) {
        if (!this.level().isClientSide()) {
            EntityType<?> type = this.getType();
            if (!(type == EntityTypes.COW || type == EntityTypes.PIG || type == EntityTypes.SHEEP || type == EntityTypes.CHICKEN)) return;

            SaturationTracker tracker = farm_and_charm$getSaturationTracker();
            tracker.tick((Animal)(Object)this);

            int saturationLevel = tracker.level();
            int foodCounter = tracker.foodCounter();

            if (saturationLevel == farm_and_charm$lastSyncedSaturationLevel && foodCounter == farm_and_charm$lastSyncedFoodCounter) {
                return;
            }

            farm_and_charm$lastSyncedSaturationLevel = saturationLevel;
            farm_and_charm$lastSyncedFoodCounter = foodCounter;

            SyncSaturationPacket packet = new SyncSaturationPacket(this.getId(), saturationLevel, foodCounter);
            PacketHandler.sendSaturationSync(packet, this);
        }
    }

    @Inject(method = "mobInteract", at = @At("HEAD"), cancellable = true)
    private void farm_and_charm$injectSaturationFeeding(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        EntityType<?> type = this.getType();
        if (!(type == EntityTypes.COW || type == EntityTypes.PIG || type == EntityTypes.SHEEP || type == EntityTypes.CHICKEN)) return;

        Animal animal = (Animal)(Object)this;
        ItemStack stack = player.getItemInHand(hand);

        if (!animal.isFood(stack) || animal.isBaby()) return;
        if (animal.canFallInLove()) return;

        SaturationTracker tracker = farm_and_charm$getSaturationTracker();
        tracker.tryFeed(animal, player, hand);

        if (!animal.level().isClientSide()) {
            int saturationLevel = tracker.level();
            int foodCounter = tracker.foodCounter();

            farm_and_charm$lastSyncedSaturationLevel = saturationLevel;
            farm_and_charm$lastSyncedFoodCounter = foodCounter;

            SyncSaturationPacket packet = new SyncSaturationPacket(this.getId(), saturationLevel, foodCounter);
            PacketHandler.sendSaturationSync(packet, this);

            ((ServerLevel)animal.level()).sendParticles(ParticleTypes.HAPPY_VILLAGER, animal.getX(), animal.getY() + 1.0, animal.getZ(), 5, 0.2, 0.2, 0.2, 0.05);
        }

        cir.setReturnValue((animal.level().isClientSide() ? InteractionResult.SUCCESS_SERVER : InteractionResult.SUCCESS));
    }

    @Inject(method = "addAdditionalSaveData", at = @At("HEAD"))
    private void farm_and_charm$saveSaturation(net.minecraft.world.level.storage.ValueOutput output, CallbackInfo ci) {
        SaturationTracker tracker = farm_and_charm$getSaturationTracker();
        net.minecraft.world.level.storage.ValueOutput trackerOut = output.child("FarmAndCharmSaturation");
        trackerOut.putInt("SaturationLevel", tracker.level());
        trackerOut.putInt("SaturationCounter", tracker.foodCounter());
        trackerOut.putLong("SaturationLastFed", tracker.getLastFedTick());
        trackerOut.putInt("SaturationDecayDelay", tracker.getDecayDelay());
    }

    @Inject(method = "readAdditionalSaveData", at = @At("HEAD"))
    private void farm_and_charm$loadSaturation(net.minecraft.world.level.storage.ValueInput input, CallbackInfo ci) {
        net.minecraft.world.level.storage.ValueInput trackerIn = input.childOrEmpty("FarmAndCharmSaturation");
        SaturationTracker tracker = new SaturationTracker();
        tracker.setLevel(trackerIn.getIntOr("SaturationLevel", 0));
        tracker.setFoodCounter(trackerIn.getIntOr("SaturationCounter", 0));
        tracker.setLastFedTick(trackerIn.getLongOr("SaturationLastFed", -1L));
        tracker.setDecayDelay(trackerIn.getIntOr("SaturationDecayDelay", -1));
        farm_and_charm$setSaturationTracker(tracker);
    }
}