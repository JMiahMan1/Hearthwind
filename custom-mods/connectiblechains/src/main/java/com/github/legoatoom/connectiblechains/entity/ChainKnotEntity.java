package com.github.legoatoom.connectiblechains.entity;

import com.github.legoatoom.connectiblechains.ConnectibleChains;
import com.github.legoatoom.connectiblechains.item.ChainItemCallbacks;
import com.github.legoatoom.connectiblechains.migrator.ChainableMigrator;
import com.github.legoatoom.connectiblechains.networking.packet.ChainAttachS2CPacket;
import com.github.legoatoom.connectiblechains.tag.ModTagRegistry;
import com.github.legoatoom.connectiblechains.util.Helper;
import java.util.HashSet;
import java.util.Optional;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.decoration.BlockAttachedEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ChainKnotEntity extends BlockAttachedEntity implements Chainable, ChainLinkEntity {
   public static final ChainableMigrator<ChainKnotEntity> CHAIN_KNOT_DATA_MIGRATOR = new ChainableMigrator();
   private HashSet<Chainable.ChainData> chainDataSet = new HashSet<>();
   @Nullable
   private EntityOxidationHandler<ChainKnotEntity> oxidationHandler;
   private static final net.minecraft.network.syncher.EntityDataAccessor<net.minecraft.world.item.ItemStack> SOURCE_ITEM = SynchedEntityData.defineId(
      ChainKnotEntity.class, EntityDataSerializers.ITEM_STACK
   );

   protected ChainKnotEntity(
      net.minecraft.world.entity.EntityType<? extends net.minecraft.world.entity.decoration.BlockAttachedEntity> entityType,
      net.minecraft.world.level.Level world
   ) {
      super(entityType, world);
   }

   public ChainKnotEntity(
      net.minecraft.world.entity.EntityType<? extends net.minecraft.world.entity.decoration.BlockAttachedEntity> entityType,
      net.minecraft.world.level.Level world,
      net.minecraft.core.BlockPos pos,
      @NotNull net.minecraft.world.item.Item sourceItem
   ) {
      super(entityType, world, pos);
      this.setSourceItem(sourceItem);
      this.setPos(pos.getX(), pos.getY(), pos.getZ());
   }

   public ChainKnotEntity(Level world, BlockPos pos, @NotNull Item sourceItem) {
      this(ModEntityTypes.CHAIN_KNOT, world, pos, sourceItem);
   }

   @Nullable
   public static ChainKnotEntity getOrNull(Level world, BlockPos pos) {
      for (ChainKnotEntity chainKnotEntity : world.getEntitiesOfClass(
         ChainKnotEntity.class,
         new AABB(pos.getX(), pos.getY(), pos.getZ(), pos.getX(), pos.getY(), pos.getZ()).inflate(1.0)
      )) {
         if (chainKnotEntity.getPos().equals(pos)) {
            return chainKnotEntity;
         }
      }

      return null;
   }

   public static ChainKnotEntity getOrCreate(Level world, BlockPos pos, @NotNull Item newSourceItem) {
      ChainKnotEntity chainKnotEntity = getOrNull(world, pos);
      if (chainKnotEntity == null) {
         chainKnotEntity = new ChainKnotEntity(world, pos, newSourceItem);
         world.addFreshEntity(chainKnotEntity);
      }

      return chainKnotEntity;
   }

   public Optional<EntityOxidationHandler<ChainKnotEntity>> getOxidationHandler() {
      if (Helper.isOxidizableSourceItem(this.getSourceItem()) && this.oxidationHandler == null) {
         this.oxidationHandler = new EntityOxidationHandler(this);
      }

      return Optional.ofNullable(this.oxidationHandler);
   }

   @Override
   public ChainableMigrator<ChainKnotEntity> getDataMigrator() {
      return CHAIN_KNOT_DATA_MIGRATOR;
   }

   @Override
   public HashSet<Chainable.ChainData> getChainDataSet() {
      return this.chainDataSet;
   }

   @Override
   public void replaceChainData(@Nullable Chainable.ChainData oldChainData, @Nullable Chainable.ChainData newChainData) {
      if (oldChainData != null && !this.getChainDataSet().removeIf(chainData -> chainData.equals(oldChainData) || chainData.equals(newChainData))) {
         ConnectibleChains.LOGGER.warn("Attempted to remove {}, from {}. But it was not able to find it?", oldChainData, this.getChainDataSet());
      }

      if (newChainData != null) {
         this.getChainDataSet().add(newChainData);
      }
   }

   @Override
   public void setChainData(HashSet<Chainable.ChainData> chainDataSet) {
      this.chainDataSet = chainDataSet;
   }

   public void tick() {
      super.tick();
      if (this.level() instanceof ServerLevel serverWorld) {
         Chainable.tickChain(serverWorld, this);
         this.getOxidationHandler().ifPresent(handler -> handler.updateWeathering(serverWorld.getRandom(), serverWorld.getGameTime()));
      }
   }

   public void thunderHit(ServerLevel world, LightningBolt lightning) {
      super.thunderHit(world, lightning);
      this.getOxidationHandler().ifPresent(handler -> handler.onStruckByLightning(lightning));
   }

   public InteractionResult interact(Player player, InteractionHand hand) {
      if (this.getOxidationHandler().isPresent()) {
         InteractionResult result = this.getOxidationHandler().get().interact(player, hand);
         if (result != null) {
            return result;
         }
      }

      ItemStack handStack = player.getItemInHand(hand);
      if (this.level().isClientSide()) {
         Chainable.ChainData chainDataForPlayer = this.getChainData(player);
         if (chainDataForPlayer != null) {
            if (!player.hasInfiniteMaterials()) {
               player.addItem(new ItemStack(chainDataForPlayer.sourceItem));
            }

            return InteractionResult.SUCCESS;
         } else if (handStack.is(ModTagRegistry.CATENARY_ITEMS)) {
            return InteractionResult.SUCCESS;
         } else {
            return (InteractionResult)(handStack.is(ConventionalItemTags.SHEAR_TOOLS) ? InteractionResult.CONSUME : InteractionResult.PASS);
         }
      } else {
         if (this.isAlive() && player.level() instanceof ServerLevel serverWorld) {
            boolean hasConnectedFromPlayer = false;

            for (Chainable chainable : ChainItemCallbacks.collectChainablesAround(
               this.level(), this.getPos(), entity -> entity.getChainData(player) != null
            )) {
               Chainable.ChainData chainData = chainable.getChainData(player);
               if (chainData != null && chainable.canAttachTo(this)) {
                  chainable.attachChain(new Chainable.ChainData(this, chainData.sourceItem), player, true);
                  hasConnectedFromPlayer = true;
               }
            }

            if (hasConnectedFromPlayer) {
               this.onPlace();
               return InteractionResult.SUCCESS;
            }

            Chainable.ChainData matchingData = null;

            for (Chainable.ChainData chainData : new HashSet<>(this.getChainDataSet())) {
               if (player == this.getChainHolder(chainData)) {
                  matchingData = chainData;
                  break;
               }
            }

            if (matchingData != null) {
               this.detachChain(matchingData, false, true);
               if (!player.hasInfiniteMaterials()) {
                  player.addItem(new ItemStack(matchingData.sourceItem));
               }

               this.gameEvent(GameEvent.ENTITY_INTERACT, player);
               return InteractionResult.SUCCESS.withoutItem();
            }

            if (handStack.is(ModTagRegistry.CATENARY_ITEMS)) {
               this.onPlace();
               this.attachChain(new Chainable.ChainData(player, handStack.getItem()), null, true);
               handStack.consume(1, player);
               return InteractionResult.SUCCESS;
            }

            if (handStack.is(ConventionalItemTags.SHEAR_TOOLS)) {
               ConnectibleChains.LOGGER.debug("Removing all connections due to player {} action on chain: {}", player, this);
               this.detachAllChains(!player.hasInfiniteMaterials());
               this.remove(RemovalReason.DISCARDED);
               this.dropItem(serverWorld, player);
               return InteractionResult.CONSUME;
            }
         }

         return InteractionResult.PASS;
      }
   }

   public boolean skipAttackInteraction(Entity attacker) {
      if (!super.skipAttackInteraction(attacker)) {
         this.playSound(this.getSourceBlockSoundGroup().getHitSound(), 0.5F, 1.0F);
      }

      return true;
   }

   public boolean hurtServer(ServerLevel world, DamageSource source, float amount) {
      InteractionResult result = this.onDamageFrom(source, this.getSourceBlockSoundGroup().getHitSound());
      if (!result.consumesAction()) {
         return false;
      } else {
         if (result == InteractionResult.SUCCESS) {
            ConnectibleChains.LOGGER.debug("Dropping all chains from knot ({}) due to receiving damage from source: {}", this, source);
            this.detachAllChains(true);
         }

         return super.hurtServer(world, source, amount);
      }
   }

   protected void removeAfterChangingDimensions() {
      super.removeAfterChangingDimensions();
      this.detachAllChains(false);
   }

   public void addAdditionalSaveData(ValueOutput view) {
      this.writeChainData(view, this.getChainDataSet());
      this.getOxidationHandler().ifPresent(handler -> handler.writeCustomData(view));
   }

   public void readAdditionalSaveData(ValueInput view) {
      this.readChainData(view);
      this.getOxidationHandler().ifPresent(handler -> handler.readCustomData(view));
   }

   protected void recalculateBoundingBox() {
      this.setPosRaw(this.pos.getX() + 0.5, this.pos.getY() + 0.5, this.pos.getZ() + 0.5);
      double width = this.getType().getWidth() / 2.0;
      double height = this.getType().getHeight();
      this.setBoundingBox(
         new AABB(
            this.getX() - width,
            this.getY(),
            this.getZ() - width,
            this.getX() + width,
            this.getY() + height,
            this.getZ() + width
         )
      );
   }

   public boolean survives() {
      return this.level().getBlockState(this.pos).is(ModTagRegistry.CHAIN_CONNECTIBLE);
   }

   @NotNull
   @Override
   public Item getSourceItem() {
      return ((ItemStack)this.entityData.get(SOURCE_ITEM)).getItem();
   }

   @Override
   public void setSourceItem(@NotNull Item sourceItem) {
      this.entityData.set(SOURCE_ITEM, new ItemStack(sourceItem));
   }

   public void onPlace() {
      this.playSound(this.getSourceBlockSoundGroup().getPlaceSound(), 1.0F, 1.0F);
   }

   public void dropItem(ServerLevel world, @Nullable Entity breaker) {
      this.playSound(this.getSourceBlockSoundGroup().getBreakSound(), 1.0F, 1.0F);
   }

   public void startSeenByPlayer(ServerPlayer player) {
      for (Chainable.ChainData chainData : this.getChainDataSet()) {
         ServerPlayNetworking.send(player, new ChainAttachS2CPacket(this, null, this.getChainHolder(chainData), chainData.sourceItem));
      }
   }

   protected void defineSynchedData(Builder builder) {
      builder.define(SOURCE_ITEM, new ItemStack(Items.IRON_CHAIN));
   }

   public float rotate(Rotation rotation) {
      this.getChainDataSet().forEach(chainData -> chainData.applyRotation(rotation));
      return super.rotate(rotation);
   }

   public float mirror(Mirror mirror) {
      return this.rotate(mirror.getRotation(this.getDirection()));
   }

   @Nullable
   public ItemStack getPickResult() {
      return new ItemStack(this.getSourceItem());
   }

   @Override
   public Vec3 getChainPos(float delta) {
      return this.getPosition(delta).add(0.0, 0.2, 0.0);
   }
}
