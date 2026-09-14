package net.dungeonz.block.logic;

import com.mojang.logging.LogUtils;
import java.util.Optional;
import java.util.function.Function;

import net.dungeonz.dungeon.Dungeon;
import net.dungeonz.dungeon.DungeonPlacementHandler;
import net.minecraft.class_1297;
import net.minecraft.class_1299;
import net.minecraft.class_1301;
import net.minecraft.class_1308;
import net.minecraft.class_1317;
import net.minecraft.class_1657;
import net.minecraft.class_1924;
import net.minecraft.class_1937;
import net.minecraft.class_1952;
import net.minecraft.class_2338;
import net.minecraft.class_238;
import net.minecraft.class_2398;
import net.minecraft.class_2487;
import net.minecraft.class_2499;
import net.minecraft.class_2509;
import net.minecraft.class_2520;
import net.minecraft.class_3218;
import net.minecraft.class_3730;
import net.minecraft.class_5712;
import net.minecraft.class_5819;
import net.minecraft.class_6005;
import net.minecraft.class_6088;
import net.minecraft.class_7923;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public abstract class DungeonSpawnerLogic {
    private static final Logger LOGGER = LogUtils.getLogger();
    private int spawnDelay = 20;
    private class_6005<class_1952> spawnPotentials = class_6005.<class_1952>method_38062();
    private class_1952 spawnEntry = new class_1952();
    private double randomParticleValueOne;
    private double randomParticleValueTwo;
    private int minSpawnDelay = 200;
    private int maxSpawnDelay = 800;
    private int spawnCount = 4;
    @Nullable
    private class_1297 renderedEntity;
    private int maxNearbyEntities = 6;
    private int requiredPlayerRange = 16;
    private int spawnRange = 4;
    private int maxSpawnCount = 0;
    private int totalSpawnCount = 0;
    private String difficulty = "";
    private Dungeon dungeon = null;
    private int entityTypeId = 0;

    private boolean isPlayerInRange(class_1937 world, class_2338 pos) {
        return isPlayerInRange(world, (double) pos.method_10263() + 0.5, (double) pos.method_10264() + 0.5, (double) pos.method_10260() + 0.5, this.requiredPlayerRange);
    }

    private boolean isPlayerInRange(class_1924 world, double x, double y, double z, double range) {
        for (class_1657 playerEntity : world.method_18456()) {
            if (!class_1301.field_6156.test(playerEntity) || !class_1301.field_6157.test(playerEntity))
                continue;
            double d = playerEntity.method_5649(x, y, z);
            if (!(range < 0.0) && !(d < range * range))
                continue;
            return true;
        }
        return false;
    }

    public void clientTick(class_1937 world, class_2338 pos) {
        if (!this.isPlayerInRange(world, pos)) {
            this.randomParticleValueTwo = this.randomParticleValueOne;
        } else {
            class_5819 random = world.method_8409();
            double d = (double) pos.method_10263() + random.method_43058();
            double e = (double) pos.method_10264() + random.method_43058();
            double f = (double) pos.method_10260() + random.method_43058();
            world.method_8406(class_2398.field_11251, d, e, f, 0.0, 0.0, 0.0);
            world.method_8406(class_2398.field_11240, d, e, f, 0.0, 0.0, 0.0);
            if (this.spawnDelay > 0) {
                --this.spawnDelay;
            }
            this.randomParticleValueTwo = this.randomParticleValueOne;
            this.randomParticleValueOne = (this.randomParticleValueOne + (double) (1000.0f / ((float) this.spawnDelay + 200.0f))) % 360.0;
        }
    }

    public void serverTick(class_3218 world, class_2338 pos) {
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
            class_2487 nbtCompound = this.spawnEntry.method_38093();
            Optional<class_1299<?>> optional = class_1299.method_17684(nbtCompound);
            if (optional.isEmpty()) {
                this.updateSpawns(world, pos);
                return;
            }
            class_2499 nbtList = nbtCompound.method_10554("Pos", class_2520.field_33256);
            int j = nbtList.size();
            class_5819 random = world.method_8409();
            double d = j >= 1 ? nbtList.method_10611(0) : (double) pos.method_10263() + (random.method_43058() - random.method_43058()) * (double) this.spawnRange + 0.5;
            double e = j >= 2 ? nbtList.method_10611(1) : (double) (pos.method_10264() + random.method_43048(3) - 1);
            f = j >= 3 ? nbtList.method_10611(2) : (double) pos.method_10260() + (random.method_43058() - random.method_43058()) * (double) this.spawnRange + 0.5;
            if (!world.method_18026(optional.get().method_58629(d, e, f))) {
                continue;
            }
            class_2338 blockPos = class_2338.method_49637(d, e, f);
            // if (!this.spawnEntry.getCustomSpawnRules().isPresent() ? !SpawnRestriction.canSpawn(optional.get(), world, SpawnReason.SPAWNER, blockPos, world.getRandom())
            // : !optional.get().getSpawnGroup().isPeaceful() && world.getDifficulty() == Difficulty.PEACEFUL
            // || !(customSpawnRules = this.spawnEntry.getCustomSpawnRules().get()).blockLightLimit().contains(world.getLightLevel(LightType.BLOCK, blockPos))
            // || !customSpawnRules.skyLightLimit().contains(world.getLightLevel(LightType.SKY, blockPos)))
            if (!class_1317.method_20638(optional.get(), world, class_3730.field_16469, blockPos, world.method_8409()))
                continue;
            class_1297 entity2 = class_1299.method_17842(nbtCompound, world, entity -> {
                entity.method_5808(d, e, f, entity.method_36454(), entity.method_36455());
                return entity;
            });
            if (entity2 == null) {
                this.updateSpawns(world, pos);
                return;
            }
            int k = world.method_18467(entity2.getClass(), new class_238(pos.method_10263(), pos.method_10264(), pos.method_10260(), pos.method_10263() + 1, pos.method_10264() + 1, pos.method_10260() + 1).method_1014(this.spawnRange)).size();
            if (k >= this.maxNearbyEntities) {
                this.updateSpawns(world, pos);
                return;
            }
            entity2.method_5808(entity2.method_23317(), entity2.method_23318(), entity2.method_23321(), random.method_43057() * 360.0f, 0.0f);
            if (entity2 instanceof class_1308) {
                class_1308 mobEntity = (class_1308) entity2;
                if (this.spawnEntry.method_38097().isEmpty() && !mobEntity.method_5979(world, class_3730.field_16469) || !mobEntity.method_5957(world))
                    continue;
                if (this.spawnEntry.method_38093().method_10546() == 1 && this.spawnEntry.method_38093().method_10573("id", class_2520.field_33258)) {
                    ((class_1308) entity2).method_5943(world, world.method_8404(entity2.method_24515()), class_3730.field_16469, null);
                }
                if (dungeon != null) {
                    DungeonPlacementHandler.strengthenMob(mobEntity, dungeon, difficulty, false);
                }
            }
            if (!world.method_30736(entity2)) {
                this.totalSpawnCount++;
                this.updateSpawns(world, pos);
                return;
            }
            world.method_20290(class_6088.field_31147, pos, 0);
            world.method_33596(entity2, class_5712.field_28738, blockPos);
            if (entity2 instanceof class_1308) {
                ((class_1308) entity2).method_5990();
            }
            bl = true;
        }
        if (bl) {
            this.updateSpawns(world, pos);
        }
        if (this.maxSpawnCount != 0 && this.maxSpawnCount <= this.totalSpawnCount) {
            world.method_22352(pos, false);
        }
    }

    private void updateSpawns(class_1937 world, class_2338 pos) {
        class_5819 random = world.field_9229;
        this.spawnDelay = this.maxSpawnDelay <= this.minSpawnDelay ? this.minSpawnDelay : this.minSpawnDelay + random.method_43048(this.maxSpawnDelay - this.minSpawnDelay);
        this.spawnPotentials.method_34992(random).ifPresent(spawnPotential -> this.setSpawnEntry(world, pos, (class_1952) spawnPotential.comp_2542()));
        this.sendStatus(world, pos, 1);
    }

    public void readNbt(@Nullable class_1937 world, class_2338 pos, class_2487 nbt) {
        this.spawnDelay = nbt.method_10568("Delay");
        boolean bl = nbt.method_10573("SpawnPotentials", class_2520.field_33259);
        boolean bl2 = nbt.method_10573("SpawnData", class_2520.field_33260);
        if (!bl) {
            class_1952 mobSpawnerEntry = bl2
                    ? class_1952.field_34460.parse(class_2509.field_11560, nbt.method_10562("SpawnData")).resultOrPartial(error -> LOGGER.warn("Invalid SpawnData: {}", error)).orElseGet(class_1952::new)
                    : new class_1952();
            this.spawnPotentials = class_6005.method_38061(mobSpawnerEntry);
            this.setSpawnEntry(world, pos, mobSpawnerEntry);
        } else {
            class_2499 nbtList = nbt.method_10554("SpawnPotentials", class_2520.field_33260);
            this.spawnPotentials = class_1952.field_34461.parse(class_2509.field_11560, nbtList).resultOrPartial(error -> LOGGER.warn("Invalid SpawnPotentials list: {}", error))
                    .orElseGet(class_6005::<class_1952>method_38062);
            if (bl2) {
                class_1952 mobSpawnerEntry2 = class_1952.field_34460.parse(class_2509.field_11560, nbt.method_10562("SpawnData")).resultOrPartial(error -> LOGGER.warn("Invalid SpawnData: {}", error))
                        .orElseGet(class_1952::new);
                this.setSpawnEntry(world, pos, mobSpawnerEntry2);
            } else {
                this.spawnPotentials.method_34992(world.method_8409()).ifPresent(spawnPotential -> this.setSpawnEntry(world, pos, (class_1952) spawnPotential.comp_2542()));
            }
        }
        if (nbt.method_10573("MinSpawnDelay", class_2520.field_33263)) {
            this.minSpawnDelay = nbt.method_10568("MinSpawnDelay");
            this.maxSpawnDelay = nbt.method_10568("MaxSpawnDelay");
            this.spawnCount = nbt.method_10568("SpawnCount");
        }
        if (nbt.method_10573("MaxNearbyEntities", class_2520.field_33263)) {
            this.maxNearbyEntities = nbt.method_10568("MaxNearbyEntities");
            this.requiredPlayerRange = nbt.method_10568("RequiredPlayerRange");
        }
        if (nbt.method_10573("SpawnRange", class_2520.field_33263)) {
            this.spawnRange = nbt.method_10568("SpawnRange");
        }
        this.renderedEntity = null;
        this.maxSpawnCount = nbt.method_10550("MaxSpawnCount");
        this.totalSpawnCount = nbt.method_10550("TotalSpawnCount");
        this.difficulty = nbt.method_10558("Difficulty");
        if (nbt.method_10545("Dungeon")) {
            this.dungeon = Dungeon.getDungeon(nbt.method_10558("Dungeon"));
        }
        this.entityTypeId = nbt.method_10550("EntityTypeId");
    }

    public class_2487 writeNbt(class_2487 nbt) {
        nbt.method_10575("Delay", (short) this.spawnDelay);
        nbt.method_10575("MinSpawnDelay", (short) this.minSpawnDelay);
        nbt.method_10575("MaxSpawnDelay", (short) this.maxSpawnDelay);
        nbt.method_10575("SpawnCount", (short) this.spawnCount);
        nbt.method_10575("MaxNearbyEntities", (short) this.maxNearbyEntities);
        nbt.method_10575("RequiredPlayerRange", (short) this.requiredPlayerRange);
        nbt.method_10575("SpawnRange", (short) this.spawnRange);
        nbt.method_10566("SpawnData", class_1952.field_34460.encodeStart(class_2509.field_11560, this.spawnEntry).result().orElseThrow(() -> new IllegalStateException("Invalid SpawnData")));
        nbt.method_10566("SpawnPotentials", class_1952.field_34461.encodeStart(class_2509.field_11560, this.spawnPotentials).result().orElseThrow());
        nbt.method_10569("MaxSpawnCount", this.maxSpawnCount);
        nbt.method_10569("TotalSpawnCount", this.totalSpawnCount);
        nbt.method_10582("Difficulty", this.difficulty);
        if (this.dungeon != null) {
            nbt.method_10582("Dungeon", this.dungeon.getDungeonTypeId());
        }
        nbt.method_10569("EntityTypeId", this.entityTypeId);
        return nbt;
    }

    @Nullable
    public class_1297 getRenderedEntity(class_1937 world) {
        if (this.renderedEntity == null) {
            this.renderedEntity = class_1299.method_17842(this.spawnEntry.method_38093(), world, Function.identity());
            if (this.spawnEntry.method_38093().method_10546() != 1 || !this.spawnEntry.method_38093().method_10573("id", class_2520.field_33258) || this.renderedEntity instanceof class_1308) {
                // empty if block
            }
        }
        return this.renderedEntity;
    }

    public boolean handleStatus(class_1937 world, int status) {
        if (status == 1) {
            if (world.field_9236) {
                this.spawnDelay = this.minSpawnDelay;
            }
            return true;
        }
        return false;
    }

    public void setSpawnEntry(@Nullable class_1937 world, class_2338 pos, class_1952 spawnEntry) {
        this.spawnEntry = spawnEntry;
    }

    public void setDungeonInfo(Dungeon dungeon, String difficulty, int maxSpawnCount, class_1299<?> type) {
        this.dungeon = dungeon;
        this.difficulty = difficulty;
        this.maxSpawnCount = maxSpawnCount;
        this.setEntityId(type);
    }

    public void setEntityId(class_1299<?> type) {
        this.spawnEntry.method_38093().method_10582("id", class_7923.field_41177.method_10221(type).toString());
        this.entityTypeId = class_7923.field_41177.method_10206(type);
    }

    public int getEntityId() {
        return this.entityTypeId;
    }

    public abstract void sendStatus(class_1937 var1, class_2338 var2, int var3);

    public double randomParticleValueOne() {
        return this.randomParticleValueOne;
    }

    public double randomParticleValueTwo() {
        return this.randomParticleValueTwo;
    }
}
