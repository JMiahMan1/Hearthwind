package com.github.legoatoom.connectiblechains.entity;

import com.github.legoatoom.connectiblechains.ConnectibleChains;
import com.github.legoatoom.connectiblechains.util.Helper;
import it.unimi.dsi.fastutil.ints.IntListIterator;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ChainCollisionEntity extends Entity implements ChainLinkEntity {
   private static final float COLLIDER_SPACING = 1.5F;
   @Nullable
   private final Chainable.ChainData link;
   private Entity chainedEntity;
   @NotNull
   private Item linkSourceItem;
   @Nullable
   private EntityOxidationHandler<ChainCollisionEntity> oxidationHandler;
   @Nullable
   private ChainCollisionEntity captain;

   public ChainCollisionEntity(Level world, double x, double y, double z, Entity chainedEntity, @NotNull Chainable.ChainData link) {
      super(ModEntityTypes.CHAIN_COLLISION, world);
      this.link = link;
      this.setPos(x, y, z);
      this.chainedEntity = chainedEntity;
      this.linkSourceItem = link.sourceItem;
   }

   public ChainCollisionEntity(net.minecraft.world.entity.EntityType<? extends ChainCollisionEntity> entityType, net.minecraft.world.level.Level world) {
      super(entityType, world);
      this.link = new Chainable.ChainData(0, Items.IRON_CHAIN);
      this.linkSourceItem = Items.IRON_CHAIN;
   }

   public boolean isCaptain() {
      return this.captain == null;
   }

   public void setCaptain(ChainCollisionEntity captain) {
      assert !captain.equals(this);

      this.captain = captain;
      this.oxidationHandler = captain.getOxidationHandler().orElse(null);
   }

   public static <E extends net.minecraft.world.entity.Entity & Chainable> void createCollision(E chainedEntity, Chainable.ChainData chainData) {
      if (chainData.collisionStorage.isEmpty()) {
         if (!chainedEntity.level().isClientSide()) {
            Entity chainHolder = chainedEntity.getChainHolder(chainData);
            if (chainHolder != null) {
               double distance = chainedEntity.distanceTo(chainHolder);
               double step = 1.5 * Math.sqrt(Math.pow(ModEntityTypes.CHAIN_COLLISION.getWidth(), 2.0) * 2.0) / distance;
               double v = step;
               double centerHoldout = ModEntityTypes.CHAIN_COLLISION.getWidth() / distance;
               ChainCollisionEntity captainCollision = spawnCollision(false, chainedEntity, chainData, 0.5);
               if (captainCollision == null) {
                  ConnectibleChains.LOGGER.warn("Was unable to create collisions for {}", chainData);
               } else {
                  chainData.collisionStorage.add(captainCollision.getId());

                  for (; v < 0.5 - centerHoldout; v += step) {
                     ChainCollisionEntity collider1 = spawnCollision(false, chainedEntity, chainData, v);
                     if (collider1 != null) {
                        chainData.collisionStorage.add(collider1.getId());
                        collider1.setCaptain(captainCollision);
                     }

                     ChainCollisionEntity collider2 = spawnCollision(true, chainedEntity, chainData, v);
                     if (collider2 != null) {
                        chainData.collisionStorage.add(collider2.getId());
                        collider2.setCaptain(captainCollision);
                     }
                  }
               }
            }
         }
      }
   }

   public static <E extends net.minecraft.world.entity.Entity & Chainable> ChainCollisionEntity spawnCollision(
      boolean reverse, E chainedEntity, Chainable.ChainData chainData, double distancePercentage
   ) {
      if (chainedEntity.level() instanceof ServerLevel serverWorld) {
         Entity var19 = chainedEntity.getChainHolder(chainData);

         assert var19 != null;

         Vec3 srcPos = chainedEntity.getChainPos(1.0F);
         Vec3 dstPos;
         if (var19 instanceof ChainKnotEntity chainKnotEntity) {
            dstPos = chainKnotEntity.getChainPos(1.0F);
         } else {
            dstPos = var19.getRopeHoldPosition(1.0F);
         }

         Vec3 tmp = dstPos;
         if (reverse) {
            dstPos = srcPos;
            srcPos = tmp;
         }

         double distance = srcPos.distanceTo(dstPos);
         double x = Mth.lerp(distancePercentage, srcPos.x(), dstPos.x());
         double y = srcPos.y() + Helper.drip2(distancePercentage * distance, distance, dstPos.y() - srcPos.y());
         double z = Mth.lerp(distancePercentage, srcPos.z(), dstPos.z());
         y += -ModEntityTypes.CHAIN_COLLISION.getHeight() + 0.125F;
         ChainCollisionEntity c = new ChainCollisionEntity(serverWorld, x, y, z, chainedEntity, chainData);
         if (serverWorld.addFreshEntity(c)) {
            return c;
         } else {
            ConnectibleChains.LOGGER.warn("Tried to summon collision entity for a chain, failed to do so");
            return null;
         }
      } else {
         return null;
      }
   }

   public static void destroyCollision(ServerLevel world, Chainable.ChainData chainData) {
      IntListIterator var2 = chainData.collisionStorage.iterator();

      while (var2.hasNext()) {
         Integer entityId = (Integer)var2.next();
         Entity e = world.getEntity(entityId);
         if (e instanceof ChainCollisionEntity) {
            e.discard();
         } else if (e != null) {
            ConnectibleChains.LOGGER.warn("Collision storage contained reference to {} (#{}) which is not a collision entity.", e, entityId);
         }
      }
   }

   @Nullable
   public Chainable.ChainData getLink() {
      return this.link;
   }

   public boolean isPickable() {
      return !this.isRemoved();
   }

   public boolean isPushable() {
      return false;
   }

   @Environment(EnvType.CLIENT)
   public boolean shouldRenderAtSqrDistance(double distance) {
      LocalPlayer player = Minecraft.getInstance().player;
      return player != null && player.isHolding(itemStack -> itemStack.is(ConventionalItemTags.SHEAR_TOOLS)) ? super.shouldRenderAtSqrDistance(distance) : false;
   }

   public void tick() {
      super.tick();
      if (this.isCaptain() && this.level() instanceof ServerLevel serverWorld) {
         this.getOxidationHandler().ifPresent(handler -> handler.updateWeathering(serverWorld.getRandom(), serverWorld.getGameTime()));
      }
   }

   public Optional<EntityOxidationHandler<ChainCollisionEntity>> getOxidationHandler() {
      if (Helper.isOxidizableSourceItem(this.getSourceItem()) && this.oxidationHandler == null) {
         this.oxidationHandler = new EntityOxidationHandler(this);
      }

      return Optional.ofNullable(this.oxidationHandler);
   }

   protected void readAdditionalSaveData(ValueInput view) {
      this.getOxidationHandler().ifPresent(handler -> handler.readCustomData(view));
   }

   protected void addAdditionalSaveData(ValueOutput view) {
      this.getOxidationHandler().ifPresent(handler -> handler.writeCustomData(view));
   }

   public boolean canBeCollidedWith(@Nullable Entity entity) {
      return true;
   }

   public boolean skipAttackInteraction(Entity attacker) {
      if (!super.skipAttackInteraction(attacker)) {
         this.playSound(Chainable.getSourceBlockSoundGroup(this.getSourceItem()).getHitSound(), 0.5F, 1.0F);
      }

      return false;
   }

   public boolean hurtServer(ServerLevel world, DamageSource source, float amount) {
      assert this.getLink() != null;

      InteractionResult result = this.onDamageFrom(source, Chainable.getSourceBlockSoundGroup(this.getSourceItem()).getHitSound());
      if (!result.consumesAction()) {
         return false;
      } else {
         if (this.chainedEntity instanceof Chainable chainable) {
            ConnectibleChains.LOGGER.debug("Dropping chain ({}) due to receiving damage from source: {}", this.getLink(), source);
            chainable.detachChain(this.getLink());
         }

         return true;
      }
   }

   public InteractionResult interact(Player player, InteractionHand hand) {
      if (this.getOxidationHandler().isPresent() && this.level() instanceof ServerLevel) {
         InteractionResult result = this.getOxidationHandler().get().interact(player, hand);
         if (result != null) {
            return result;
         }
      }

      return (InteractionResult)(player.getItemInHand(hand).is(ConventionalItemTags.SHEAR_TOOLS)
         ? InteractionResult.SUCCESS
         : InteractionResult.PASS);
   }

   protected void defineSynchedData(Builder builder) {
   }

   public net.minecraft.network.protocol.Packet<net.minecraft.network.protocol.game.ClientGamePacketListener> getAddEntityPacket(
      net.minecraft.server.level.ServerEntity entityTrackerEntry
   ) {
      int id = BuiltInRegistries.ITEM.getId(this.linkSourceItem);
      return new ClientboundAddEntityPacket(this, entityTrackerEntry, id);
   }

   public void recreateFromPacket(ClientboundAddEntityPacket packet) {
      super.recreateFromPacket(packet);
      int rawChainItemSourceId = packet.getData();
      this.setSourceItem((Item)BuiltInRegistries.ITEM.byId(rawChainItemSourceId));
   }

   public void thunderHit(ServerLevel world, LightningBolt lightning) {
      super.thunderHit(world, lightning);
      this.getOxidationHandler().ifPresent(handler -> handler.onStruckByLightning(lightning));
   }

   @Nullable
   public ItemStack getPickResult() {
      return new ItemStack(this.linkSourceItem);
   }

   @Override
   public void setSourceItem(Item sourceItem) {
      boolean hasNewItem = this.linkSourceItem != sourceItem;
      this.linkSourceItem = sourceItem;
      if (this.isCaptain()
         && this.level() instanceof ServerLevel
         && this.getLink() != null
         && hasNewItem
         && this.chainedEntity instanceof Chainable chainable) {
         chainable.detachChain(this.getLink(), false, false);
         chainable.attachChain(this.getLink().copyWithSource(sourceItem), null, true, false);
      }
   }

   @Override
   public Item getSourceItem() {
      return this.linkSourceItem;
   }
}
