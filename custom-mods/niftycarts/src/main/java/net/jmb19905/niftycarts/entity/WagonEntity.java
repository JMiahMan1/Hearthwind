package net.jmb19905.niftycarts.entity;

import java.util.List;
import net.jmb19905.niftycarts.NiftyCarts;
import net.jmb19905.niftycarts.NiftyCartsConfig;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.Entity.MoveFunction;
import net.minecraft.world.entity.animal.AgeableWaterCreature;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.WoolCarpetBlock;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class WagonEntity extends AbstractDrawnInventoryEntity {
   private static final net.minecraft.network.syncher.EntityDataAccessor<Integer> UNFURL = SynchedEntityData.defineId(
      WagonEntity.class, EntityDataSerializers.INT
   );
   private static final net.minecraft.network.syncher.EntityDataAccessor<Integer> ROOF_COLOR = SynchedEntityData.defineId(
      WagonEntity.class, EntityDataSerializers.INT
   );
   private static final net.minecraft.network.syncher.EntityDataAccessor<net.minecraft.world.item.ItemStack> EQUIPPED_CARPET = SynchedEntityData.defineId(
      WagonEntity.class, EntityDataSerializers.ITEM_STACK
   );
   private static final net.minecraft.network.syncher.EntityDataAccessor<Integer> CHEST_COUNT = SynchedEntityData.defineId(
      WagonEntity.class, EntityDataSerializers.INT
   );

   public WagonEntity(
      net.minecraft.world.entity.EntityType<? extends net.minecraft.world.entity.Entity> entityTypeIn, net.minecraft.world.level.Level worldIn
   ) {
      super(entityTypeIn, worldIn, 108);
   }

   @Override
   public void onDestroyedAndDoDrops(DamageSource source) {
      super.onDestroyedAndDoDrops(source);
      if (this.level() instanceof ServerLevel serverLevel && (Boolean)serverLevel.getGameRules().get(GameRules.ENTITY_DROPS)) {
         this.spawnAtLocation(serverLevel, new ItemStack(Items.CHEST, this.getChestCount()));
         this.spawnAtLocation(serverLevel, (ItemStack)this.entityData.get(EQUIPPED_CARPET));
      }
   }

   public int getChestCount() {
      return (Integer)this.entityData.get(CHEST_COUNT);
   }

   public int getMaxChestCount() {
      return 3;
   }

   public int getCurrentRowCount() {
      return this.getChestCount() * 4;
   }

   @Override
   protected double getSpacing() {
      return 2.5;
   }

   @Override
   public boolean shouldPitch() {
      return false;
   }

   public int getUnfurled() {
      return (Integer)this.entityData.get(UNFURL);
   }

   public boolean hasRoof() {
      return (Integer)this.entityData.get(ROOF_COLOR) != -1;
   }

   @Nullable
   public DyeColor getRoofColor() {
      return !this.hasRoof() ? null : DyeColor.byId((Integer)this.entityData.get(ROOF_COLOR));
   }

   public Identifier getRoofTexture() {
      DyeColor color = this.getRoofColor();
      String name = "white";
      if (color != null) {
         name = color.getName();
      }

      return Identifier.fromNamespaceAndPath("niftycarts", "textures/entity/wagon_roof_" + name + ".png");
   }

   @Override
   protected void defineSynchedData(Builder builder) {
      super.defineSynchedData(builder);
      builder.define(UNFURL, 0);
      builder.define(ROOF_COLOR, -1);
      builder.define(CHEST_COUNT, 0);
      builder.define(EQUIPPED_CARPET, ItemStack.EMPTY);
   }

   @Override
   public Item getCartItem() {
      return NiftyCarts.WAGON.get(this.getWoodType());
   }

   @Override
   public float getDisconnectedAngle() {
      return 20.0F;
   }

   @NotNull
   @Override
   public InteractionResult interact(@NotNull Player player, @NotNull InteractionHand interactionHand, @NotNull Vec3 vec3) {
      if (this.isLocked()) {
         return InteractionResult.FAIL;
      } else {
         ItemStack itemStack = player.getItemInHand(interactionHand);
         InteractionResult bannerResult = this.useBanner(player, interactionHand);
         if (bannerResult.consumesAction()) {
            return bannerResult;
         } else if (vec3.y > 2.2 && !player.isSecondaryUseActive()) {
            return this.interactCarpet(itemStack, player);
         } else if (itemStack.is(Items.CHEST) && this.canAddChest()) {
            return this.interactChest(itemStack, player);
         } else if (player.isSecondaryUseActive() && !this.getPassengers().isEmpty() && !this.level().isClientSide()) {
            for (Entity entity : this.getPassengers()) {
               if (!(entity instanceof Player)) {
                  entity.stopRiding();
               }
            }

            return InteractionResult.SUCCESS_SERVER;
         } else {
            return super.interact(player, interactionHand, vec3);
         }
      }
   }

   private InteractionResult interactCarpet(ItemStack itemStack, Player player) {
      if (itemStack.is(ItemTags.WOOL_CARPETS)) {
         if (!(itemStack.getItem() instanceof BlockItem item)) {
            return InteractionResult.PASS;
         }

         if (!(item.getBlock() instanceof WoolCarpetBlock block)) {
            return InteractionResult.PASS;
         }

         if (itemStack.getCount() >= 5 || player.hasInfiniteMaterials()) {
            this.entityData.set(ROOF_COLOR, block.getColor().getId());
            this.playSound(SoundEvents.WOOL_PLACE);
            if (!player.hasInfiniteMaterials()) {
               itemStack.shrink(5);
               if (!player.getInventory().add((ItemStack)this.entityData.get(EQUIPPED_CARPET))) {
                  player.drop((ItemStack)this.entityData.get(EQUIPPED_CARPET), false);
               }
            }

            this.entityData.set(EQUIPPED_CARPET, new ItemStack(item, 5));
         }
      } else if (this.hasRoof()) {
         this.entityData.set(UNFURL, (this.getUnfurled() + 1) % 3);
         this.playSound(SoundEvents.WOOL_STEP);
      }

      return InteractionResult.CONSUME;
   }

   private InteractionResult interactChest(ItemStack itemStack, Player player) {
      this.entityData.set(CHEST_COUNT, this.getChestCount() + 1);
      if (!player.hasInfiniteMaterials()) {
         itemStack.shrink(1);
      }

      return InteractionResult.CONSUME;
   }

   private boolean canAddChest() {
      int chests = this.getChestCount();
      int passengers = this.getPassengers().size();

      return switch (chests) {
         case 0 -> true;
         case 1 -> passengers <= 2;
         case 2 -> passengers == 0;
         default -> false;
      };
   }

   @Override
   public void handleRotation(Vec3 target) {
      this.setYRot(getYaw(target));
   }

   @Override
   public NiftyCartsConfig.CartConfig getConfig() {
      return NiftyCartsConfig.get().wagon;
   }

   @Override
   protected InteractionResult onInteractNotOpen(Player player, InteractionHand hand) {
      if (this.getPulling() != player) {
         if (!this.canAddPassenger(player)) {
            return InteractionResult.PASS;
         } else if (!this.level().isClientSide()) {
            return (InteractionResult)(player.startRiding(this) ? InteractionResult.CONSUME : InteractionResult.PASS);
         } else {
            return InteractionResult.SUCCESS;
         }
      } else {
         return InteractionResult.PASS;
      }
   }

   protected boolean canAddPassenger(@NotNull Entity entity) {
      return switch (this.getChestCount()) {
         case 0, 1 -> this.getPassengers().size() < 4;
         case 2 -> this.getPassengers().size() < 2;
         default -> false;
      };
   }

   public float getPassengersRidingOffsetY(EntityDimensions entityDimensions, float f) {
      return (entityDimensions.height() - 2.0F - 0.125F) * f;
   }

   @NotNull
   protected Vec3 getPassengerAttachmentPoint(@NotNull Entity entity, @NotNull EntityDimensions entityDimensions, float factor) {
      int idx = this.getPassengers().indexOf(entity);
      double f = idx != 0 && idx != 2 ? -1.2 : 0.1;
      double s = idx != 0 && idx != 1 ? -0.7 : 0.7;
      f = this.getChestCount() == 2 ? -1.2 : f;
      s = this.getChestCount() == 2 ? (idx == 0 ? -0.7 : 0.7) : s;
      Vec3 forward = this.getLookAngle().scale(f);
      Vec3 sideways = new Vec3(forward.z, 0.0, -forward.x).normalize().scale(s);
      return new Vec3(
         forward.x + sideways.x,
         this.getPassengersRidingOffsetY(entityDimensions, factor) + forward.y,
         forward.z + sideways.z
      );
   }

   public void positionRider(@NotNull Entity passenger, @NotNull MoveFunction moveFunction) {
      super.positionRider(passenger, moveFunction);
      int idx = this.getPassengers().indexOf(passenger);
      int dir = idx != 0 && idx != 3 ? -1 : 1;
      if (this.hasPassenger(passenger)) {
         passenger.setYBodyRot(this.getYRot() + (passenger instanceof TamableAnimal ? 180 : 90) * dir);
         float f2 = Mth.wrapDegrees(passenger.getYRot() - this.getYRot() - (passenger instanceof TamableAnimal ? 180 : 90) * dir);
         float clamped = Mth.clamp(f2, -105.0F, 105.0F);
         passenger.yRotO += clamped - f2;
         passenger.setYRot(passenger.getYRot() + (clamped - f2));
         passenger.setYHeadRot(passenger.getYRot());
      }
   }

   @Override
   public void tick() {
      super.tick();
      if (!this.isLocked()) {
         List<net.minecraft.world.entity.Entity> list = this.level()
            .getEntities(this, this.getBoundingBox().inflate(0.2F, -0.01F, 0.2F), EntitySelector.pushableBy(this));
         if (!list.isEmpty()) {
            boolean bl = !this.level().isClientSide() && !(this.getControllingPassenger() instanceof Player);

            for (Entity entity : list) {
               if (!entity.hasPassenger(this)
                  && bl
                  && this.canAddPassenger(entity)
                  && !entity.isPassenger()
                  && entity.getBbWidth() < this.getBbWidth() / 2.0F
                  && entity.getBbWidth() * entity.getBbHeight() < 1.5
                  && entity instanceof LivingEntity
                  && !(entity instanceof AgeableWaterCreature)
                  && !(entity instanceof Player)) {
                  if (entity instanceof TamableAnimal tamable) {
                     tamable.setInSittingPose(true);
                  }

                  entity.startRiding(this);
               }
            }
         }
      }
   }

   @Override
   protected AbstractContainerMenu createMenuLootUnpacked(int i, Inventory inventory, Player player) {
      net.minecraft.world.inventory.MenuType<net.minecraft.world.inventory.ChestMenu> type = switch (this.getChestCount()) {
         case 1 -> NiftyCarts.CHEST_9x4_MENU_TYPE;
         case 2 -> NiftyCarts.CHEST_9x8_MENU_TYPE;
         case 3 -> NiftyCarts.CHEST_9x12_MENU_TYPE;
         default -> null;
      };
      return type == null ? null : new ChestMenu(type, i, inventory, this, this.getCurrentRowCount());
   }

   @Override
   protected void addAdditionalSaveData(@NotNull ValueOutput output) {
      super.addAdditionalSaveData(output);
      output.putInt("Unfurl", (Integer)this.entityData.get(UNFURL));
      output.putInt("RoofColor", (Integer)this.entityData.get(ROOF_COLOR));
      output.putInt("ChestCount", (Integer)this.entityData.get(CHEST_COUNT));
      if (!((ItemStack)this.entityData.get(EQUIPPED_CARPET)).isEmpty()) {
         ItemStack itemStack = (ItemStack)this.entityData.get(EQUIPPED_CARPET);
         output.store("Carpet", ItemStack.CODEC, itemStack);
      }
   }

   @Override
   protected void readAdditionalSaveData(ValueInput input) {
      super.readAdditionalSaveData(input);
      this.entityData.set(UNFURL, input.getInt("Unfurl").orElse(0));
      this.entityData.set(ROOF_COLOR, input.getInt("RoofColor").orElse(-1));
      this.entityData.set(CHEST_COUNT, input.getInt("ChestCount").orElse(0));
      this.entityData.set(EQUIPPED_CARPET, input.read("Carpet", ItemStack.CODEC).orElse(ItemStack.EMPTY));
   }

   @Override
   protected void saveInventory(ValueOutput output) {
      ContainerHelper.saveAllItems(output, this.getItemStacks());
   }

   @Override
   protected void readInventory(ValueInput input) {
      ContainerHelper.loadAllItems(input, this.getItemStacks());
   }
}
