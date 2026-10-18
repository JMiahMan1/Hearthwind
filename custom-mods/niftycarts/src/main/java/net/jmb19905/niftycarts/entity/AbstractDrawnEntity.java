package net.jmb19905.niftycarts.entity;

import com.google.common.collect.UnmodifiableIterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.jmb19905.niftycarts.NiftyCarts;
import net.jmb19905.niftycarts.NiftyCartsConfig;
import net.jmb19905.niftycarts.advancement.NCCriteriaTriggers;
import net.jmb19905.niftycarts.network.clientbound.UpdateDrawnPayload;
import net.jmb19905.niftycarts.network.serverbound.ToggleSlowPayload;
import net.jmb19905.niftycarts.util.CartWheel;
import net.jmb19905.niftycarts.util.NiftyWorld;
import net.minecraft.core.Direction;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.InterpolationHandler;
import net.minecraft.world.entity.ItemSteerable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.PlayerRideable;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.DismountHelper;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractDrawnEntity extends Entity {
   private static final net.minecraft.network.syncher.EntityDataAccessor<Integer> TIME_SINCE_HIT = SynchedEntityData.defineId(
      AbstractDrawnEntity.class, EntityDataSerializers.INT
   );
   private static final net.minecraft.network.syncher.EntityDataAccessor<Integer> FORWARD_DIRECTION = SynchedEntityData.defineId(
      AbstractDrawnEntity.class, EntityDataSerializers.INT
   );
   private static final net.minecraft.network.syncher.EntityDataAccessor<Float> DAMAGE_TAKEN = SynchedEntityData.defineId(
      AbstractDrawnEntity.class, EntityDataSerializers.FLOAT
   );
   private static final net.minecraft.network.syncher.EntityDataAccessor<net.minecraft.world.item.ItemStack> BANNER = SynchedEntityData.defineId(
      AbstractDrawnEntity.class, EntityDataSerializers.ITEM_STACK
   );
   private static final net.minecraft.network.syncher.EntityDataAccessor<String> WOOD_TYPE = SynchedEntityData.defineId(
      AbstractDrawnEntity.class, EntityDataSerializers.STRING
   );
   private static final net.minecraft.network.syncher.EntityDataAccessor<Boolean> LOCKED = SynchedEntityData.defineId(
      AbstractDrawnEntity.class, EntityDataSerializers.BOOLEAN
   );
   private static final Identifier PULL_MODIFIER_ID = NiftyCarts.resLoc("pull");
   private final CartInterpolationHandler interpolation = new CartInterpolationHandler(this);
   protected List<CartWheel> wheels;
   private int pullingId = -1;
   private UUID pullingUUID = null;
   public Entity pulling;
   protected AbstractDrawnEntity drawn;

   public AbstractDrawnEntity(
      net.minecraft.world.entity.EntityType<? extends net.minecraft.world.entity.Entity> entityTypeIn, net.minecraft.world.level.Level worldIn
   ) {
      super(entityTypeIn, worldIn);
      this.blocksBuilding = true;
      this.initWheels();
   }

   protected double getSpacing() {
      return 1.7;
   }

   public boolean shouldPitch() {
      return true;
   }

   public float getDisconnectedAngle() {
      return 25.0F;
   }

   public boolean isLocked() {
      return (Boolean)this.entityData.get(LOCKED);
   }

   public float maxUpStep() {
      return 1.2F;
   }

   @NotNull
   public InteractionResult interact(@NotNull Player player, @NotNull InteractionHand hand, @NotNull Vec3 vec) {
      return (InteractionResult)(this.isLocked() ? InteractionResult.FAIL : super.interact(player, hand, vec));
   }


   public void tick() {
      if (this.getTimeSinceHit() > 0) {
         this.setTimeSinceHit(this.getTimeSinceHit() - 1);
      }

      if (!this.isNoGravity()) {
         this.setDeltaMovement(0.0, this.getDeltaMovement().y - 0.08, 0.0);
      }

      if (this.getDamageTaken() > 0.0F) {
         this.setDamageTaken(this.getDamageTaken() - 1.0F);
      }

      super.tick();
      this.interpolation.interpolate();
      if (this.pulling == null) {
         if (this.shouldPitch()) {
            this.setXRot(this.getDisconnectedAngle());
         }

         this.move(MoverType.SELF, this.getDeltaMovement());
         this.attemptReattach();
      }

      for (Entity entity : this.level().getEntities(this, this.getBoundingBox(), EntitySelector.pushableBy(this))) {
         this.push(entity);
      }
   }

   public void pulledTick() {
      if (this.pulling != null) {
         if (!this.canPull(this.pulling)) {
            this.setPulling(null);
         } else {
            Vec3 targetVec = this.getRelativeTargetVec(1.0F);
            this.handleRotation(targetVec);

            while (this.getYRot() - this.yRotO < -180.0F) {
               this.yRotO -= 360.0F;
            }

            while (this.getYRot() - this.yRotO >= 180.0F) {
               this.yRotO += 360.0F;
            }

            if (this.pulling.onGround()) {
               targetVec = new Vec3(targetVec.x, 0.0, targetVec.z);
            }

            double targetVecLength = targetVec.length();
            double r = 0.2;
            double relativeSpacing = Math.max(this.getSpacing() + 0.5 * this.pulling.getBbWidth(), 1.0);
            double diff = targetVecLength - relativeSpacing;
            Vec3 move;
            if (Math.abs(diff) < 0.2) {
               move = this.getDeltaMovement();
            } else {
               move = this.getDeltaMovement().add(targetVec.subtract(targetVec.normalize().scale(relativeSpacing + 0.2 * Math.signum(diff))));
            }

            this.setOnGround(true);
            double startX = this.getX();
            double startY = this.getY();
            double startZ = this.getZ();
            this.move(MoverType.SELF, move);
            if (this.isAlive()) {
               this.addStats(this.getX() - startX, this.getY() - startY, this.getZ() - startZ);
               if (this.level().isClientSide()) {
                  for (CartWheel wheel : this.wheels) {
                     wheel.tick();
                  }
               } else {
                  targetVec = this.getRelativeTargetVec(1.0F);
                  if (targetVec.length() > relativeSpacing + 1.0) {
                     this.setPulling(null);
                  }
               }

               this.updatePassengers();
               if (this.drawn != null) {
                  this.drawn.pulledTick();
               }
            }
         }
      }
   }

   protected Optional<net.minecraft.world.entity.player.Player> getControllingPlayer() {
      if (this.getPulling() instanceof Player pl) {
         return Optional.of(pl);
      } else {
         return this.getPulling() != null && this.getPulling().getControllingPassenger() instanceof Player pl ? Optional.of(pl) : Optional.empty();
      }
   }

   private void addStats(double x, double y, double z) {
      if (!this.level().isClientSide()) {
         int cm = Math.round(Mth.sqrt((float)(x * x + y * y + z * z)) * 100.0F);
         if (cm > 0) {
            Entity pulling = this.getPulling();
            if (pulling.getControllingPassenger() instanceof PostilionEntity && this.getControllingPassenger() instanceof ServerPlayer player) {
               if (this instanceof AnimalCartEntity) {
                  player.awardStat(NiftyCarts.STEER_ANIMAL_CART_CM, cm);
                  int allCm = player.getStats().getValue(Stats.CUSTOM.get(NiftyCarts.STEER_ANIMAL_CART_CM));
                  NCCriteriaTriggers.STEER_CART.trigger(player, this, allCm);
               } else if (this instanceof ReaperCartEntity) {
                  player.awardStat(NiftyCarts.STEER_REAPER_CM, cm);
               }
            }

            Optional<net.minecraft.world.entity.player.Player> playerOptional = this.getControllingPlayer();
            playerOptional.ifPresent(playerx -> {
               Identifier stat = (Identifier)NiftyCarts.CART_PULL_CM.get(this.getType());
               playerx.awardStat(stat, cm);
               if (playerx instanceof ServerPlayer serverPlayer) {
                  int allCm = serverPlayer.getStats().getValue(Stats.CUSTOM.get(stat));
                  float fillLevel = this instanceof AbstractDrawnInventoryEntity invCart ? invCart.getFillLevel() : 0.0F;
                  NCCriteriaTriggers.PULL_CART.trigger(serverPlayer, this, allCm, fillLevel);
               }
            });

            for (Entity passenger : this.getPassengers()) {
               if (passenger instanceof Player playerx) {
                  playerx.awardStat(NiftyCarts.RIDE_CART_CM, cm);
               }
            }
         }
      }
   }

   public void initWheels() {
      this.wheels = Arrays.asList(new CartWheel(this, 0.9F), new CartWheel(this, -0.9F));
   }

   public boolean shouldRemovePulling() {
      if (this.horizontalCollision) {
         Vec3 start = new Vec3(this.getX(), this.getY() + this.getBbHeight(), this.getZ());
         Vec3 end = new Vec3(this.pulling.getX(), this.pulling.getY() + this.pulling.getBbHeight() / 2.0F, this.pulling.getZ());
         BlockHitResult result = this.level().clip(new ClipContext(start, end, Block.COLLIDER, Fluid.NONE, this));
         return result.getType() == Type.BLOCK;
      } else {
         return false;
      }
   }

   public void updatePassengers() {
      for (Entity passenger : this.getPassengers()) {
         this.positionRider(passenger);
      }
   }

   public Entity getPulling() {
      return this.pulling;
   }

   public void setPulling(Entity entityIn) {
      if (!this.level().isClientSide()) {
         if (this.canBePulledBy(entityIn)) {
            if (entityIn == null) {
               if (this.pulling instanceof LivingEntity) {
                  AttributeInstance attr = ((LivingEntity)this.pulling).getAttribute(Attributes.MOVEMENT_SPEED);
                  if (attr != null) {
                     attr.removeModifier(ToggleSlowPayload.PULL_SLOWLY_MODIFIER_ID);
                     attr.removeModifier(PULL_MODIFIER_ID);
                  }
               } else if (this.pulling instanceof AbstractDrawnEntity) {
                  ((AbstractDrawnEntity)this.pulling).drawn = null;
               }

               for (ServerPlayer player : PlayerLookup.tracking(this)) {
                  ServerPlayNetworking.send(player, new UpdateDrawnPayload(-1, this.getId()));
               }

               this.pullingUUID = null;
               if (this.tickCount > 20) {
                  this.playDetachSound();
               }
            } else {
               if (entityIn instanceof LivingEntity && (Double)this.getConfig().pullSpeed.get() != 0.0) {
                  AttributeInstance attr = ((LivingEntity)entityIn).getAttribute(Attributes.MOVEMENT_SPEED);
                  if (attr != null && attr.getModifier(PULL_MODIFIER_ID) == null) {
                     attr.addTransientModifier(new AttributeModifier(PULL_MODIFIER_ID, (Double)this.getConfig().pullSpeed.get(), Operation.ADD_MULTIPLIED_TOTAL));
                  }
               }

               if (entityIn instanceof PathfinderMob pathfinder) {
                  pathfinder.getNavigation().stop();
               }

               for (ServerPlayer player : PlayerLookup.tracking(this)) {
                  ServerPlayNetworking.send(player, new UpdateDrawnPayload(entityIn.getId(), this.getId()));
               }

               this.pullingUUID = entityIn.getUUID();
               if (this.tickCount > 20) {
                  this.playAttachSound();
               }
            }

            if (entityIn instanceof AbstractDrawnEntity) {
               ((AbstractDrawnEntity)entityIn).drawn = this;
            }

            this.pulling = entityIn;
            NiftyWorld.get(this.level()).addPulling(this);
         }
      } else {
         if (entityIn != null) {
            this.pullingId = entityIn.getId();
            if (entityIn instanceof AbstractDrawnEntity) {
               ((AbstractDrawnEntity)entityIn).drawn = this;
            }
         } else {
            this.pullingId = -1;

            for (CartWheel wheel : this.wheels) {
               wheel.clearIncrement();
            }

            if (this.pulling instanceof AbstractDrawnEntity) {
               ((AbstractDrawnEntity)this.pulling).drawn = null;
            }
         }

         this.pulling = entityIn;
         NiftyWorld.get(this.level()).addPulling(this);
      }
   }

   private void playAttachSound() {
      this.playSound(NiftyCarts.ATTACH_SOUND, 0.2F, 1.0F);
   }

   private void playDetachSound() {
      this.playSound(NiftyCarts.DETACH_SOUND, 0.2F, 1.0F);
   }

   private void attemptReattach() {
      if (this.level().isClientSide()) {
         if (this.pullingId != -1) {
            Entity entity = this.level().getEntity(this.pullingId);
            if (entity != null && entity.isAlive()) {
               this.setPulling(entity);
            }
         }
      } else if (this.pullingUUID != null) {
         Entity entity = this.level().getEntity(this.pullingUUID);
         if (entity != null && entity.isAlive()) {
            this.setPulling(entity);
         }
      }
   }

   public boolean shouldStopPulledTick() {
      if (this.isAlive() && this.getPulling() != null && this.getPulling().isAlive() && !this.getPulling().isPassenger()) {
         if (!this.level().isClientSide() && this.shouldRemovePulling()) {
            this.setPulling(null);
            return true;
         } else {
            return false;
         }
      } else {
         if (this.pulling != null && this.pulling instanceof Player) {
            this.setPulling(null);
         } else {
            this.pulling = null;
         }

         return true;
      }
   }

   public Vec3 getRelativeTargetVec(float delta) {
      double x;
      double y;
      double z;
      if (delta == 1.0F) {
         x = this.pulling.getX() - this.getX();
         y = this.pulling.getY() - this.getY();
         z = this.pulling.getZ() - this.getZ();
      } else {
         x = Mth.lerp(delta, this.pulling.xOld, this.pulling.getX()) - Mth.lerp(delta, this.xOld, this.getX());
         y = Mth.lerp(delta, this.pulling.yOld, this.pulling.getY()) - Mth.lerp(delta, this.yOld, this.getY());
         z = Mth.lerp(delta, this.pulling.zOld, this.pulling.getZ()) - Mth.lerp(delta, this.zOld, this.getZ());
      }

      float yaw = (float)Math.toRadians(this.pulling.getYRot());
      float nx = -Mth.sin(yaw);
      float nz = Mth.cos(yaw);
      double r = 0.2;
      return new Vec3(x + nx * 0.2, y, z + nz * 0.2);
   }

   public void handleRotation(Vec3 target) {
      this.setYRot(getYaw(target));
      if (this.shouldPitch()) {
         this.setXRot(getPitch(target));
      }
   }

   public static float getYaw(Vec3 vec) {
      return Mth.wrapDegrees((float)Math.toDegrees(-Mth.atan2(vec.x, vec.z)));
   }

   public static float getPitch(Vec3 vec) {
      return Mth.wrapDegrees(
         (float)Math.toDegrees(-Mth.atan2(vec.y, Mth.sqrt((float)(vec.x * vec.x + vec.z * vec.z))))
      );
   }

   public double getWheelRotation(int wheel) {
      return this.wheels.get(wheel).getRotation();
   }

   public double getWheelRotationIncrement(int wheel) {
      return this.wheels.get(wheel).getRotationIncrement();
   }

   public abstract Item getCartItem();

   protected boolean canBePulledBy(Entity entityIn) {
      if (this.isLocked()) {
         return false;
      } else if (this.level().isClientSide()) {
         return true;
      } else if (entityIn == null) {
         return true;
      } else {
         System.out.println(this.pulling);
         return (this.pulling == null || !this.pulling.isAlive()) && !this.hasPassenger(entityIn) && this.canPull(entityIn);
      }
   }

   private boolean canPull(Entity entity) {
      ArrayList<String> allowed = (ArrayList<String>)this.getConfig().pullEntities.get();
      if (allowed.isEmpty()) {
         return entity instanceof Player player ? !player.isSpectator() : entity instanceof PlayerRideable && !(entity instanceof ItemSteerable);
      } else {
         return allowed.contains(EntityType.getKey(entity.getType()).toString());
      }
   }

   public abstract NiftyCartsConfig.CartConfig getConfig();

   public boolean hurtServer(@NotNull ServerLevel serverLevel, @NotNull DamageSource source, float amount) {
      if (this.isLocked()) {
         return false;
      } else if (this.isInvulnerableToBase(source)) {
         return false;
      } else if (this.level().isClientSide() || !this.isAlive()) {
         return false;
      } else if (source.is(DamageTypes.CACTUS)) {
         return false;
      } else if (source.getEntity() != null && this.hasPassenger(source.getEntity())) {
         return false;
      } else {
         this.setForwardDirection(-this.getForwardDirection());
         this.setTimeSinceHit(10);
         this.setDamageTaken(this.getDamageTaken() + amount * 10.0F);
         boolean flag = source.getEntity() instanceof Player player && player.getAbilities().instabuild;
         boolean adventureFlag = source.getEntity() instanceof Player playerx
            && playerx.gameMode() == GameType.ADVENTURE
            && !(Boolean)this.getConfig().adventureModeInteract.get();
         if (adventureFlag) {
            return false;
         } else {
            if (flag || this.getDamageTaken() > 40.0F) {
               this.onDestroyed(source, flag);
               this.setPulling(null);
               this.discard();
            }

            return true;
         }
      }
   }

   protected InteractionResult useBanner(Player player, InteractionHand hand) {
      ItemStack stack = player.getItemInHand(hand);
      if (stack.is(ItemTags.BANNERS)) {
         ItemStack oldBanner = this.getBanner();
         if (!this.level().isClientSide()) {
            ItemStack banner = stack.split(1);
            NCCriteriaTriggers.CART_ADD_BANNER.trigger((ServerPlayer)player, banner);
            if (!oldBanner.isEmpty()) {
               if (stack.isEmpty()) {
                  player.setItemInHand(hand, oldBanner);
               } else if (!player.getInventory().add(oldBanner)) {
                  player.drop(oldBanner, false);
               }
            }

            this.playSound(SoundEvents.WOOD_PLACE, 1.0F, 0.8F);
            this.setBanner(banner);
         }

         return InteractionResult.SUCCESS;
      } else {
         return InteractionResult.PASS;
      }
   }

   public void onDestroyed(DamageSource source, boolean byCreativePlayer) {
      if (this.level() instanceof ServerLevel serverLevel) {
         if ((Boolean)serverLevel.getGameRules().get(GameRules.ENTITY_DROPS)) {
            if (!byCreativePlayer) {
               this.spawnAtLocation(serverLevel, this.getCartItem());
               this.spawnAtLocation(serverLevel, this.getBanner());
            }

            this.onDestroyedAndDoDrops(source);
         }
      }
   }

   public void onDestroyedAndDoDrops(DamageSource source) {
   }

   public boolean isPushedByFluid() {
      return false;
   }

   public boolean canBeCollidedWith(Entity entity) {
      return this.isAlive();
   }

   @Nullable
   public InterpolationHandler getInterpolation() {
      return this.interpolation;
   }

   @Nullable
   public LivingEntity getControllingPassenger() {
      List<net.minecraft.world.entity.Entity> passengers = this.getPassengers();
      if (passengers.isEmpty()) {
         return null;
      } else {
         Entity first = (Entity)passengers.getFirst();
         return !(first instanceof Animal) && first instanceof LivingEntity ? (LivingEntity)first : null;
      }
   }

   protected boolean isLocalClientAuthoritative() {
      return false;
   }

   @NotNull
   public Vec3 getDismountLocationForPassenger(LivingEntity rider) {
      for (float angle : rider.getMainArm() == HumanoidArm.RIGHT ? new float[]{90.0F, -90.0F} : new float[]{-90.0F, 90.0F}) {
         Vec3 pos = this.dismount(getCollisionHorizontalEscapeVector(this.getBbWidth(), rider.getBbWidth(), this.getYRot() + angle), rider);
         if (pos != null) {
            return pos;
         }
      }

      return this.position();
   }

   private Vec3 dismount(Vec3 dir, LivingEntity rider) {
      double x = this.getX() + dir.x;
      double y = this.getBoundingBox().minY;
      double z = this.getZ() + dir.z;
      double limit = this.getBoundingBox().maxY + 0.75;
      MutableBlockPos blockPos = new MutableBlockPos();
      UnmodifiableIterator var12 = rider.getDismountPoses().iterator();

      while (var12.hasNext()) {
         Pose pose = (Pose)var12.next();
         blockPos.set(x, y, z);

         for (; blockPos.getY() < limit; blockPos.move(Direction.UP)) {
            double ground = this.level().getBlockFloorHeight(blockPos);
            if (blockPos.getY() + ground > limit) {
               break;
            }

            if (DismountHelper.isBlockFloorValid(ground)) {
               Vec3 pos = new Vec3(x, blockPos.getY() + ground, z);
               if (DismountHelper.canDismountTo(this.level(), rider, rider.getLocalBoundsForPose(pose).move(pos))) {
                  rider.setPose(pose);
                  return pos;
               }
            }
         }
      }

      return null;
   }

   public void setDamageTaken(float damageTaken) {
      this.entityData.set(DAMAGE_TAKEN, damageTaken);
   }

   public float getDamageTaken() {
      return (Float)this.entityData.get(DAMAGE_TAKEN);
   }

   public void setTimeSinceHit(int timeSinceHit) {
      this.entityData.set(TIME_SINCE_HIT, timeSinceHit);
   }

   public int getTimeSinceHit() {
      return (Integer)this.entityData.get(TIME_SINCE_HIT);
   }

   public void setForwardDirection(int forwardDirection) {
      this.entityData.set(FORWARD_DIRECTION, forwardDirection);
   }

   public int getForwardDirection() {
      return (Integer)this.entityData.get(FORWARD_DIRECTION);
   }

   public void setBanner(ItemStack banner) {
      this.entityData.set(BANNER, banner);
   }

   public ItemStack getBanner() {
      return (ItemStack)this.entityData.get(BANNER);
   }

   public DyeColor getBannerColor() {
      ItemStack banner = this.getBanner();
      return banner.getItem() instanceof BannerItem bannerItem ? bannerItem.getColor() : null;
   }

   public BannerPatternLayers getBannerPattern() {
      ItemStack banner = this.getBanner();
      if (banner.getItem() instanceof BannerItem) {
         BannerPatternLayers bannerPatternLayers = (BannerPatternLayers)banner.get(DataComponents.BANNER_PATTERNS);
         if (bannerPatternLayers != null) {
            return bannerPatternLayers;
         }
      }

      return BannerPatternLayers.EMPTY;
   }

   public ItemStack getPickResult() {
      return new ItemStack(this.getCartItem());
   }

   public boolean isPickable() {
      return true;
   }

   public void setWoodType(WoodType type) {
      this.entityData.set(WOOD_TYPE, type.name());
   }

   public WoodType getWoodType() {
      return WoodType.values().filter(type -> type.name().equals(this.entityData.get(WOOD_TYPE))).findFirst().orElse(null);
   }

   protected void defineSynchedData(Builder builder) {
      builder.define(TIME_SINCE_HIT, 0);
      builder.define(FORWARD_DIRECTION, 1);
      builder.define(DAMAGE_TAKEN, 0.0F);
      builder.define(BANNER, ItemStack.EMPTY);
      builder.define(WOOD_TYPE, "oak");
      builder.define(LOCKED, false);
   }

   protected void readAdditionalSaveData(ValueInput input) {
      Optional<UUID> optId = input.read("PullingUUID", UUIDUtil.CODEC);
      optId.ifPresent(value -> this.pullingUUID = value);
      Optional<net.minecraft.world.item.ItemStack> optItem = input.read("BannerItem", ItemStack.OPTIONAL_CODEC);
      optItem.ifPresent(this::setBanner);
      Optional<String> woodTypeString = input.getString("WoodType");
      WoodType woodType = WoodType.values().filter(type -> type.name().equals(woodTypeString.orElse(null))).findFirst().orElse(WoodType.OAK);
      this.setWoodType(woodType);
      boolean locked = input.getBooleanOr("Locked", false);
      this.entityData.set(LOCKED, locked);
   }

   protected void addAdditionalSaveData(@NotNull ValueOutput output) {
      if (this.pullingUUID != null) {
         output.store("PullingUUID", UUIDUtil.CODEC, this.pullingUUID);
      }

      ItemStack banner = this.getBanner();
      if (!banner.isEmpty()) {
         output.store("BannerItem", ItemStack.OPTIONAL_CODEC, banner);
      }

      output.putString("WoodType", this.getWoodType().name());
      output.putBoolean("Locked", this.isLocked());
   }

   public AbstractDrawnEntity.RenderInfo getInfo(float delta) {
      return new AbstractDrawnEntity.RenderInfo(delta);
   }

   public class RenderInfo {
      final float delta;
      Vec3 target;
      float yaw = Float.NaN;
      float pitch = Float.NaN;

      public RenderInfo(final float delta) {
         this.delta = delta;
      }

      public Vec3 getTarget() {
         if (this.target == null) {
            if (AbstractDrawnEntity.this.pulling == null) {
               this.target = AbstractDrawnEntity.this.getViewVector(this.delta);
            } else {
               this.target = AbstractDrawnEntity.this.getRelativeTargetVec(this.delta);
            }
         }

         return this.target;
      }

      public float getYaw() {
         if (Float.isNaN(this.yaw)) {
            if (AbstractDrawnEntity.this.pulling == null) {
               this.yaw = Mth.lerp(this.delta, AbstractDrawnEntity.this.yRotO, AbstractDrawnEntity.this.getYRot());
            } else {
               this.yaw = AbstractDrawnEntity.getYaw(this.getTarget());
            }
         }

         return this.yaw;
      }

      public float getPitch() {
         if (Float.isNaN(this.pitch)) {
            if (AbstractDrawnEntity.this.pulling == null) {
               if (!AbstractDrawnEntity.this.shouldPitch()) {
                  this.pitch = AbstractDrawnEntity.this.getDisconnectedAngle();
               } else {
                  this.pitch = Mth.lerp(this.delta, AbstractDrawnEntity.this.xRotO, AbstractDrawnEntity.this.getXRot());
               }
            } else {
               this.pitch = AbstractDrawnEntity.getPitch(this.getTarget());
            }
         }

         return this.pitch;
      }
   }
}
