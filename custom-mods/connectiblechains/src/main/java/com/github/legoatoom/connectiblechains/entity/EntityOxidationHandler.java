package com.github.legoatoom.connectiblechains.entity;

import com.google.common.collect.BiMap;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.HoneycombItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.UseRemainder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.WeatheringCopper.WeatherState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

public class EntityOxidationHandler<E extends net.minecraft.world.entity.Entity & ChainLinkEntity> {
   private static final long IGNORE_WEATHERING_TICK = -2L;
   private static final long UNSET_WEATHERING_TICK = -1L;
   private static final int WEATHERING_TICK_FROM = 50400;
   private static final int WEATHERING_TICK_TO = 55200;
   private final E entity;
   private long nextOxidationAge = -1L;
   @Nullable
   private UUID lastStruckLightning;

   public EntityOxidationHandler(E entity) {
      this.entity = entity;
      if (this.getDegradationLevel().isEmpty()) {
         this.nextOxidationAge = -2L;
      }
   }

   public Optional<net.minecraft.world.level.block.WeatheringCopper.WeatherState> getDegradationLevel() {
      return this.getBlock() instanceof WeatheringCopper oxidizable ? Optional.ofNullable((WeatherState)oxidizable.getAge()) : Optional.empty();
   }

   private Item getSourceItem() {
      return this.entity.getSourceItem();
   }

   private void setSourceItem(Item sourceItem) {
      this.entity.setSourceItem(sourceItem);
   }

   public Block getBlock() {
      return ((BlockItem)this.getSourceItem()).getBlock();
   }

   public void writeCustomData(ValueOutput view) {
      view.putLong("next_weather_age", this.nextOxidationAge);
   }

   public void readCustomData(ValueInput view) {
      this.nextOxidationAge = view.getLongOr("next_weather_age", -1L);
   }

   private void decreaseOxidation() {
      Optional<net.minecraft.world.level.block.Block> optionalBlock = WeatheringCopper.getPrevious(this.getBlock());
      if (!optionalBlock.isEmpty()) {
         this.setSourceItem(((Block)optionalBlock.get()).asItem());
      }
   }

   private void increaseOxidation() {
      Optional<net.minecraft.world.level.block.Block> optionalBlock = WeatheringCopper.getNext(this.getBlock());
      if (!optionalBlock.isEmpty()) {
         this.setSourceItem(((Block)optionalBlock.get()).asItem());
      }
   }

   private void setIgnoreWeathering(boolean waxed) {
      Optional<net.minecraft.world.level.block.Block> optionalBlock;
      if (waxed) {
         optionalBlock = Optional.ofNullable((Block)((BiMap)HoneycombItem.WAXABLES.get()).get(this.getBlock()));
      } else {
         optionalBlock = Optional.ofNullable((Block)((BiMap)HoneycombItem.WAX_OFF_BY_BLOCK.get()).get(this.getBlock()));
      }

      if (!optionalBlock.isEmpty()) {
         this.setSourceItem(((Block)optionalBlock.get()).asItem());
      }
   }

   public InteractionResult interact(Player player, InteractionHand hand) {
      ItemStack itemStack = player.getItemInHand(hand);
      Level world = this.entity.level();
      if (itemStack.is(Items.HONEYCOMB) && this.nextOxidationAge != -2L) {
         world.levelEvent(this.entity, 3003, this.entity.blockPosition(), 0);
         this.nextOxidationAge = -2L;
         this.setIgnoreWeathering(true);
         this.consume(player, hand, itemStack);
         return InteractionResult.SUCCESS_SERVER;
      } else {
         if (itemStack.is(ItemTags.AXES)) {
            if (this.nextOxidationAge == -2L) {
               world.playSound(null, this.entity.blockPosition(), SoundEvents.AXE_SCRAPE, this.entity.getSoundSource(), 1.0F, 1.0F);
               world.levelEvent(this.entity, 3004, this.entity.blockPosition(), 0);
               this.nextOxidationAge = -1L;
               this.setIgnoreWeathering(false);
               itemStack.hurtAndBreak(1, player, hand.asEquipmentSlot());
               return InteractionResult.SUCCESS_SERVER;
            }

            Optional<net.minecraft.world.level.block.WeatheringCopper.WeatherState> oxidationLevel = this.getDegradationLevel();
            if (oxidationLevel.isPresent() && oxidationLevel.get() != WeatherState.UNAFFECTED) {
               world.playSound(null, this.entity.blockPosition(), SoundEvents.AXE_SCRAPE, this.entity.getSoundSource(), 1.0F, 1.0F);
               world.levelEvent(this.entity, 3005, this.entity.blockPosition(), 0);
               this.nextOxidationAge = -1L;
               this.decreaseOxidation();
               itemStack.hurtAndBreak(1, player, hand.asEquipmentSlot());
               return InteractionResult.SUCCESS_SERVER;
            }
         }

         return null;
      }
   }

   private void consume(Player player, InteractionHand hand, ItemStack stack) {
      int i = stack.getCount();
      UseRemainder useRemainderComponent = (UseRemainder)stack.get(DataComponents.USE_REMAINDER);
      stack.consume(1, player);
      if (useRemainderComponent != null) {
         ItemStack itemStack = useRemainderComponent.convertIntoRemainder(stack, i, player.hasInfiniteMaterials(), player::handleExtraItemsCreatedOnUse);
         player.setItemInHand(hand, itemStack);
      }
   }

   public void updateWeathering(RandomSource random, long gameTime) {
      if (this.nextOxidationAge != -2L) {
         if (this.nextOxidationAge == -1L) {
            this.nextOxidationAge = gameTime + random.nextIntBetweenInclusive(50400, 55200);
         } else {
            Optional<net.minecraft.world.level.block.WeatheringCopper.WeatherState> oxidationLevel = this.getDegradationLevel();
            if (!oxidationLevel.isEmpty()) {
               boolean isFullyOxidized = ((WeatherState)oxidationLevel.get()).equals(WeatherState.OXIDIZED);
               if (gameTime >= this.nextOxidationAge && !isFullyOxidized) {
                  this.increaseOxidation();
                  boolean isNewStateFullyOxidized = ((WeatherState)oxidationLevel.get()).next().equals(WeatherState.OXIDIZED);
                  this.nextOxidationAge = isNewStateFullyOxidized ? 0L : this.nextOxidationAge + random.nextIntBetweenInclusive(50400, 55200);
               }
            }
         }
      }
   }

   public void onStruckByLightning(LightningBolt lightning) {
      UUID uUID = lightning.getUUID();
      if (!uUID.equals(this.lastStruckLightning)) {
         this.lastStruckLightning = uUID;
         this.getDegradationLevel().ifPresent(oxidationLevel -> {
            if (oxidationLevel != WeatherState.UNAFFECTED) {
               this.nextOxidationAge = -1L;
               this.decreaseOxidation();
            }
         });
      }
   }
}
