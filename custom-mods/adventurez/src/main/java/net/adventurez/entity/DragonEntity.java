package net.adventurez.entity;

import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.server.level.ServerLevel;

import net.minecraft.world.level.storage.ValueInput;

import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.sounds.SoundSource;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

import com.google.common.collect.UnmodifiableIterator;

import net.adventurez.entity.goal.DragonFindOwnerGoal;
import net.adventurez.entity.goal.DragonFlyRandomlyGoal;
import net.adventurez.entity.goal.DragonSitGoal;
import net.adventurez.entity.nonliving.FireBreathEntity;
import net.adventurez.init.ConfigInit;
import net.adventurez.init.ItemInit;
import net.adventurez.init.SoundInit;
import net.adventurez.init.TagInit;
import net.adventurez.mixin.accessor.LivingEntityAccessor;
import net.adventurez.network.packet.DragonFireBreathPacket;
import net.adventurez.network.packet.VelocityPacket;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.world.entity.vehicle.DismountHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.InteractionHand;
import net.minecraft.resources.Identifier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.Level;

public class DragonEntity extends TamableAnimal implements ContainerListener {

    public static final EntityDataAccessor<Boolean> IS_FLYING;
    public static final EntityDataAccessor<Boolean> IS_START_FLYING;
    public static final EntityDataAccessor<Boolean> CLIENT_START_FLYING;
    public static final EntityDataAccessor<Boolean> CLIENT_END_FLYING;
    public static final EntityDataAccessor<Byte> TAMEABLE_FLAGS;
    public static final EntityDataAccessor<Optional<EntityReference<LivingEntity>>> OWNER_UUID;
    public static final EntityDataAccessor<Boolean> HAS_SADDLE;
    public static final EntityDataAccessor<Boolean> HAS_CHEST;
    public static final EntityDataAccessor<Boolean> OTHER_EARS;
    public static final EntityDataAccessor<Boolean> OTHER_TAIL;
    public static final EntityDataAccessor<Boolean> OTHER_EYES;
    public static final EntityDataAccessor<Integer> DRAGON_SIZE;
    public static final EntityDataAccessor<Boolean> FIRE_BREATH;
    public static final EntityDataAccessor<Boolean> RED_DRAGON;

    private boolean sitting;
    public boolean isFlying;
    private int startFlyingTimer = 0;
    private float dragonSideSpeed = 0.0F;
    private float dragonForwardSpeed = 0.0F;
    public int keyBind = 342;
    private float turningFloat;
    private boolean hasSaddle;
    private int healingFood;
    private SimpleContainer inventory;
    private int onGroundTicker;
    private int dragonAge;
    private int dragonAgeFoodBonus;
    private int fireBreathCooldown;
    public boolean fireBreathActive;
    private int startFlyingTime = 0;
    private int fluidTicker = 0;

    @SuppressWarnings("deprecation")
    public DragonEntity(EntityType<? extends TamableAnimal> entityType, Level level) {
        super(entityType, level);
        this.onChestedStatusChanged();
        this.refreshDimensions();
        this.xpReward = 10;
    }

