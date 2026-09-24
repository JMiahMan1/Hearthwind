package net.adventurez.entity.render;

import java.util.List;

import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionHand;

public class AdventureRenderState extends HumanoidRenderState {
    public float limbAngle;
    public float limbDistance;
    public float animationProgress;
    public float yHeadRot;
    public float headPitch;
    public float f;
    public float g;
    public float h;
    public float i;
    public float j;
    public float yBodyRot;
    public int size;
    public int roarTick;
    public int attackTicks;
    public boolean onGround;
    public boolean sitting;
    public boolean spellcasting;
    public float health;
    public float maxHealth;
    public double x;
    public double y;
    public double z;
    public List<Entity> passengers = List.of();
    public ItemStack mainHandItem = ItemStack.EMPTY;
    public SynchedEntityData entityData;
    public Level level;
    public int variant;
    public int backCrystals;
    public int lavaTexture;
    public int inventoryItemId;
    public boolean deepslateVariant;
    public boolean rareVariant;
    public boolean invulnerableShield;
    public boolean halfLifeChange;
    public boolean otherEyes;
    public boolean fireBreathing;
    public boolean flying;
    public boolean clientStartFlying;
    public boolean startFlying;
    public boolean clientEndFlying;
    public boolean hasSaddle;
    public boolean hasChest;
    public boolean voidOrb;
    public boolean openMouth;
    public boolean tamed;
    public float deathProgress;
    public float beamTime;
    public float beamScale;
    public net.minecraft.world.phys.Vec3 beamTarget;
    public final net.minecraft.client.renderer.item.ItemStackRenderState inventoryItem = new net.minecraft.client.renderer.item.ItemStackRenderState();

    public void capture(Entity entity, float partialTicks) {
        this.entityData = entity.getEntityData();
        this.level = entity.level();
        this.x = entity.getX();
        this.y = entity.getY();
        this.z = entity.getZ();
        this.yBodyRot = entity.getYRot();
        this.onGround = entity.onGround();
        this.passengers = entity.getPassengers();
        if (entity instanceof LivingEntity living) {
            this.isBaby = living.isBaby();
            this.health = living.getHealth();
            this.maxHealth = living.getMaxHealth();
        }
        this.limbAngle = this.walkAnimationPos;
        this.limbDistance = this.walkAnimationSpeed;
        this.animationProgress = this.ageInTicks;
        this.yHeadRot = this.yRot;
        this.headPitch = this.xRot;
        this.f = this.limbAngle;
        this.g = this.limbDistance;
        this.h = this.animationProgress;
        this.i = this.yHeadRot;
        this.j = this.headPitch;
    }

    public SynchedEntityData getEntityData() {
        return this.entityData;
    }

    public Level level() {
        return this.level;
    }

    public boolean isBaby() {
        return this.isBaby;
    }

    public boolean onGround() {
        return this.onGround;
    }

    public boolean isInSittingPose() {
        return this.sitting;
    }

    public List<Entity> getPassengers() {
        return this.passengers;
    }

    public int getSize() {
        return this.size;
    }

    public float getHealth() {
        return this.health;
    }

    public float getMaxHealth() {
        return this.maxHealth;
    }

    public boolean isSpellcasting() {
        return this.spellcasting;
    }

    public int getRoarTick() {
        return this.roarTick;
    }

    public double getX() {
        return this.x;
    }

    public double getY() {
        return this.y;
    }

    public double getZ() {
        return this.z;
    }

    public ItemStack getItemInHand(InteractionHand hand) {
        return this.mainHandItem;
    }

    public ItemStack getMainHandItem() {
        return this.mainHandItem;
    }
}
