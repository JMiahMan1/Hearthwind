package net.dungeonz.block.logic;

import com.mojang.logging.LogUtils;
import java.util.Optional;

import net.dungeonz.dungeon.Dungeon;
import net.dungeonz.dungeon.DungeonPlacementHandler;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntitySpawnRequest;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.EntityGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.SpawnData;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.core.registries.BuiltInRegistries;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public abstract class DungeonSpawnerLogic {
    private static final Logger LOGGER = LogUtils.getLogger();
    private int spawnDelay = 20;
    private WeightedList<SpawnData> spawnPotentials = WeightedList.of();
    private SpawnData spawnEntry = new SpawnData();
    private double randomParticleValueOne;
    private double randomParticleValueTwo;
    private int minSpawnDelay = 200;
    private int maxSpawnDelay = 800;
    private int spawnCount = 4;
    @Nullable
    private Entity renderedEntity;
    private int maxNearbyEntities = 6;
    private int requiredPlayerRange = 16;
    private int spawnRange = 4;
    private int maxSpawnCount = 0;
    private int totalSpawnCount = 0;
    private String difficulty = "";
    private Dungeon dungeon = null;
    private int entityTypeId = 0;

    private boolean isPlayerInRange(Level world, BlockPos pos) {
        return isPlayerInRange(world, (double) pos.getX() + 0.5, (double) pos.getY() + 0.5, (double) pos.getZ() + 0.5, this.requiredPlayerRange);
    }

    private boolean isPlayerInRange(EntityGetter world, double x, double y, double z, double range) {
        for (Player playerEntity : world.players()) {
            if (!EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(playerEntity) || !EntitySelector.LIVING_ENTITY_STILL_ALIVE.test(playerEntity))
                continue;
            double d = playerEntity.distanceToSqr(x, y, z);
            if (!(range < 0.0) && !(d < range * range))
                continue;
            return true;
        }
        return false;
    }

    public void clientTick(Level world, BlockPos pos) {
        if (!this.isPlayerInRange(world, pos)) {
            this.randomParticleValueTwo = this.randomParticleValueOne;
        } else {
            RandomSource random = world.getRandom();
            double d = (double) pos.getX() + random.nextDouble();
            double e = (double) pos.getY() + random.nextDouble();
            double f = (double) pos.getZ() + random.nextDouble();
            world.addParticle(ParticleTypes.SMOKE, d, e, f, 0.0, 0.0, 0.0);
            world.addParticle(ParticleTypes.FLAME, d, e, f, 0.0, 0.0, 0.0);
            if (this.spawnDelay > 0) {
                --this.spawnDelay;
            }
            this.randomParticleValueTwo = this.randomParticleValueOne;
            this.randomParticleValueOne = (this.randomParticleValueOne + (double) (1000.0f / ((float) this.spawnDelay + 200.0f))) % 360.0;
        }
    }

    public void serverTick(ServerLevel world, BlockPos pos) {
        if (!this.isPlayerInRange(world, pos)) {
            return;
        }
        if (this.spawnDelay == -1) {
            this.updateSpawns(world, pos);
        }
        if (this.spawnDelay > 0) {
            --this.spawnDelay;
            return;
        }

        boolean bl = false;
        for (int i = 0; i < this.spawnCount; ++i) {
            // MobSpawnerEntry.CustomSpawnRules customSpawnRules;
            double f;
            CompoundTag nbtCompound = this.spawnEntry.getEntityToSpawn();
            Optional<EntityType<?>> optional = EntityType.by(TagValueInput.create(ProblemReporter.DISCARDING, world.registryAccess(), nbtCompound));
            if (optional.isEmpty()) {
                this.updateSpawns(world, pos);
                return;
            }
            ListTag nbtList = nbtCompound.getListOrEmpty("Pos");
            int j = nbtList.size();
            RandomSource random = world.getRandom();
            double d = j >= 1 ? nbtList.getDoubleOr(0, 0.0) : (double) pos.getX() + (random.nextDouble() - random.nextDouble()) * (double) this.spawnRange + 0.5;
            double e = j >= 2 ? nbtList.getDoubleOr(1, 0.0) : (double) (pos.getY() + random.nextInt(3) - 1);
            f = j >= 3 ? nbtList.getDoubleOr(2, 0.0) : (double) pos.getZ() + (random.nextDouble() - random.nextDouble()) * (double) this.spawnRange + 0.5;
            if (!world.noCollision(optional.get().getSpawnAABB(d, e, f))) {
                continue;
            }
            BlockPos blockPos = BlockPos.containing(d, e, f);
            // if (!this.spawnEntry.getCustomSpawnRules().isPresent() ? !SpawnRestriction.canSpawn(optional.get(), world, SpawnReason.SPAWNER, blockPos, world.getRandom())
            // : !optional.get().getSpawnGroup().isPeaceful() && world.getDifficulty() == Difficulty.PEACEFUL
            // || !(customSpawnRules = this.spawnEntry.getCustomSpawnRules().get()).blockLightLimit().contains(world.getLightLevel(LightType.BLOCK, blockPos))
            // || !customSpawnRules.skyLightLimit().contains(world.getLightLevel(LightType.SKY, blockPos)))
            if (!SpawnPlacements.checkSpawnRules(optional.get(), world, EntitySpawnReason.SPAWNER, blockPos, world.getRandom()))
                continue;
            Entity entity2 = EntityType.loadEntityRecursive(nbtCompound, world, new EntitySpawnRequest(EntitySpawnReason.SPAWNER, true), entity -> {
                entity.setPos(d, e, f);
                return entity;
            });
            if (entity2 == null) {
                this.updateSpawns(world, pos);
                return;
            }
            int k = world.getEntitiesOfClass(entity2.getClass(), new AABB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1).inflate(this.spawnRange)).size();
            if (k >= this.maxNearbyEntities) {
                this.updateSpawns(world, pos);
                return;
            }
            entity2.setPos(entity2.getX(), entity2.getY(), entity2.getZ());
            entity2.setYRot(random.nextFloat() * 360.0f);
            entity2.setXRot(0.0f);
            if (entity2 instanceof Mob) {
                Mob mobEntity = (Mob) entity2;
                if (this.spawnEntry.getCustomSpawnRules().isEmpty() && !mobEntity.checkSpawnRules(world, EntitySpawnReason.SPAWNER) || !mobEntity.checkSpawnObstruction(world))
                    continue;
                if (this.spawnEntry.getEntityToSpawn().size() == 1 && this.spawnEntry.getEntityToSpawn().contains("id")) {
                    ((Mob) entity2).finalizeSpawn(world, world.getCurrentDifficultyAt(entity2.blockPosition()), EntitySpawnReason.SPAWNER, null);
                }
                if (dungeon != null) {
                    DungeonPlacementHandler.strengthenMob(mobEntity, dungeon, difficulty, false);
                }
            }
            if (!world.tryAddFreshEntityWithPassengers(entity2)) {
                this.totalSpawnCount++;
                this.updateSpawns(world, pos);
                return;
            }
            world.levelEvent(LevelEvent.PARTICLES_MOBBLOCK_SPAWN, pos, 0);
            world.gameEvent(entity2, GameEvent.ENTITY_PLACE, blockPos);
            if (entity2 instanceof Mob) {
                ((Mob) entity2).spawnAnim();
            }
            bl = true;
        }
        if (bl) {
            this.updateSpawns(world, pos);
        }
        if (this.maxSpawnCount != 0 && this.maxSpawnCount <= this.totalSpawnCount) {
            world.destroyBlock(pos, false);
        }
    }

    private void updateSpawns(Level world, BlockPos pos) {
        RandomSource random = world.getRandom();
        this.spawnDelay = this.maxSpawnDelay <= this.minSpawnDelay ? this.minSpawnDelay : this.minSpawnDelay + random.nextInt(this.maxSpawnDelay - this.minSpawnDelay);
        this.spawnPotentials.getRandom(random).ifPresent(spawnPotential -> this.setSpawnEntry(world, pos, spawnPotential));
        this.sendStatus(world, pos, 1);
    }

    public void readNbt(@Nullable Level world, BlockPos pos, ValueInput input) {
        this.spawnDelay = input.getShortOr("Delay", (short) 0);
        // 26.x: ValueInput.read parses via codec directly (no NbtOps round-trip).
        Optional<SpawnData> spawnData = input.read("SpawnData", SpawnData.CODEC);
        Optional<WeightedList<SpawnData>> potentials = input.read("SpawnPotentials", SpawnData.LIST_CODEC);
        if (potentials.isEmpty()) {
            SpawnData entry = spawnData.orElseGet(SpawnData::new);
            this.spawnPotentials = WeightedList.of(entry);
            this.setSpawnEntry(world, pos, entry);
        } else {
            this.spawnPotentials = potentials.get();
            if (spawnData.isPresent()) {
                this.setSpawnEntry(world, pos, spawnData.get());
            } else {
                RandomSource random = world != null ? world.getRandom() : RandomSource.create();
                this.spawnPotentials.getRandom(random).ifPresent(spawnPotential -> this.setSpawnEntry(world, pos, spawnPotential));
            }
        }
        if (input.contains("MinSpawnDelay")) {
            this.minSpawnDelay = input.getShortOr("MinSpawnDelay", (short) 0);
            this.maxSpawnDelay = input.getShortOr("MaxSpawnDelay", (short) 0);
            this.spawnCount = input.getShortOr("SpawnCount", (short) 0);
        }
        if (input.contains("MaxNearbyEntities")) {
            this.maxNearbyEntities = input.getShortOr("MaxNearbyEntities", (short) 0);
            this.requiredPlayerRange = input.getShortOr("RequiredPlayerRange", (short) 0);
        }
        if (input.contains("SpawnRange")) {
            this.spawnRange = input.getShortOr("SpawnRange", (short) 0);
        }
        this.renderedEntity = null;
        this.maxSpawnCount = input.getIntOr("MaxSpawnCount", 0);
        this.totalSpawnCount = input.getIntOr("TotalSpawnCount", 0);
        this.difficulty = input.getStringOr("Difficulty", "");
        if (input.contains("Dungeon")) {
            this.dungeon = Dungeon.getDungeon(input.getStringOr("Dungeon", ""));
        }
        this.entityTypeId = input.getIntOr("EntityTypeId", 0);
    }

    public void writeNbt(ValueOutput output) {
        output.putShort("Delay", (short) this.spawnDelay);
        output.putShort("MinSpawnDelay", (short) this.minSpawnDelay);
        output.putShort("MaxSpawnDelay", (short) this.maxSpawnDelay);
        output.putShort("SpawnCount", (short) this.spawnCount);
        output.putShort("MaxNearbyEntities", (short) this.maxNearbyEntities);
        output.putShort("RequiredPlayerRange", (short) this.requiredPlayerRange);
        output.putShort("SpawnRange", (short) this.spawnRange);
        output.store("SpawnData", SpawnData.CODEC, this.spawnEntry);
        output.store("SpawnPotentials", SpawnData.LIST_CODEC, this.spawnPotentials);
        output.putInt("MaxSpawnCount", this.maxSpawnCount);
        output.putInt("TotalSpawnCount", this.totalSpawnCount);
        output.putString("Difficulty", this.difficulty);
        if (this.dungeon != null) {
            output.putString("Dungeon", this.dungeon.getDungeonTypeId());
        }
        output.putInt("EntityTypeId", this.entityTypeId);
    }

    @Nullable
    public Entity getRenderedEntity(Level world) {
        if (this.renderedEntity == null) {
            this.renderedEntity = EntityType.loadEntityRecursive(this.spawnEntry.getEntityToSpawn(), world, new EntitySpawnRequest(EntitySpawnReason.SPAWNER, true), BaseSpawner.SET_DISPLAY_ENTITY_ID);
            if (this.spawnEntry.getEntityToSpawn().size() != 1 || !this.spawnEntry.getEntityToSpawn().contains("id") || this.renderedEntity instanceof Mob) {
                // empty if block
            }
        }
        return this.renderedEntity;
    }

    public boolean handleStatus(Level world, int status) {
        if (status == 1) {
            if (world.isClientSide()) {
                this.spawnDelay = this.minSpawnDelay;
            }
            return true;
        }
        return false;
    }

    public void setSpawnEntry(@Nullable Level world, BlockPos pos, SpawnData spawnEntry) {
        this.spawnEntry = spawnEntry;
    }

    public void setDungeonInfo(Dungeon dungeon, String difficulty, int maxSpawnCount, EntityType<?> type) {
        this.dungeon = dungeon;
        this.difficulty = difficulty;
        this.maxSpawnCount = maxSpawnCount;
        this.setEntityId(type);
    }

    public void setEntityId(EntityType<?> type) {
        this.spawnEntry.getEntityToSpawn().putString("id", BuiltInRegistries.ENTITY_TYPE.getKey(type).toString());
        this.entityTypeId = BuiltInRegistries.ENTITY_TYPE.getId(type);
    }

    public int getEntityId() {
        return this.entityTypeId;
    }

    public abstract void sendStatus(Level var1, BlockPos var2, int var3);

    public double randomParticleValueOne() {
        return this.randomParticleValueOne;
    }

    public double randomParticleValueTwo() {
        return this.randomParticleValueTwo;
    }
}