    public static AttributeSupplier.Builder createDragonAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 60.0D).add(Attributes.ATTACK_DAMAGE, 9.0D).add(Attributes.MOVEMENT_SPEED, 0.28D)
                .add(Attributes.ARMOR, 6.0D).add(Attributes.KNOCKBACK_RESISTANCE, 0.5D);
    }

    @Override
    public void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new DragonSitGoal(this));
        this.goalSelector.addGoal(2, new DragonFindOwnerGoal(this, 0.1D, 10.0F, 2.0F));
        this.goalSelector.addGoal(3, new DragonFlyRandomlyGoal(this));
        this.goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 0.9D));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 10.0F, 0.8F));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
    }

    @Override
    protected void defineSynchedData(Builder builder) {
        super.defineSynchedData(builder);
        builder.define(TAMEABLE_FLAGS, (byte) 0);
        builder.define(OWNER_UUID, Optional.empty());
        builder.define(IS_FLYING, false);
        builder.define(CLIENT_END_FLYING, false);
        builder.define(IS_START_FLYING, false);
        builder.define(CLIENT_START_FLYING, false);
        builder.define(HAS_SADDLE, false);
        builder.define(HAS_CHEST, false);
        builder.define(OTHER_EARS, false);
        builder.define(OTHER_TAIL, false);
        builder.define(OTHER_EYES, false);
        builder.define(DRAGON_SIZE, 1);
        builder.define(FIRE_BREATH, false);
        builder.define(RED_DRAGON, false);
    }

    @Override
    public void addAdditionalSaveData(ValueOutput tag) {
        super.addAdditionalSaveData(tag);
        if (this.getOwnerUuid() != null) {
            tag.store("DragonOwner", net.minecraft.core.UUIDUtil.CODEC, this.getOwnerUuid());
        }
        tag.putBoolean("IsFlying", this.isFlying);
        tag.putBoolean("SittingDragon", this.sitting);
        tag.putBoolean("HasSaddle", this.hasSaddle);
        tag.putBoolean("HasChest", this.hasChest());
        if (this.hasChest()) {
            java.util.List<ItemStack> items = new java.util.ArrayList<>();
            for (int i = 0; i < this.inventory.getContainerSize(); ++i) {
                items.add(this.inventory.getItem(i));
            }
            tag.store("Items", ItemStack.OPTIONAL_CODEC.listOf(), items);
        }
        tag.putInt("DragonSize", this.getSize());
        tag.putBoolean("OtherDragonEars", this.getEntityData().get(DragonEntity.OTHER_EARS));
        tag.putBoolean("OtherDragonTail", this.getEntityData().get(DragonEntity.OTHER_TAIL));
        tag.putBoolean("OtherDragonEyes", this.getEntityData().get(DragonEntity.OTHER_EYES));
        tag.putBoolean("RedDragon", this.getEntityData().get(DragonEntity.RED_DRAGON));
        tag.putInt("DragonAge", this.dragonAge);
        tag.putInt("StartFlyingTime", this.startFlyingTime);
    }

    @Override
    public void readAdditionalSaveData(ValueInput tag) {
        super.readAdditionalSaveData(tag);
        UUID ownerUuid = tag.read("DragonOwner", net.minecraft.core.UUIDUtil.CODEC).orElse(null);
        if (ownerUuid != null) {
            this.setOwnerUuid(ownerUuid);
            this.setTamed(true);
        } else {
            this.setTamed(false);
        }
        this.isFlying = tag.getBooleanOr("IsFlying", false);
        this.entityData.set(IS_FLYING, this.isFlying);
        this.sitting = tag.getBooleanOr("SittingDragon", false);
        this.setSitting(this.sitting);
        this.hasSaddle = tag.getBooleanOr("HasSaddle", false);
        this.entityData.set(HAS_SADDLE, this.hasSaddle);
        this.setHasChest(tag.getBooleanOr("HasChest", false));
        if (this.hasChest()) {
            java.util.List<ItemStack> items = tag.read("Items", ItemStack.OPTIONAL_CODEC.listOf()).orElse(java.util.List.of());
            this.onChestedStatusChanged();
            for (int i = 0; i < Math.min(items.size(), this.inventory.getContainerSize()); ++i) {
                this.inventory.setItem(i, items.get(i));
            }
        }
        this.setSize(tag.getIntOr("DragonSize", 0));
        this.entityData.set(OTHER_EARS, tag.getBooleanOr("OtherDragonEars", false));
        this.entityData.set(OTHER_TAIL, tag.getBooleanOr("OtherDragonTail", false));
        this.entityData.set(OTHER_EYES, tag.getBooleanOr("OtherDragonEyes", false));
        this.entityData.set(RED_DRAGON, tag.getBooleanOr("RedDragon", false));
        this.dragonAge = tag.getIntOr("DragonAge", 0);
        if (this.isFlying) {
            this.startFlyingTime = tag.getIntOr("StartFlyingTime", 0);
        }
    }

    @Override
    public void travel(Vec3 movementInput) {
        if (this.isAlive()) {
            if ((!this.getPassengers().isEmpty()) && this.canBeControlledByRider()) {
                LivingEntity livingEntity = (LivingEntity) this.getControllingPassenger();
                double wrapper = Mth.wrapDegrees(this.yBodyRot - (double) this.getYRot());
                this.setYRot((float) ((double) this.getYRot() + wrapper));
                this.yRotO = this.getYRot();
                this.setXRot(livingEntity.getXRot() * 0.5F);
                this.setYRot(this.getYRot()); this.setXRot(this.getXRot());
                this.yHeadRot = livingEntity.yHeadRot;
                boolean shouldFlyUp = false;
                boolean shouldFlyDown = false;
                shouldFlyUp = livingEntity.isJumping(); // Pressing jump button for going upwards
                if (this.level().isClientSide() && livingEntity instanceof LocalPlayer) {
                    LocalPlayer clientPlayerEntity = (LocalPlayer) livingEntity;
                    shouldFlyDown = InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), this.keyBind);
                    if (clientPlayerEntity.input.keyPresses.left()) {
                        turningFloat -= 0.05F;
                    }
                    if (!clientPlayerEntity.input.keyPresses.left() && turningFloat < 0.0F) {
                        turningFloat = 0.0F;
                    }
                    if (clientPlayerEntity.input.keyPresses.right()) {
                        turningFloat += 0.05F;
                    }
                    if (!clientPlayerEntity.input.keyPresses.right() && turningFloat > 0.0F) {
                        turningFloat = 0.0F;
                    }
                }

                this.setYRot(ConfigInit.CONFIG.heavy_dragon_flight ? this.getYRot() + Mth.wrapDegrees(turningFloat) : livingEntity.getYRot());

                float f = livingEntity.xxa * 0.1F;
                float g = livingEntity.zza * 0.5F;
                float flySpeed = 0.0F;
                float maxForwardSpeed = 0.6F;
                float maxSidewaysSpeed = 0.15F;

                if ((this.dragonForwardSpeed < maxForwardSpeed && livingEntity.zza > 0.0F) || (this.dragonForwardSpeed > maxForwardSpeed * -0.3F && livingEntity.zza < 0.0F)) {
                    this.dragonForwardSpeed += g * 0.04F;
                }
                if ((this.dragonSideSpeed < maxSidewaysSpeed && livingEntity.xxa > 0.0F) || (this.dragonSideSpeed > maxSidewaysSpeed * -1 && livingEntity.xxa < 0.0F)) {
                    this.dragonSideSpeed += f * 0.03F;
                }
                if (livingEntity.xxa == 0.0F) {
                    // this.dragonSideSpeed *= 0.7F;
                    this.dragonSideSpeed = 0.0F;
                }
                if (livingEntity.zza == 0.0F) {
                    // this.dragonForwardSpeed *= 0.7F;
                    this.dragonForwardSpeed = 0.0F;
                }

                // To do, set proper sit position
                if (shouldFlyUp && !this.isFlying && this.startFlyingTimer < 10) {
                    if (this.isInWater() && this.getFluidHeight(FluidTags.WATER) > 0.5D) {
                        if (!this.level().isClientSide() && this.getFirstPassenger() instanceof ServerPlayer) {
                            ServerPlayNetworking.send((ServerPlayer) this.getFirstPassenger(), new VelocityPacket(this.getId(), 0.05F));
                        }
                    } else {
                        this.startFlyingTimer++;
                        this.getEntityData().set(CLIENT_START_FLYING, true);
                        this.getEntityData().set(IS_START_FLYING, true);
                    }
                }
                if (!shouldFlyUp && !this.isFlying && this.startFlyingTimer > 0) {
                    this.startFlyingTimer--;
                    this.getEntityData().set(IS_START_FLYING, false);
                }
                if ((this.startFlyingTimer <= 0 && !this.isFlying) || this.isFlying || this.getEntityData().get(IS_FLYING)) {
                    this.getEntityData().set(CLIENT_START_FLYING, false);
                }

                if (this.startFlyingTimer >= 10 && (!this.isFlying || !this.getEntityData().get(IS_FLYING))) {
                    this.isFlying = true;
                    this.getEntityData().set(IS_FLYING, true);
                    this.startFlyingTimer = 0;
                    this.startFlyingTime = (int) this.level().getGameTime();
                }
                if (this.isFlying && this.onGround()) {// && shouldFlyDown only client: bad
                    this.onGroundTicker++;
                    if (this.onGroundTicker > 3) {
                        this.onGroundTicker = 0;
                        this.isFlying = false;
                        this.getEntityData().set(IS_FLYING, false);
                        this.getEntityData().set(CLIENT_END_FLYING, true);
                    }

                }
                if ((this.isFlying || this.getEntityData().get(IS_FLYING)) && shouldFlyUp) {
                    flySpeed = 0.15F;
                }
                if ((this.isFlying || this.getEntityData().get(IS_FLYING)) && shouldFlyDown) {
                    flySpeed = -0.2F;
                }
                if ((this.isFlying || this.getEntityData().get(IS_FLYING)) && !shouldFlyDown && !shouldFlyUp) {
                    flySpeed *= 0.4F;
                }

                if (this.isClientAuthoritative()) {
                    this.setSpeed((float) this.getAttributeValue(Attributes.MOVEMENT_SPEED));
                    if ((!this.getEntityData().get(IS_FLYING) && !this.isFlying) || (this.isInWater() && this.getFluidHeight(FluidTags.WATER) > 0.2D)) {
                        super.travel(new Vec3((double) f, movementInput.y, (double) g));
                    } else {
                        Vec3 vec3d;
                        if (ConfigInit.CONFIG.heavy_dragon_flight)
                            vec3d = new Vec3((double) this.dragonSideSpeed, movementInput.y + flySpeed, (double) this.dragonForwardSpeed);
                        else {
                            vec3d = new Vec3(livingEntity.xxa * 0.7D, movementInput.y + flySpeed, livingEntity.zza * 0.7D);
                        }
                        // vec3d x,y,z
                        // x is used when left or right key
                        // z is used when forward or backward
                        if (this.getDeltaMovement().horizontalDistance() > 0.01D) {
                            if (vec3d.z < 0.001D && vec3d.z > -0.001D) {
                                this.setDeltaMovement(this.getDeltaMovement().scale(0.95D));
                            }
                        }
                        this.setDeltaMovement(this.getDeltaMovement().add(vec3d.scale(0.05F)));
                        this.move(MoverType.SELF, this.getDeltaMovement());
                        this.setDeltaMovement(this.getDeltaMovement());
                    }
                } else if (livingEntity instanceof Player) {
                    this.setDeltaMovement(Vec3.ZERO);
                }
                this.updateWalkAnimation(0.0F);
            } else {
                if (this.isFlying || this.getEntityData().get(IS_FLYING)) {
                    this.setDeltaMovement(this.getDeltaMovement().add(movementInput.scale(0.03F)));
                    this.move(MoverType.SELF, this.getDeltaMovement());
                    this.setDeltaMovement(this.getDeltaMovement().scale((0.91F)));
                    double wrapper = Mth.wrapDegrees(this.yHeadRot - (double) this.getYRot());
                    this.setYRot((float) ((double) this.getYRot() + wrapper));
                    BlockPos blockPos = this.blockPosition().below(2);
                    if (this.level().getBlockState(blockPos).isFaceSturdy(this.level(), blockPos, Direction.UP)) {
                        this.setDeltaMovement(this.getDeltaMovement().add(0.0D, -0.005D, 0.0D));
                    }
                    if (this.onGround()) {
                        this.getEntityData().set(CLIENT_END_FLYING, true);
                        this.isFlying = false;
                        this.getEntityData().set(IS_FLYING, false);
                    }
                } else {
                    super.travel(movementInput);
                }
            }
        }
        if (this.isFlying && this.startFlyingTime != 0 && (int) (this.level().getGameTime() - this.startFlyingTime) % 25 == 0) {
            this.playWingFlapSound();
        }

    }

    private boolean canBeControlledByRider() {
        return this.getControllingPassenger() instanceof LivingEntity;
    }

    @Override
    public boolean onClimbable() {
        return false;
    }

    @Override
    public boolean canFreeze() {
        return false;
    }

    @Override
    @Nullable
    public LivingEntity getControllingPassenger() {
        return !this.getPassengers().isEmpty() && this.getPassengers().get(0) instanceof LivingEntity ? (LivingEntity) this.getPassengers().get(0) : null;
    }

    @Override
    public Vec3 getDismountLocationForPassenger(LivingEntity passenger) {
        return super.getDismountLocationForPassenger(passenger);
    }

    private void putPlayerOnBack(Player player) {
        if (!this.level().isClientSide()) {
            player.setYRot(this.getYRot());
            player.setXRot(this.getXRot());
            player.startRiding(this);
        }
    }

    public void dragonFireBreath() {
        if (this.fireBreathCooldown <= 60) {
            this.fireBreathCooldown++;
            if (!this.level().isClientSide()) {
                if (this.fireBreathCooldown == 1) {
                    this.level().playSound((Player) null, this, SoundInit.DRAGON_BREATH_EVENT, SoundSource.HOSTILE, 1.0F, 1.0F);
                }
                if (this.fireBreathCooldown % 3 == 0) {
                    Vec3 vec3d = this.calculateViewVector(xRotO, yHeadRot);
                    Vec3 otherVec3d = this.calculateViewVector(xRotO, yBodyRot);
                    vec3d = vec3d.add(otherVec3d);
                    FireBreathEntity fireBreathEntity = new FireBreathEntity(this.level(), this, vec3d);
                    fireBreathEntity.snapTo(this.getX() + vec3d.x * 3D,
                            this.getY() + this.getBoundingBox().getYsize() * 0.65D + (this.getXRot() > 0F ? -this.getXRot() / 40F : -this.getXRot() / 80F), this.getZ() + vec3d.z * 3D,
                            this.getYRot(), this.getXRot());

                    this.level().addFreshEntity(fireBreathEntity);
                }
            } else {
                if (!this.getEntityData().get(FIRE_BREATH)) {
                    this.getEntityData().set(FIRE_BREATH, true);
                }
            }
        } else {
            this.fireBreathActive = false;
        }
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand, Vec3 interactionPoint) {
        ItemStack itemStack = player.getItemInHand(hand);
        Item item = itemStack.getItem();
        if (!this.isBaby() && (!this.getPassengers().isEmpty()))
            return super.interact(player, hand, Vec3.ZERO);

        if (this.level().isClientSide()) {
            boolean bl = this.isOwner(player) || this.isTamed() || this.dragonFood(item) && !this.isTamed();
            return bl ? InteractionResult.CONSUME : InteractionResult.PASS;
        } else {
            // Check for owner
            if (this.isTamed() && this.getSize() > 1 && this.isOwner(player)) {
                if (item == Items.CHEST && !this.hasChest() && this.getSize() > 2) {
                    this.level().playSound((Player) null, this, SoundInit.EQUIP_CHEST_EVENT, SoundSource.NEUTRAL, 0.5F, 1.0F);
                    this.setHasChest(true);
                    if (!player.isCreative()) {
                        itemStack.shrink(1);
                    }
                    return InteractionResult.SUCCESS;
                }
                if (this.dragonFood(item) && this.getHealth() < this.getMaxHealth() && player.isShiftKeyDown()) {
                    if (!player.isCreative()) {
                        itemStack.shrink(1);
                    }
                    this.heal((float) this.healingFood);
                    return InteractionResult.SUCCESS;
                } else if (!player.isShiftKeyDown()) {
                    if (!this.hasSaddle) {
                        if (item == ItemInit.DRAGON_SADDLE) {
                            this.level().playSound((Player) null, this, SoundEvents.HORSE_SADDLE.value(), SoundSource.NEUTRAL, 0.8F, 1.0F);
                            if (!player.isCreative()) {
                                itemStack.shrink(1);
                            }
                            this.hasSaddle = true;
                            this.getEntityData().set(HAS_SADDLE, true);
                            return InteractionResult.SUCCESS;
                        }
                        return InteractionResult.FAIL;
                    }
                    this.setSitting(false);
                    this.putPlayerOnBack(player);
                    return InteractionResult.SUCCESS;
                } else if (this.isInSittingPose()) {
                    this.setSitting(false);
                    return InteractionResult.SUCCESS;
                } else if (this.onGround()) {
                    this.setSitting(true);
                    return InteractionResult.SUCCESS;
                } else
                    return InteractionResult.PASS;
            } else if (this.dragonFood(item)) {
                if (!this.isTamed()) {
                    if (!player.isCreative()) {
                        itemStack.shrink(1);
                    }
                    int tamer;
                    if (item == ItemInit.ORC_SKIN) {
                        tamer = 1;
                    } else {
                        tamer = healingFood;
                    }
                    if (this.random.nextInt(tamer) == 0) {
                        this.setOwner(player);
                        this.navigation.stop();
                        this.setTarget((LivingEntity) null);
                        this.level().broadcastEntityEvent(this, (byte) 7);
                    } else {
                        this.level().broadcastEntityEvent(this, (byte) 6);
                    }

                    return InteractionResult.SUCCESS;
                } else {
                    if (this.canEatFood(this.dragonAge, 3, 13)) {
                        if (!player.isCreative()) {
                            itemStack.shrink(1);
                        }
                        dragonAgeFoodBonus++;
                        return InteractionResult.SUCCESS;
                    } else {
                        return InteractionResult.PASS;
                    }

                }
            }

            return super.interact(player, hand, Vec3.ZERO);
        }
    }

    private boolean dragonFood(Item item) {
        if (item == ItemInit.ORC_SKIN) {
            healingFood = 6;
            return true;
        } else if (item == Items.PORKCHOP || item == Items.BEEF || item == ItemInit.MAMMOTH_MEAT || item == ItemInit.RAW_VENISON || item == ItemInit.RHINO_MEAT || item == ItemInit.WARTHOG_MEAT
                || item == ItemInit.ENDER_WHALE_MEAT) {
            healingFood = 5;
            return true;
        } else if (item == Items.MUTTON || item == Items.CHICKEN || item == ItemInit.IGUANA_MEAT) {
            healingFood = 4;
            return true;
        } else if (item == Items.RABBIT) {
            healingFood = 3;
            return true;
        }
        return false;
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return this.dragonFood(stack.getItem());
    }

    private boolean canEatFood(int age, int minAge, int maxAge) {
        if (age < minAge || (age < maxAge && age > maxAge - (minAge * 2))) {
            return true;
        } else
            return false;
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide()) {
            if (this.getSize() < 3) {
                if (this.tickCount % 1200 == 0) {
                    this.dragonAge++;
                }
                if (this.dragonAge == 5 && this.getSize() == 1) {
                    this.setSize(2);
                    this.healDragonIfGrowing();
                }
                if (this.dragonAge == 15 && this.getSize() == 2) {
                    this.setSize(3);
                    this.healDragonIfGrowing();
                }
                if (this.dragonAgeFoodBonus > 5) {
                    this.dragonAgeFoodBonus = 0;
                    this.dragonAge++;
                    this.level().broadcastEntityEvent(this, (byte) 9);
                }
            }
            if (this.isInWater() || this.isInLava()) {
                if (this.isFlying && this.getFluidHeight(FluidTags.WATER) > 1.2D) {
                    this.fluidTicker++;
                    if (this.fluidTicker >= 30) {
                        this.isFlying = false;
                        this.getEntityData().set(IS_FLYING, false);
                        this.getEntityData().set(IS_START_FLYING, false);
                        this.getEntityData().set(CLIENT_START_FLYING, false);
                        this.getEntityData().set(CLIENT_END_FLYING, false);
                        this.startFlyingTimer = 0;
                        this.fluidTicker = 0;
                    }
                } else if (this.level().isClientSide() && (!this.getPassengers().isEmpty()) && !this.isFlying && this.getFluidHeight(FluidTags.WATER) > 1.0D) {
                    this.push(0.0D, 0.05D, 0.0D);
                }
            } else if (this.fluidTicker != 0)
                this.fluidTicker = 0;
        }

        if (this.fireBreathCooldown > 60) {
            this.fireBreathCooldown++;
            if (this.getEntityData().get(FIRE_BREATH)) {
                this.getEntityData().set(FIRE_BREATH, false);
            }
            if (this.fireBreathCooldown >= 120) {
                this.fireBreathCooldown = 0;
            }
        }
        if (this.fireBreathActive) {
            this.dragonFireBreath();
        }
    }

    private void healDragonIfGrowing() {
        if (this.getHealth() < this.getMaxHealth()) {
            this.setHealth(this.getHealth() + 20F);
        }
    }

    @Override
    public boolean isPushable() {
        return !(!this.getPassengers().isEmpty());
    }

    @Override
    public boolean canBeLeashed() {
        return !this.isLeashed();
    }

    @Environment(EnvType.CLIENT)
    protected void showEmoteParticle(boolean positive) {
        ParticleOptions particleEffect = ParticleTypes.HEART;
        if (!positive) {
            particleEffect = ParticleTypes.SMOKE;
        }

        for (int i = 0; i < 7; ++i) {
            double d = this.random.nextGaussian() * 0.02D;
            double e = this.random.nextGaussian() * 0.02D;
            double f = this.random.nextGaussian() * 0.02D;
            this.level().addParticle(particleEffect, this.getRandomX(1.0D), this.getRandomY() + 0.5D, this.getRandomZ(1.0D), d, e, f);
        }

    }

    @Override
    @Environment(EnvType.CLIENT)
    public void handleEntityEvent(byte status) {
        if (status == 7 || status == 9) {
            this.showEmoteParticle(true);
        } else if (status == 6) {
            this.showEmoteParticle(false);
        } else {
            super.handleEntityEvent(status);
        }

    }

    public boolean isTamed() {
        return ((Byte) this.entityData.get(TAMEABLE_FLAGS) & 4) != 0;
    }

    public void setTamed(boolean tamed) {
        byte b = (Byte) this.entityData.get(TAMEABLE_FLAGS);
        if (tamed) {
            this.entityData.set(TAMEABLE_FLAGS, (byte) (b | 4));
        } else {
            this.entityData.set(TAMEABLE_FLAGS, (byte) (b & -5));
        }
    }

    @Nullable
    public UUID getOwnerUuid() {
        return this.entityData.get(OWNER_UUID).map(EntityReference::getUUID).orElse(null);
    }

    public void setOwnerUuid(@Nullable UUID uuid) {
        this.entityData.set(OWNER_UUID, Optional.ofNullable(uuid).map(value -> EntityReference.of(value)));
    }

    public void setOwner(Player player) {
        this.setTamed(true);
        this.setOwnerUuid(player.getUUID());
    }

    @Override
    public void setOwner(LivingEntity owner) {
        this.setTamed(owner != null);
        this.setOwnerUuid(owner == null ? null : owner.getUUID());
    }

    @Override
    public EntityReference<LivingEntity> getOwnerReference() {
        UUID uuid = this.getOwnerUuid();
        return uuid == null ? null : EntityReference.of(uuid);
    }

    @Override
    public boolean isTame() {
        return this.isTamed();
    }

    @Override
    public void setTame(boolean tamed, boolean applySideEffects) {
        this.setTamed(tamed);
    }

    @Nullable
    @Override
    public LivingEntity getOwner() {
        try {
            UUID uUID = this.getOwnerUuid();
            return uUID == null ? null : this.level().getPlayerInAnyDimension(uUID);
        } catch (IllegalArgumentException var2) {
            return null;
        }
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        return this.isOwner(target) ? false : super.canAttack(target);
    }

    public boolean isOwner(LivingEntity entity) {
        return entity == this.getOwner();
    }

    public boolean isTeammate(Entity other) {
        if (this.isTamed()) {
            LivingEntity livingEntity = this.getOwner();
            if (other == livingEntity) {
                return true;
            }

            if (livingEntity != null) {
                return livingEntity == other;
            }
        }

        return super.considersEntityAsAlly(other);
    }

    @Override
    public void die(DamageSource source) {
        if (!this.level().isClientSide()) {
            if (((ServerLevel) this.level()).getGameRules().get(GameRules.SHOW_DEATH_MESSAGES) && this.getOwner() instanceof ServerPlayer owner) {
                owner.sendSystemMessage(this.getCombatTracker().getDeathMessage());
            }
            if (FabricLoader.getInstance().isModLoaded("dragonloot")) {
                this.spawnAtLocation((ServerLevel) this.level(), new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath("dragonloot", "dragon_scale")), (int) (this.getSize() * this.level().getRandom().nextFloat())));
            }
        }

        super.die(source);
    }

    public boolean isInSittingPose() {
        return ((Byte) this.entityData.get(TAMEABLE_FLAGS) & 1) != 0;
    }

    public void setInSittingPose(boolean inSittingPose) {
        this.sitting = inSittingPose;
        byte b = (Byte) this.entityData.get(TAMEABLE_FLAGS);
        if (inSittingPose) {
            this.entityData.set(TAMEABLE_FLAGS, (byte) (b | 1));
        } else {
            this.entityData.set(TAMEABLE_FLAGS, (byte) (b & 0xFFFFFFFE));
        }

    }

    public void setSitting(boolean sitting) {
        this.sitting = sitting;
        setInSittingPose(sitting);
    }

    @Override
    public int getMaxHeadXRot() {
        if (this.isInSittingPose()) {
            return 20;
        }
        return super.getMaxHeadXRot();
    }

    @Override
    public boolean causeFallDamage(double fallDistance, float damageMultiplier, DamageSource damageSource) {
        return false;
    }

    @Override
    protected void checkFallDamage(double heightDifference, boolean onGround, BlockState landedState, BlockPos landedPosition) {
        if (!this.level().isClientSide() && !this.isFlying && !onGround && heightDifference < -0.7D) {
            if (this.sitting)
                this.setSitting(false);
            if (!(!this.getPassengers().isEmpty())) {
                this.isFlying = true;
                this.getEntityData().set(IS_FLYING, true);
                this.getEntityData().set(IS_START_FLYING, false);
                this.getEntityData().set(CLIENT_START_FLYING, false);
                this.startFlyingTime = (int) this.level().getGameTime();
            }
        }
        super.checkFallDamage(heightDifference, onGround, landedState, landedPosition);
    }

    private void playWingFlapSound() {
        this.level().playSound(null, this, SoundEvents.ENDER_DRAGON_FLAP, this.getSoundSource(), 5.0f, 0.8f + this.random.nextFloat() * 0.3f);
    }

    public void setKeyBind(String key) {
        this.keyBind = InputConstants.getKey(key).getValue();
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundInit.DRAGON_IDLE_EVENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundInit.DRAGON_HIT_EVENT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundInit.DRAGON_DEATH_EVENT;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(SoundInit.DRAGON_STEP_EVENT, 0.7F, 1.0F);
    }

    @Override
    protected float nextStep() {
        return (float) ((int) this.moveDist + 2) - 0.01F;
    }

    public boolean mustNotDisturb(double num) {
        return false;
    }

    @Override
    public void slotChanged(AbstractContainerMenu menu, int slotId, ItemStack stack) {
    }

    @Override
    public void dataChanged(AbstractContainerMenu menu, int slotId, int dataId) {
    }

    private void onChestedStatusChanged() {
        SimpleContainer simpleInventory = this.inventory;
        this.inventory = new SimpleContainer(27);
        if (simpleInventory != null) {
            
            int i = Math.min(simpleInventory.getContainerSize(), this.inventory.getContainerSize());

            for (int j = 0; j < i; ++j) {
                ItemStack itemStack = simpleInventory.getItem(j);
                if (!itemStack.isEmpty()) {
                    this.inventory.setItem(j, itemStack.copy());
                }
            }
        }

    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel serverLevel, DamageSource source, boolean recentlyHit) {
        super.dropCustomDeathLoot(serverLevel, source, recentlyHit);
        if (this.inventory != null) {
            for (int i = 0; i < this.inventory.getContainerSize(); ++i) {
                ItemStack itemStack = this.inventory.getItem(i);
                if (!itemStack.isEmpty()) {
                    this.spawnAtLocation(serverLevel, itemStack);
                }
            }
        }
        if (this.hasChest()) {
            this.spawnAtLocation(serverLevel, new ItemStack(Blocks.CHEST));
            this.setHasChest(false);
        }
    }

    public boolean hasChest() {
        return (Boolean) this.entityData.get(HAS_CHEST);
    }

    public void setHasChest(boolean hasChest) {
        this.entityData.set(HAS_CHEST, hasChest);
    }

    public void openInventory(Player player) {
        if (!this.level().isClientSide() && (!(!this.getPassengers().isEmpty()) || this.hasPassenger(player)) && this.isTamed()) {
            player.openMenu(new SimpleMenuProvider(
                    (containerId, inv, p) -> ChestMenu.threeRows(containerId, p.getInventory(), this.inventory), this.getName()));
        }

    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason spawnReason, @Nullable SpawnGroupData spawnGroupData) {
        this.getEntityData().set(OTHER_EARS, this.level().getRandom().nextBoolean());
        this.getEntityData().set(OTHER_TAIL, this.level().getRandom().nextBoolean());
        this.getEntityData().set(OTHER_EYES, this.level().getRandom().nextBoolean());
        if (this.level().dimension().equals(Level.NETHER))
            this.getEntityData().set(RED_DRAGON, this.level().getRandom().nextBoolean());
        if (spawnReason.equals(EntitySpawnReason.COMMAND)) {
            this.setSize(3);
        } else {
            this.setSize(1);
        }
        return super.finalizeSpawn(level, difficulty, spawnReason, spawnGroupData);
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob parent) {
        return null;
    }

    @Override
    protected EntityDimensions getDefaultDimensions(Pose pose) {
        return super.getDefaultDimensions(pose).scale((float) this.getSize() / 3.0F);
    }

    public int getSize() {
        return (Integer) this.entityData.get(DRAGON_SIZE);
    }

    public void setSize(int size) {
        this.entityData.set(DRAGON_SIZE, size);
        this.reapplyPosition();
        this.refreshDimensions();
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue((double) (size * 20));
        this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue((double) size * 3);
        this.getAttribute(Attributes.ARMOR).setBaseValue((double) size * 2);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> data) {
        if (DRAGON_SIZE.equals(data)) {
            this.refreshDimensions();
            this.setYRot(this.yHeadRot);
            this.setYBodyRot(this.yHeadRot);
        }
        super.onSyncedDataUpdated(data);
    }

    @Override
    public void refreshDimensions() {
        double d = this.getX();
        double e = this.getY();
        double f = this.getZ();
        super.refreshDimensions();
        this.setPos(d, e, f);
    }

    @Override
    protected float getSoundVolume() {
        return 0.3F * (float) this.getSize();
    }

    @Override
    public float getVoicePitch() {
        return 1.6F - ((float) 0.2F * this.getSize());
    }

    // @Override
    // protected float getActiveEyeHeight(Pose pose, EntityDimensions dimensions) {
    // if (this.getSize() == 1) {
    // return 0.80F * dimensions.height;
    // }
    // return 0.85F * dimensions.height;
    // }

    // @Override
    // public double getMountedHeightOffset() {
    // double flySubtraction = 0.92D;
    // if (this.isFlying) {
    // flySubtraction = 0.9D;
    // }
    // return (double) this.getSize() * 0.794 * flySubtraction;
    // }

    @Override
    public Vec3 getPassengerRidingPosition(Entity passenger) {
        if (passenger instanceof Mob mobEntity) {
            this.yBodyRot = mobEntity.yBodyRot;
        }
        if (!this.level().isClientSide()) {
            return super.getPassengerRidingPosition(passenger);
        }
        float offSet = 12F;
        if (passenger.equals(this.getControllingPassenger())) {
            offSet = 1F;
        }
        float f = Mth.sin(this.yBodyRot * 0.017453292F) * offSet;
        float g = Mth.cos(this.yBodyRot * 0.017453292F) * offSet;
        return new Vec3(this.getX() + (double) (0.1F * f), this.getY(0.66F), this.getZ() - (double) (0.1F * g));
    }

    @Override
    public boolean hurtServer(ServerLevel serverLevel, DamageSource source, float amount) {
        if (source.is(TagInit.IS_WALL)) {
            return false;
        }
        if (!this.level().isClientSide()) {
            this.setSitting(false);
        }
        return !this.isInvulnerableTo(serverLevel, source) && super.hurtServer(serverLevel, source, amount);
    }

    static {
        IS_FLYING = SynchedEntityData.defineId(DragonEntity.class, EntityDataSerializers.BOOLEAN);
        IS_START_FLYING = SynchedEntityData.defineId(DragonEntity.class, EntityDataSerializers.BOOLEAN);
        CLIENT_START_FLYING = SynchedEntityData.defineId(DragonEntity.class, EntityDataSerializers.BOOLEAN);
        CLIENT_END_FLYING = SynchedEntityData.defineId(DragonEntity.class, EntityDataSerializers.BOOLEAN);
        TAMEABLE_FLAGS = SynchedEntityData.defineId(DragonEntity.class, EntityDataSerializers.BYTE);
        OWNER_UUID = SynchedEntityData.defineId(DragonEntity.class, EntityDataSerializers.OPTIONAL_LIVING_ENTITY_REFERENCE);
        HAS_SADDLE = SynchedEntityData.defineId(DragonEntity.class, EntityDataSerializers.BOOLEAN);
        HAS_CHEST = SynchedEntityData.defineId(DragonEntity.class, EntityDataSerializers.BOOLEAN);
        OTHER_EARS = SynchedEntityData.defineId(DragonEntity.class, EntityDataSerializers.BOOLEAN);
        OTHER_TAIL = SynchedEntityData.defineId(DragonEntity.class, EntityDataSerializers.BOOLEAN);
        OTHER_EYES = SynchedEntityData.defineId(DragonEntity.class, EntityDataSerializers.BOOLEAN);
        DRAGON_SIZE = SynchedEntityData.defineId(DragonEntity.class, EntityDataSerializers.INT);
        FIRE_BREATH = SynchedEntityData.defineId(DragonEntity.class, EntityDataSerializers.BOOLEAN);
        RED_DRAGON = SynchedEntityData.defineId(DragonEntity.class, EntityDataSerializers.BOOLEAN);
    }

    @Environment(EnvType.CLIENT)
    public static void flyDragonDown(LocalPlayer player, String keyString) {
        if (player.getVehicle() != null && player.getVehicle() instanceof DragonEntity)
            ((DragonEntity) player.getVehicle()).setKeyBind(keyString);
    }

    @Environment(EnvType.CLIENT)
    public static void dragonFireBreath(LocalPlayer player) {
        if (player.getVehicle() != null && player.getVehicle() instanceof DragonEntity dragonEntity && player.getVehicle().isAlive()) {
            if (dragonEntity.getEntityData().get(DragonEntity.DRAGON_SIZE) >= 3) {
                ClientPlayNetworking.send(new DragonFireBreathPacket());
                ((DragonEntity) player.getVehicle()).fireBreathActive = true;
            }
        }
    }

}
