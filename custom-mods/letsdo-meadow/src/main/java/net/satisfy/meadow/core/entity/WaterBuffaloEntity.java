package net.satisfy.meadow.core.entity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.cow.Cow;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.level.Level;
import net.satisfy.meadow.core.registry.EntityTypeRegistry;
import net.satisfy.meadow.core.registry.ObjectRegistry;
import net.satisfy.meadow.core.registry.SoundEventRegistry;
import org.jetbrains.annotations.NotNull;


public class WaterBuffaloEntity extends Cow {
    public WaterBuffaloEntity(EntityType<? extends Cow> entityType, Level world) {
        super(entityType, world);
    }

    @Override
    public Cow getBreedOffspring(ServerLevel serverWorld, AgeableMob passiveEntity) {
        return EntityTypeRegistry.WATER_BUFFALO.get().create(serverWorld, net.minecraft.world.entity.EntitySpawnReason.BREEDING);
    }

    @Override
    public @NotNull InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        if (itemStack.is(ObjectRegistry.WOODEN_BUCKET.get()) && !this.isBaby()) {
            player.playSound(SoundEvents.COW_MILK, 1.0F, 1.0F);
            ItemStack itemStack2 = ItemUtils.createFilledResult(itemStack, player, ObjectRegistry.WOODEN_BUFFALO_MILK_BUCKET.get().getDefaultInstance());
            player.setItemInHand(hand, itemStack2);
            return (this.level().isClientSide() ? InteractionResult.SUCCESS : InteractionResult.CONSUME);
        } else {
            return super.mobInteract(player, hand);
        }
    }

    @Override
    protected @NotNull SoundEvent getAmbientSound() {
        return SoundEventRegistry.BUFFALO_AMBIENT.get();
    }

    @Override
    protected @NotNull SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEventRegistry.BUFFALO_HURT.get();
    }

    @Override
    protected @NotNull SoundEvent getDeathSound() {
        return SoundEventRegistry.BUFFALO_DEATH.get();
    }


    @Override
    public int getMaxHeadYRot() {
        return 35;
    }
}