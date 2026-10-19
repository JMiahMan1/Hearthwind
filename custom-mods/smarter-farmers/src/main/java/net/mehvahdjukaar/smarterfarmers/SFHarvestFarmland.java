package net.mehvahdjukaar.smarterfarmers;

import com.google.common.base.Preconditions;
import com.mojang.datafixers.util.Pair;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.mehvahdjukaar.smarterfarmers.util.FrequencyOrderedCollection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction.Plane;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.behavior.HarvestFarmland;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEvent.Context;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

public class SFHarvestFarmland extends HarvestFarmland {
   public int plantTimer;
   public BlockPos aboveFarmlandPos = null;
   public ItemStack seedToHold = null;
   @VisibleForTesting
   public boolean active = false;
   @VisibleForTesting
   public List<Pair<net.minecraft.core.BlockPos, SFHarvestFarmland.Action>> farmlandAround;
   @VisibleForTesting
   public long lastTriedToStart;

   protected boolean checkExtraStartConditions(ServerLevel level, Villager owner) {
      this.lastTriedToStart = level.getGameTime();
      if (!level.getGameRules().get(GameRules.MOB_GRIEFING)) {
         return false;
      } else if (!owner.getVillagerData().profession().is(VillagerProfession.FARMER)) {
         return false;
      } else {
         this.farmlandAround = this.getValidFarmlandAround(level, owner);
         if (this.farmlandAround.isEmpty()) {
            return false;
         } else {
            Pair<net.minecraft.core.BlockPos, SFHarvestFarmland.Action> chosen = this.farmlandAround
               .get(level.getRandom().nextInt(this.farmlandAround.size()));
            this.aboveFarmlandPos = (BlockPos)chosen.getFirst();
            this.seedToHold = null;
            if (chosen.getSecond() == SFHarvestFarmland.Action.PLANT) {
               ItemStack seed = this.getSeedToPlantAt(this.aboveFarmlandPos, level, owner);
               if (!seed.isEmpty()) {
                  this.seedToHold = seed;
               } else {
                  this.farmlandAround.removeIf(e -> e.getSecond() == SFHarvestFarmland.Action.PLANT);
                  if (this.farmlandAround.isEmpty()) {
                     return false;
                  }

                  chosen = this.farmlandAround.get(level.getRandom().nextInt(this.farmlandAround.size()));
                  this.aboveFarmlandPos = (BlockPos)chosen.getFirst();
               }
            }

            return true;
         }
      }
   }

   private List<Pair<net.minecraft.core.BlockPos, SFHarvestFarmland.Action>> getValidFarmlandAround(
      net.minecraft.server.level.ServerLevel level, net.minecraft.world.entity.npc.villager.Villager owner
   ) {
      List<Pair<net.minecraft.core.BlockPos, SFHarvestFarmland.Action>> validFarmland = new ArrayList<>();
      MutableBlockPos mutableBlockPos = owner.blockPosition().mutable();

      for (int i = -1; i <= 1; i++) {
         for (int j = -1; j <= 1; j++) {
            for (int k = -1; k <= 1; k++) {
               mutableBlockPos.set(owner.getX() + i, owner.getY() + j, owner.getZ() + k);
               SFHarvestFarmland.Action actionForPos = this.getActionForPos(mutableBlockPos, level);
               if (actionForPos != null) {
                  validFarmland.add(Pair.of(new BlockPos(mutableBlockPos), actionForPos));
               }
            }
         }
      }

      return validFarmland;
   }

