package net.jmb19905.niftycarts.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.UUID;
import net.jmb19905.niftycarts.NiftyCarts;
import net.jmb19905.niftycarts.entity.AbstractDrawnEntity;
import net.minecraft.core.UUIDUtil;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.level.storage.SavedDataStorage;

public class NiftyWorld extends SavedData {
   public static final Codec<NiftyWorld> CODEC = RecordCodecBuilder.create(
      instance -> instance.group(
            NiftyWorld.EntityWithId.CODEC
               .listOf()
               .optionalFieldOf("drawnList", List.of())
               .forGetter(
                  world -> world.pulling
                     .int2ObjectEntrySet()
                     .stream()
                     .map(entry -> new NiftyWorld.EntityWithId(entry.getIntKey(), (UUID)entry.getValue()))
                     .toList()
               )
         )
         .apply(instance, NiftyWorld::new)
   );
   public static final net.minecraft.world.level.saveddata.SavedDataType<NiftyWorld> TYPE = new SavedDataType<>(
      net.minecraft.resources.Identifier.fromNamespaceAndPath("niftycarts", "niftycarts_world"), NiftyWorld::new, CODEC, DataFixTypes.LEVEL
   );
   private static NiftyWorld clientInstance = null;
   private final Int2ObjectMap<UUID> pulling = new Int2ObjectOpenHashMap();

   public NiftyWorld() {
   }

   private NiftyWorld(List<NiftyWorld.EntityWithId> entityWithIds) {
      entityWithIds.forEach(entityWithId -> this.pulling.put(entityWithId.id, entityWithId.entityId));
   }

   public void addPulling(AbstractDrawnEntity drawn) {
      Entity pulling = drawn.getPulling();
      if (pulling != null) {
         this.setPulling(pulling.getId(), drawn);
      }
   }

   public void setPulling(int pullId, AbstractDrawnEntity drawn) {
      this.pulling.put(pullId, drawn.getUUID());
      this.setDirty();
   }

   public Optional<AbstractDrawnEntity> getDrawn(net.minecraft.world.entity.Entity e) {
      if (e == null) {
         return Optional.empty();
      } else {
         UUID drawnId = (UUID)this.pulling.get(e.getId());
         if (drawnId == null) {
            return Optional.empty();
         } else {
            return e.level().getEntity(drawnId) instanceof AbstractDrawnEntity drawn ? Optional.of(drawn) : Optional.empty();
         }
      }
   }

   public boolean isPulling(Entity e) {
      return this.pulling.containsKey(e.getId());
   }

   public void tick(Level level) {
      Iterator<Integer> it = this.pulling.keySet().iterator();

      while (it.hasNext()) {
         int pullId = it.next();
         UUID cartId = (UUID)this.pulling.get(pullId);
         if (level.getEntity(cartId) instanceof AbstractDrawnEntity cart) {
            if (cart.shouldStopPulledTick()) {
               it.remove();
               this.setDirty();
            } else if (!(cart.getPulling() instanceof AbstractDrawnEntity)) {
               cart.pulledTick();
            }
         } else {
            it.remove();
         }
      }
   }

   public Optional<net.minecraft.world.entity.Entity> getCurrentlyPulling(AbstractDrawnEntity drawn) {
      OptionalInt id = this.pulling.keySet().intStream().filter(pullID -> this.pulling.get(pullID) == drawn.getUUID()).findFirst();
      return id.isEmpty() ? Optional.empty() : Optional.ofNullable(drawn.level().getEntity(id.getAsInt()));
   }

   public Int2ObjectMap<UUID> getPulling() {
      return this.pulling;
   }

   public static NiftyWorld get(Level level) {
      return level.isClientSide() ? getClient() : getServer(NiftyCarts.server, level.dimension());
   }

   public static NiftyWorld getClient() {
      if (clientInstance == null) {
         clientInstance = new NiftyWorld();
      }

      return clientInstance;
   }

   public static NiftyWorld getServer(MinecraftServer server, net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> levelType) {
      SavedDataStorage dataStorage = Objects.requireNonNull(server.getLevel(levelType)).getDataStorage();
      return (NiftyWorld)dataStorage.computeIfAbsent(TYPE);
   }

   record EntityWithId(int id, UUID entityId) {
      public static final Codec<NiftyWorld.EntityWithId> CODEC = RecordCodecBuilder.create(
         instance -> instance.group(
               Codec.INT.fieldOf("id").forGetter(NiftyWorld.EntityWithId::id),
               UUIDUtil.CODEC.fieldOf("UUID").forGetter(NiftyWorld.EntityWithId::entityId)
            )
            .apply(instance, NiftyWorld.EntityWithId::new)
      );

      public static NiftyWorld.EntityWithId from(Entry<AbstractDrawnEntity> entry) {
         return new NiftyWorld.EntityWithId(entry.getIntKey(), ((AbstractDrawnEntity)entry.getValue()).getUUID());
      }
   }
}
