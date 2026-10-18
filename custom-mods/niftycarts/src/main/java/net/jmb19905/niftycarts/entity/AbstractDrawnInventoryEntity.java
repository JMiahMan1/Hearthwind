package net.jmb19905.niftycarts.entity;
import net.minecraft.world.phys.Vec3;

import java.util.Objects;
import net.jmb19905.niftycarts.util.NiftyInventory;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.HasCustomInventoryScreen;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.ContainerEntity;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEvent.Context;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractDrawnInventoryEntity extends AbstractDrawnEntity implements HasCustomInventoryScreen, ContainerEntity {
   private NiftyInventory itemStacks;
   private final int containerSize;
   @Nullable
   private net.minecraft.resources.ResourceKey<net.minecraft.world.level.storage.loot.LootTable> lootTable;
   private long lootTableSeed;

   public AbstractDrawnInventoryEntity(
      net.minecraft.world.entity.EntityType<? extends net.minecraft.world.entity.Entity> entityTypeIn,
      net.minecraft.world.level.Level worldIn,
      int containerSize
   ) {
      super(entityTypeIn, worldIn);
      this.itemStacks = NiftyInventory.withSize(containerSize, ItemStack.EMPTY);
      this.containerSize = containerSize;
      this.itemStacks.setOnContentsChanged(this::onContentsChanged);
   }

   protected float getFillLevel() {
      float slots = (float)this.getItemStacks().stream().filter(s -> !s.isEmpty()).count();
      return slots / this.containerSize;
   }

   public boolean stillValid(@NotNull Player player) {
      return this.isChestVehicleStillValid(player);
   }

   @Override
   public void onDestroyedAndDoDrops(DamageSource source) {
      if (this.level() instanceof ServerLevel) {
         this.chestVehicleDestroyed(source, (ServerLevel)this.level(), this);
      }
   }

   public void remove(@NotNull RemovalReason removalReason) {
      if (!this.level().isClientSide() && removalReason.shouldDestroy()) {
         Containers.dropContents(this.level(), this, this);
      }

      super.remove(removalReason);
   }

   protected abstract InteractionResult onInteractNotOpen(Player var1, InteractionHand var2);

   protected boolean canInteractNotOpen() {
      return true;
   }

   @NotNull
   @Override
   public InteractionResult interact(@NotNull Player player, @NotNull InteractionHand interactionHand, @NotNull Vec3 vec) {
      if (this.isLocked()) {
         return InteractionResult.FAIL;
      } else if (this.canInteractNotOpen() && this.canAddPassenger(player) && !player.isSecondaryUseActive()) {
         return this.onInteractNotOpen(player, interactionHand);
      } else {
         InteractionResult interactionResult = this.interactWithContainerVehicle(player);
         if (interactionResult.consumesAction()) {
            this.gameEvent(GameEvent.CONTAINER_OPEN, player);
            if (this.level() instanceof ServerLevel serverLevel) {
               PiglinAi.angerNearbyPiglins(serverLevel, player, true);
            }
         }

         return interactionResult;
      }
   }

   public void openCustomInventoryScreen(Player player) {
      player.openMenu(this);
      if (!player.level().isClientSide()) {
         this.gameEvent(GameEvent.CONTAINER_OPEN, player);
         if (this.level() instanceof ServerLevel serverLevel) {
            PiglinAi.angerNearbyPiglins(serverLevel, player, true);
         }
      }
   }

   public void clearContent() {
      this.clearChestVehicleContent();
   }

   public int getContainerSize() {
      return this.containerSize;
   }

   @NotNull
   public ItemStack getItem(int i) {
      return this.getChestVehicleItem(i);
   }

   @NotNull
   public ItemStack removeItem(int i, int j) {
      return this.removeChestVehicleItem(i, j);
   }

   @NotNull
   public ItemStack removeItemNoUpdate(int i) {
      return this.removeChestVehicleItemNoUpdate(i);
   }

   public void setItem(int i, @NotNull ItemStack itemStack) {
      this.setChestVehicleItem(i, itemStack);
   }

   @NotNull
   public SlotAccess getSlot(int i) {
      return Objects.requireNonNull(this.getChestVehicleSlot(i));
   }

   public void setChanged() {
   }

   @Nullable
   public AbstractContainerMenu createMenu(int i, @NotNull Inventory inventory, @NotNull Player player) {
      if (this.lootTable != null && player.isSpectator()) {
         return null;
      } else {
         this.unpackLootTable(inventory.player);
         return this.createMenuLootUnpacked(i, inventory, player);
      }
   }

   protected abstract AbstractContainerMenu createMenuLootUnpacked(int var1, Inventory var2, Player var3);

   @NotNull
   public net.minecraft.core.NonNullList<net.minecraft.world.item.ItemStack> getItemStacks() {
      return this.itemStacks;
   }

   public void clearItemStacks() {
      this.itemStacks = NiftyInventory.withSize(this.getContainerSize(), ItemStack.EMPTY);
      this.itemStacks.setOnContentsChanged(this::onContentsChanged);
   }

   public void stopOpen(Player player) {
      this.level().gameEvent(GameEvent.CONTAINER_CLOSE, this.position(), Context.of(player));
   }

   protected void onContentsChanged(int slot) {
   }

   public void unpackLootTable(@Nullable Player player) {
      this.unpackChestVehicleLootTable(player);
   }

   @Nullable
   public net.minecraft.resources.ResourceKey<net.minecraft.world.level.storage.loot.LootTable> getContainerLootTable() {
      return this.lootTable;
   }

   public void setContainerLootTable(@Nullable net.minecraft.resources.ResourceKey<net.minecraft.world.level.storage.loot.LootTable> resourceLocation) {
      this.lootTable = resourceLocation;
   }

   public long getContainerLootTableSeed() {
      return this.lootTableSeed;
   }

   public void setContainerLootTableSeed(long l) {
      this.lootTableSeed = l;
   }

   @Override
   protected void addAdditionalSaveData(@NotNull ValueOutput output) {
      super.addAdditionalSaveData(output);
      this.saveInventory(output);
   }

   protected abstract void saveInventory(ValueOutput var1);

   @Override
   protected void readAdditionalSaveData(ValueInput input) {
      super.readAdditionalSaveData(input);
      this.readInventory(input);
   }

   protected abstract void readInventory(ValueInput var1);
}