   protected ItemStack getSeedToPlantAt(BlockPos targetPos, ServerLevel level, Villager villager) {
      FrequencyOrderedCollection<Item> blockAsItemAround = new FrequencyOrderedCollection<>();
      MutableBlockPos mutableBlockPos = targetPos.mutable();
      boolean hasAirInNeighbor = false;

      for (int x = -1; x <= 1; x++) {
         for (int y = -1; y <= 1; y++) {
            for (int z = -1; z <= 1; z++) {
               if (x != 0 || z != 0) {
                  mutableBlockPos.set(targetPos.getX() + x, targetPos.getY() + y, targetPos.getZ() + z);
                  BlockState blockState = level.getBlockState(mutableBlockPos);
                  if (!hasAirInNeighbor && y == 0 && Math.abs(x) + Math.abs(z) == 1) {
                     hasAirInNeighbor = blockState.isAir();
                  }

                  Item item = blockState.getBlock().asItem();
                  if (item != Items.AIR) {
                     blockAsItemAround.add(item);
                  }
               }
            }
         }
      }

      SimpleContainer inventory = villager.getInventory();
      Set<net.minecraft.world.item.Item> availableSeeds = new HashSet<>();
      Map<net.minecraft.world.item.Item, net.minecraft.world.item.ItemStack> villagerSeedsInInventory = new HashMap<>();

      for (int i = 0; i < inventory.getContainerSize(); i++) {
         ItemStack itemStack = inventory.getItem(i);
         Item it = itemStack.getItem();
         if (!itemStack.isEmpty() && SFPlatformStuff.isValidSeed(itemStack, villager)) {
            availableSeeds.add(it);
            villagerSeedsInInventory.put(it, itemStack);
         }
      }

      if (availableSeeds.isEmpty()) {
         return ItemStack.EMPTY;
      } else {
         boolean failedForMelon = false;

         for (Item item : blockAsItemAround) {
            if (availableSeeds.contains(item)) {
               if (!item.getDefaultInstance().is(SmarterFarmers.STEM_SEEDS) || (targetPos.getX() + targetPos.getZ()) % 2 != 0) {
                  return (ItemStack)villagerSeedsInInventory.get(item);
               }

               failedForMelon = true;
            }
         }

         return failedForMelon ? ItemStack.EMPTY : (ItemStack)villagerSeedsInInventory.get(availableSeeds.iterator().next());
      }
   }

