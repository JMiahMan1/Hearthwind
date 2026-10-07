package com.github.legoatoom.connectiblechains.entity;

import com.github.legoatoom.connectiblechains.ConnectibleChains;
import com.github.legoatoom.connectiblechains.migrator.ChainableMigrator;
import com.github.legoatoom.connectiblechains.networking.packet.ChainAttachS2CPacket;
import com.github.legoatoom.connectiblechains.tag.ModTagRegistry;
import com.mojang.datafixers.util.Either;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.decoration.BlockAttachedEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.ValueInput.ValueInputList;
import net.minecraft.world.level.storage.ValueOutput.ValueOutputList;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Chainable {
   String CHAINS_NBT_KEY = "connectiblechains_Chains";
   String SOURCE_ITEM_KEY = "connectiblechains_SourceItem";

   static double getMaxChainLength() {
      return ConnectibleChains.runtimeConfig.getMaxChainRange();
   }

   private static <E extends net.minecraft.world.entity.decoration.BlockAttachedEntity & Chainable> void resolveChainDataSet(
      E entity, HashSet<Chainable.ChainData> chainDataSet
   ) {
      if (entity.level() instanceof ServerLevel serverWorld) {
         for (Chainable.ChainData chainData : new HashSet<>(chainDataSet)) {
            if (chainData.unresolvedChainData != null) {
               Optional<UUID> optionalUUID = chainData.unresolvedChainData.left();
               Optional<net.minecraft.core.BlockPos> optionalRelPos = chainData.unresolvedChainData.right();
               if (optionalUUID.isPresent()) {
                  Entity chainHolder = serverWorld.getEntity(optionalUUID.get());
                  if (chainHolder != null) {
                     Chainable.ChainData newChainData = new Chainable.ChainData(chainHolder, chainData.sourceItem);
                     entity.replaceChainData(chainData, null);
                     attachChain(entity, newChainData, null, true, true);
                     continue;
                  }
               } else if (optionalRelPos.isPresent()) {
                  ChainKnotEntity chainHolder = ChainKnotEntity.getOrNull(serverWorld, entity.getPos().offset((Vec3i)optionalRelPos.get()));
                  if (chainHolder != null) {
                     Chainable.ChainData newChainData = new Chainable.ChainData(chainHolder, chainData.sourceItem);
                     entity.replaceChainData(chainData, null);
                     attachChain(entity, newChainData, null, true, true);
                     continue;
                  }
               }

               if (entity.tickCount > 100) {
                  ConnectibleChains.LOGGER.debug("Dropping chain connection as we have not been able to find chainholder for {}", chainData);
                  entity.spawnAtLocation(serverWorld, chainData.sourceItem);
                  entity.replaceChainData(chainData, null);
               }
            }
         }
      }
   }

   private static <E extends net.minecraft.world.entity.decoration.BlockAttachedEntity & Chainable> void detachChain(
      E entity, Chainable.ChainData chainData, boolean sendPacket, boolean dropItem, boolean playSound
   ) {
      if (chainData.chainHolder != null && chainData.isAlive()) {
         chainData.kill();
         entity.replaceChainData(chainData, null);
         if (playSound) {
            entity.playSound(chainData.getSourceBlockSoundGroup().getBreakSound(), 1.0F, 1.0F);
         }

         if (entity.level() instanceof ServerLevel serverWorld) {
            if (dropItem) {
               entity.spawnAtLocation(serverWorld, chainData.sourceItem);
            }

            if (sendPacket) {
               serverWorld.getChunkSource().sendToTrackingPlayers(entity, new ChainAttachS2CPacket(entity, chainData.chainHolder, null, chainData.sourceItem).asPacket());
            }

            ChainCollisionEntity.destroyCollision(serverWorld, chainData);
         }
      }
   }

   private static <E extends net.minecraft.world.entity.decoration.BlockAttachedEntity & Chainable> void attachChain(
      E entity, Chainable.ChainData chainData, @Nullable net.minecraft.world.entity.Entity previousHolder, boolean sendPacket, boolean playSound
   ) {
      if (chainData.chainHolder == null) {
         throw new IllegalArgumentException("Given chainData has empty holder");
      } else {
         entity.replaceChainData(entity.getChainData(previousHolder), chainData);
         if (playSound) {
            entity.playSound(chainData.getSourceBlockSoundGroup().getBreakSound(), 1.0F, 1.0F);
         }

         if (sendPacket && entity.level() instanceof ServerLevel serverWorld) {
            serverWorld.getChunkSource()
               .sendToTrackingPlayers(entity, new ChainAttachS2CPacket(entity, previousHolder, chainData.chainHolder, chainData.sourceItem).asPacket());
            if (chainData.chainHolder instanceof Chainable) {
               ChainCollisionEntity.createCollision(entity, chainData);
            }
         }
      }
   }

   static <E extends net.minecraft.world.entity.decoration.BlockAttachedEntity & Chainable> void tickChain(
      net.minecraft.server.level.ServerLevel world, E entity
   ) {
      HashSet<Chainable.ChainData> chainDataSet = entity.getChainDataSet();
      resolveChainDataSet(entity, chainDataSet);

      for (Chainable.ChainData chainData : new HashSet<>(chainDataSet)) {
         Entity chainHolder = entity.getChainHolder(chainData);
         if (chainHolder != null) {
            if (entity.isRemoved() || chainHolder.isRemoved()) {
               RemovalReason reason = entity.isRemoved() ? entity.getRemovalReason() : chainHolder.getRemovalReason();
               if (reason == null) {
                  throw new AssertionError();
               }

               if (reason.shouldDestroy()) {
                  if (entity.isRemoved()) {
                     ConnectibleChains.LOGGER.debug("Removing chain since chainReceiver ({}) is no longer alive, data: {}", entity, chainData);
                  } else {
                     ConnectibleChains.LOGGER.debug("Removing chain since chainHolder ({}) is no longer alive, data: {}", chainHolder, chainData);
                  }

                  entity.detachChain(chainData, (Boolean)world.getGameRules().get(GameRules.ENTITY_DROPS), true);
               }
            }

            chainHolder = entity.getChainHolder(chainData);
            if (chainHolder != null && chainHolder.level().equals(entity.level())) {
               float distanceTo = entity.distanceTo(chainHolder);
               if (entity.beforeChainTick(chainHolder, distanceTo) && distanceTo > getMaxChainLength()) {
                  entity.breakLongChain(chainData);
               }
            }
         }
      }
   }

   @Nullable
   private static <E extends net.minecraft.world.entity.decoration.BlockAttachedEntity & Chainable> net.minecraft.world.entity.Entity getChainHolder(
      E entity, Chainable.ChainData chainData
   ) {
      if (!entity.getChainDataSet().contains(chainData)) {
         return null;
      } else {
         if (chainData.unresolvedChainHolderId != 0 && entity.level().isClientSide()) {
            Entity chainHolder = entity.level().getEntity(chainData.unresolvedChainHolderId);
            if (chainHolder instanceof Entity) {
               entity.replaceChainData(chainData, new Chainable.ChainData(chainHolder, chainData.sourceItem));
            }
         }

         return chainData.chainHolder;
      }
   }

   static SoundType getSourceBlockSoundGroup(Item sourceItem) {
      if (sourceItem instanceof BlockItem blockItem) {
         return blockItem.getBlock().defaultBlockState().getSoundType();
      } else {
         return new ItemStack(sourceItem).is(ModTagRegistry.ROPES)
            ? new SoundType(
               1.0F,
               1.0F,
               SoundEvents.LEAD_UNTIED,
               SoundType.WOOL.getStepSound(),
               SoundEvents.LEAD_TIED,
               SoundType.WOOL.getHitSound(),
               SoundType.WOOL.getFallSound()
            )
            : SoundType.CHAIN;
      }
   }

   default boolean canAttachTo(Entity entity) {
      if (this == entity) {
         return false;
      } else if (this.getChainData(entity) != null) {
         return false;
      } else if (entity instanceof Chainable chainable) {
         return chainable.getChainData((Entity & ChainLinkEntity)this) != null ? false : this.getDistanceToCenter(entity) <= getMaxChainLength();
      } else {
         return false;
      }
   }

   default double getDistanceToCenter(Entity entity) {
      return entity.getBoundingBox().getCenter().distanceTo(((Entity & ChainLinkEntity)this).getBoundingBox().getCenter());
   }

   <E extends net.minecraft.world.entity.decoration.BlockAttachedEntity & Chainable> ChainableMigrator<E> getDataMigrator();

   HashSet<Chainable.ChainData> getChainDataSet();

   void replaceChainData(@Nullable Chainable.ChainData var1, @Nullable Chainable.ChainData var2);

   void setChainData(HashSet<Chainable.ChainData> var1);

   default void addUnresolvedChainHolderId(int unresolvedOldChainHolderId, int unresolvedNewChainHolderId, Item sourceItem) {
      Chainable.ChainData oldChainData = null;
      Chainable.ChainData newChainData = null;
      if (unresolvedOldChainHolderId != 0) {
         oldChainData = new Chainable.ChainData(unresolvedOldChainHolderId, sourceItem);
      }

      if (unresolvedNewChainHolderId != 0) {
         newChainData = new Chainable.ChainData(unresolvedNewChainHolderId, sourceItem);
      }

      this.replaceChainData(oldChainData, newChainData);
   }

   default void readChainData(ValueInput view) {
      view = this.getDataMigrator().migrate(view, (BlockAttachedEntity & Chainable)this);
      Item source = view.getString(SOURCE_ITEM_KEY)
         .map(sourceKey -> (Item)BuiltInRegistries.ITEM.getValue(Identifier.tryParse(sourceKey)))
         .orElse(Items.IRON_CHAIN);
      this.setSourceItem(source);
      HashSet<Chainable.ChainData> chainData = readChainDataSet(view);
      if (!this.getChainDataSet().isEmpty() && chainData.isEmpty()) {
         this.detachAllChains(false);
      }

      this.setChainData(chainData);
   }

   default void writeChainData(net.minecraft.world.level.storage.ValueOutput view, HashSet<Chainable.ChainData> chainDataSet) {
      this.getDataMigrator().addVersionTag(view);
      view.store(SOURCE_ITEM_KEY, BuiltInRegistries.ITEM.byNameCodec(), this.getSourceItem());
      writeChainDataSet((BlockAttachedEntity & Chainable)this, chainDataSet, view);
   }

   private static <E extends net.minecraft.world.entity.decoration.BlockAttachedEntity & Chainable> HashSet<Chainable.ChainData> readChainDataSet(
      net.minecraft.world.level.storage.ValueInput view
   ) {
      HashSet<Chainable.ChainData> result = new HashSet<>();
      Optional<net.minecraft.world.level.storage.ValueInput.ValueInputList> optionalList = view.childrenList(CHAINS_NBT_KEY);
      if (optionalList.isPresent()) {
         for (ValueInput element : (ValueInputList)optionalList.get()) {
            Item source = element.read(SOURCE_ITEM_KEY, BuiltInRegistries.ITEM.byNameCodec()).orElse(Items.IRON_CHAIN);
            element.read("UUID", UUIDUtil.AUTHLIB_CODEC).ifPresent(uuid -> result.add(new Chainable.ChainData(Either.left(uuid), source)));
            element.read("RelativePos", BlockPos.CODEC).ifPresent(vec3i -> result.add(new Chainable.ChainData(Either.right(vec3i), source)));
         }
      }

      return result;
   }

   private static <E extends net.minecraft.world.entity.decoration.BlockAttachedEntity & Chainable> void writeChainDataSet(
      E entity, HashSet<Chainable.ChainData> chainDataSet, net.minecraft.world.level.storage.ValueOutput writeView
   ) {
      ValueOutputList linksTag = writeView.childrenList(CHAINS_NBT_KEY);

      for (Chainable.ChainData chainData : chainDataSet) {
         Either<UUID, net.minecraft.core.BlockPos> either;
         if (chainData.chainHolder instanceof ChainKnotEntity chainKnotEntity) {
            either = Either.right(chainKnotEntity.getPos().subtract(entity.getPos()));
         } else if (chainData.chainHolder != null) {
            either = Either.left(chainData.chainHolder.getUUID());
         } else {
            either = chainData.unresolvedChainData;
         }

         if (either != null) {
            ValueOutput tag = linksTag.addChild();
            either.ifLeft(uuid -> tag.store("UUID", UUIDUtil.AUTHLIB_CODEC, uuid))
               .ifRight(relPos -> tag.store("RelativePos", BlockPos.CODEC, relPos));
            tag.store(SOURCE_ITEM_KEY, BuiltInRegistries.ITEM.byNameCodec(), chainData.sourceItem);
         }
      }
   }

   default void detachChain(Chainable.ChainData chainData) {
      this.detachChain(chainData, true, true);
   }

   default void detachChain(Chainable.ChainData chainData, boolean dropItem, boolean playSound) {
      detachChain((BlockAttachedEntity & Chainable)this, chainData, true, dropItem, playSound);
   }

   default void detachAllChains(boolean dropItems) {
      for (Chainable.ChainData chainData : new HashSet<>(this.getChainDataSet())) {
         this.detachChain(chainData, dropItems, true);
      }
   }

   default boolean beforeChainTick(Entity chainHolder, float distance) {
      return true;
   }

   default void breakLongChain(Chainable.ChainData chainData) {
      ConnectibleChains.LOGGER.debug("Breaking chain as it is too long! {}", chainData);
      this.detachChain(chainData);
   }

   default void attachChain(Chainable.ChainData chainData, @Nullable Entity previousHolder, boolean sendPacket) {
      this.attachChain(chainData, previousHolder, sendPacket, true);
   }

   default void attachChain(Chainable.ChainData chainData, @Nullable Entity previousHolder, boolean sendPacket, boolean playSound) {
      attachChain((BlockAttachedEntity & Chainable)this, chainData, previousHolder, sendPacket, playSound);
   }

   @Nullable
   default Entity getChainHolder(Chainable.ChainData chainData) {
      return getChainHolder((BlockAttachedEntity & Chainable)this, chainData);
   }

   @Nullable
   default Chainable.ChainData getChainData(@Nullable Entity holder) {
      if (holder != null) {
         for (Chainable.ChainData chainData : new HashSet<>(this.getChainDataSet())) {
            if (this.getChainHolder(chainData) == holder) {
               return chainData;
            }
         }
      }

      return null;
   }

   Item getSourceItem();

   void setSourceItem(Item var1);

   default SoundType getSourceBlockSoundGroup() {
      return getSourceBlockSoundGroup(this.getSourceItem());
   }

   Vec3 getChainPos(float var1);

   public static final class ChainData {
      private boolean isDead = false;
      public final IntArrayList collisionStorage = new IntArrayList(16);
      @Nullable
      public Either<UUID, net.minecraft.core.BlockPos> unresolvedChainData;
      @NotNull
      public final Item sourceItem;
      final int unresolvedChainHolderId;
      @Nullable
      private final Entity chainHolder;

      @Nullable
      public Entity getChainHolder() {
         return this.chainHolder;
      }

      public ChainData(@Nullable Either<UUID, net.minecraft.core.BlockPos> unresolvedChainData, @NotNull net.minecraft.world.item.Item sourceItem) {
         this.unresolvedChainData = unresolvedChainData;
         this.sourceItem = sourceItem;
         this.chainHolder = null;
         this.unresolvedChainHolderId = 0;
      }

      public ChainData(@Nullable Entity chainHolder, @NotNull Item sourceItem) {
         this.chainHolder = chainHolder;
         this.sourceItem = sourceItem;
         this.unresolvedChainData = null;
         this.unresolvedChainHolderId = 0;
      }

      public ChainData(int unresolvedChainHolderId, @NotNull Item sourceItem) {
         this.unresolvedChainHolderId = unresolvedChainHolderId;
         this.sourceItem = sourceItem;
         this.chainHolder = null;
         this.unresolvedChainData = null;
      }

      public SoundType getSourceBlockSoundGroup() {
         return Chainable.getSourceBlockSoundGroup(this.sourceItem);
      }

      private int getHolderId() {
         return this.chainHolder != null ? this.chainHolder.getId() : this.unresolvedChainHolderId;
      }

      @Override
      public boolean equals(Object o) {
         if (o instanceof Chainable.ChainData chainData) {
            int thisId = this.getHolderId();
            int thatId = chainData.getHolderId();
            return thisId != 0 && thisId == thatId ? true : this.unresolvedChainData != null && this.unresolvedChainData.equals(chainData.unresolvedChainData);
         } else {
            return false;
         }
      }

      public void kill() {
         if (this.isDead) {
            ConnectibleChains.LOGGER.warn("Stop! Stop! {} is already dead!", this);
         }

         this.isDead = true;
      }

      public boolean isAlive() {
         return !this.isDead;
      }

      @Override
      public int hashCode() {
         return Objects.hash(this.getHolderId());
      }

      public void applyRotation(Rotation rotation) {
         if (this.unresolvedChainData != null) {
            this.unresolvedChainData = this.unresolvedChainData.mapRight(blockPos -> blockPos.rotate(rotation));
         }
      }

      @Override
      public String toString() {
         return "ChainData{collisionStorage="
            + this.collisionStorage
            + ", unresolvedChainData="
            + this.unresolvedChainData
            + ", sourceItem="
            + this.sourceItem
            + ", unresolvedChainHolderId="
            + this.unresolvedChainHolderId
            + ", chainHolder="
            + this.chainHolder
            + "}";
      }

      public Chainable.ChainData copyWithSource(Item sourceItem) {
         if (this.chainHolder != null) {
            return new Chainable.ChainData(this.chainHolder, sourceItem);
         } else {
            return this.unresolvedChainData != null
               ? new Chainable.ChainData(this.unresolvedChainData, sourceItem)
               : new Chainable.ChainData(this.unresolvedChainHolderId, sourceItem);
         }
      }
   }
}