   protected void start(ServerLevel level, Villager entity, long gameTime) {
      this.active = true;
      this.plantTimer = SmarterFarmers.CONFIG.timeToHarvest;
      Preconditions.checkNotNull(this.aboveFarmlandPos);
      entity.setItemSlot(EquipmentSlot.MAINHAND, this.seedToHold != null ? this.seedToHold.copy() : FarmTaskLogic.getHoe(entity));
      entity.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new BlockPosTracker(this.aboveFarmlandPos));
      entity.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(new BlockPosTracker(this.aboveFarmlandPos), 0.5F, 1));
      if (net.fabricmc.loader.api.FabricLoader.getInstance().getEnvironmentType() == net.fabricmc.api.EnvType.CLIENT) {
         // debug overlay removed in the 26.2 port (render-state rework)
      }
   }

   protected void stop(ServerLevel level, Villager entity, long gameTime) {
      this.active = false;
      entity.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
      entity.getBrain().eraseMemory(MemoryModuleType.LOOK_TARGET);
      entity.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
      this.aboveFarmlandPos = null;
      this.farmlandAround.clear();
   }

   @Nullable
   protected SFHarvestFarmland.Action getActionForPos(BlockPos pos, ServerLevel level) {
      BlockState cropState = level.getBlockState(pos);
      BlockState farmState = level.getBlockState(pos.below());
      if (cropState.is(SmarterFarmers.HARVEST_BLACKLIST)) {
         return null;
      } else {
         if (FarmTaskLogic.isFarmland(farmState)) {
            if (FarmTaskLogic.isCropMature(cropState, pos, level)) {
               return SFHarvestFarmland.Action.HARVEST_AND_REPLANT;
            }

            if (cropState.isAir()) {
               return SFHarvestFarmland.Action.plantIfNoMelonsAround(pos, level);
            }
         }

         if (farmState.is(SmarterFarmers.FARMER_TILLABLE)) {
            if (cropState.is(SmarterFarmers.HARVEST_ON_TILLABLE_NO_REPLANT)) {
               return SFHarvestFarmland.Action.HARVEST;
            }

            if (cropState.is(SmarterFarmers.HARVEST_ON_TILLABLE)) {
               return SFHarvestFarmland.Action.HARVEST_AND_REPLANT;
            }

            if (cropState.isAir() && SmarterFarmers.CONFIG.plantOnTillable) {
               return SFHarvestFarmland.Action.plantIfNoMelonsAround(pos, level);
            }
         }

         return null;
      }
   }

   public void tick(ServerLevel level, Villager villager, long tickCount) {
      if (this.aboveFarmlandPos.closerToCenterThan(villager.position(), 2.0)) {
         this.plantTimer--;
         if (this.plantTimer <= 0) {
            BlockState targetState = level.getBlockState(this.aboveFarmlandPos);
            BlockPos belowPos = this.aboveFarmlandPos.below();
            Item toReplace = Items.AIR;
            if (!targetState.isAir()) {
               if (targetState.is(SmarterFarmers.HARVEST_ON_FARMLAND_SPECIAL)
                  || targetState.is(SmarterFarmers.HARVEST_ON_TILLABLE)
                  || targetState.is(SmarterFarmers.HARVEST_ON_TILLABLE_NO_REPLANT)) {
                  level.destroyBlock(this.aboveFarmlandPos, true, villager);
                  BlockState below = level.getBlockState(belowPos);
                  if (SFPlatformStuff.tillBlock(below, belowPos, level)) {
                     level.playSound(null, belowPos, SoundEvents.HOE_TILL, SoundSource.BLOCKS, 1.0F, 1.0F);
                  }

                  if (targetState.is(SmarterFarmers.HARVEST_ON_TILLABLE_NO_REPLANT)) {
                     this.aboveFarmlandPos = null;
                     return;
                  }
               } else if (FarmTaskLogic.isCropMature(targetState, this.aboveFarmlandPos, level)) {
                  toReplace = targetState.getBlock().asItem();
                  level.destroyBlock(this.aboveFarmlandPos, true, villager);
               }
            }

            BlockState farmlandBlock = level.getBlockState(belowPos);
            targetState = level.getBlockState(this.aboveFarmlandPos);
            if (targetState.isAir()) {
               if (FarmTaskLogic.isFarmland(farmlandBlock)) {
                  this.replant(level, villager, toReplace);
               } else if (farmlandBlock.is(SmarterFarmers.FARMER_TILLABLE) && SFPlatformStuff.tillBlock(farmlandBlock, belowPos, level)) {
                  level.playSound(null, belowPos, SoundEvents.HOE_TILL, SoundSource.BLOCKS, 1.0F, 1.0F);
                  this.replant(level, villager, toReplace);
               }
            }

            this.aboveFarmlandPos = null;
         }
      }
   }

   private void replant(ServerLevel level, Villager villager, Item toReplace) {
      ItemStack itemToPlant = ItemStack.EMPTY;
      if (toReplace != Items.AIR) {
         itemToPlant = this.findSameItem(villager.getInventory(), toReplace);
      }

      if (itemToPlant.isEmpty()) {
         itemToPlant = this.getSeedToPlantAt(this.aboveFarmlandPos, level, villager);
      }

      if (!itemToPlant.isEmpty()) {
         boolean success = false;
         if (SFPlatformStuff.trySpecialPlant(level, this.aboveFarmlandPos, itemToPlant, villager)) {
            success = true;
         } else if (itemToPlant.getItem() instanceof BlockItem blockItem) {
            BlockState toPlant = blockItem.getBlock().defaultBlockState();
            level.setBlock(this.aboveFarmlandPos, toPlant, 3);
            success = true;
         }

         if (success) {
            BlockState placed = level.getBlockState(this.aboveFarmlandPos);
            level.gameEvent(GameEvent.BLOCK_PLACE, this.aboveFarmlandPos, Context.of(villager, placed));
            level.playSound(
               null,
               this.aboveFarmlandPos.getX(),
               this.aboveFarmlandPos.getY(),
               this.aboveFarmlandPos.getZ(),
               SoundEvents.CROP_PLANTED,
               SoundSource.BLOCKS,
               1.0F,
               1.0F
            );
            itemToPlant.shrink(1);
         } else {
            SmarterFarmers.LOGGER.error("Failed to replant {} from item {}", toReplace, itemToPlant);
         }
      } else {
         SmarterFarmers.LOGGER.error("Failed to replant {}", toReplace);
      }
   }

   @NotNull
   private ItemStack findSameItem(SimpleContainer inventory, Item toReplace) {
      if (toReplace != Items.AIR && toReplace instanceof BlockItem) {
         for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack is = inventory.getItem(i);
            if (is.getItem() == toReplace) {
               return is;
            }
         }
      }

      return ItemStack.EMPTY;
   }

   protected boolean canStillUse(ServerLevel level, Villager entity, long gameTime) {
      return this.aboveFarmlandPos != null;
   }

   public static enum Action {
      HARVEST,
      HARVEST_AND_REPLANT,
      PLANT;

      @Nullable
      public static SFHarvestFarmland.Action plantIfNoMelonsAround(BlockPos pos, ServerLevel level) {
         for (Direction dir : Plane.HORIZONTAL) {
            BlockPos neighbor = pos.relative(dir);
            BlockState neighborState = level.getBlockState(neighbor);
            if (neighborState.getBlock() instanceof StemBlock) {
               boolean hasOtherSpace = false;

               for (Direction dir2 : Plane.HORIZONTAL) {
                  if (dir2 != dir.getOpposite()) {
                     BlockPos neighbor2 = neighbor.relative(dir2);
                     if (level.getBlockState(neighbor2).isAir()) {
                        hasOtherSpace = true;
                        break;
                     }
                  }
               }

               if (!hasOtherSpace) {
                  return null;
               }
            }
         }

         return PLANT;
      }

      public boolean harvests() {
         return this == HARVEST || this == HARVEST_AND_REPLANT;
      }
   }
}
