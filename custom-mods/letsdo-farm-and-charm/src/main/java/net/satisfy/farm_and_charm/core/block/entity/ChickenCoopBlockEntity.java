package net.satisfy.farm_and_charm.core.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.animal.chicken.Chicken;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.satisfy.farm_and_charm.core.entity.ChickenCoopAccess;
import net.satisfy.farm_and_charm.core.registry.EntityTypeRegistry;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public class ChickenCoopBlockEntity extends BlockEntity {
    private static final int MAX_CHICKENS = 3;
    private static final int MAX_EGGS = 9;
    private static final String KEY_CHICKENS = "Chickens";
    private static final String KEY_EGGS = "EggCount";
    private static final String KEY_COOP_TIME = "CoopTime";
    private static final String KEY_UUID = "UUID";
    private static final String KEY_UUID_MOST = "UUIDMost";
    private static final String KEY_UUID_LEAST = "UUIDLeast";
    private static final String KEY_LEASH = "Leash";
    private static final String KEY_CAPTURED_UUID = "CapturedUUID";
    private final List<CompoundTag> storedChickens = new ArrayList<>();
    private int eggCount = 0;

    public ChickenCoopBlockEntity(BlockPos pos, BlockState state) {
        super(EntityTypeRegistry.CHICKEN_COOP_BLOCK_ENTITY.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, ChickenCoopBlockEntity coop) {
        if (level.isClientSide()) return;
        Iterator<CompoundTag> iterator = coop.storedChickens.iterator();
        while (iterator.hasNext()) {
            CompoundTag chickenTag = iterator.next();
            int ticks = chickenTag.getIntOr(KEY_COOP_TIME, 0) - 1;
            if (ticks <= 0) {
                chickenTag.remove(KEY_COOP_TIME);
                Entity chicken = EntityType.create(EntityTypes.CHICKEN, net.minecraft.world.level.storage.TagValueInput.create(net.minecraft.util.ProblemReporter.DISCARDING, level.registryAccess(), chickenTag), level, net.minecraft.world.entity.EntitySpawnReason.TRIGGERED).orElse(null);
                if (chicken instanceof Chicken spawned) {
                    int coopCooldown = 20 * 60 * (4 + level.getRandom().nextInt(2));
                    CompoundTag toLoad = chickenTag.copy();
                    toLoad.remove(KEY_UUID);
                    toLoad.remove(KEY_UUID_MOST);
                    toLoad.remove(KEY_UUID_LEAST);
                    toLoad.remove(KEY_CAPTURED_UUID);
                    spawned.load(net.minecraft.world.level.storage.TagValueInput.create(net.minecraft.util.ProblemReporter.DISCARDING, level.registryAccess(), toLoad));
                    spawned.setHealth(spawned.getMaxHealth());
                    spawned.setInvisible(false);
                    spawned.setNoAi(false);
                    spawned.setSilent(false);
                    if (spawned instanceof ChickenCoopAccess coopChicken) {
                        coopChicken.farmAndCharm$setCoopCooldown(coopCooldown);
                    }
                    BlockPos spawnPos = findSafeSpawnPosition(level, pos);
                    spawned.setPos(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5);
                    level.addFreshEntity(spawned);
                    iterator.remove();
                    coop.setChanged();
                    level.sendBlockUpdated(pos, coop.getBlockState(), coop.getBlockState(), 3);
                }
            } else {
                chickenTag.putInt(KEY_COOP_TIME, ticks);
            }
        }
    }

    private static BlockPos findSafeSpawnPosition(Level level, BlockPos center) {
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            BlockPos offset = center.relative(dir);
            if (level.getBlockState(offset).isAir() && level.getBlockState(offset.above()).isAir()) {
                return offset;
            }
        }
        return center.above();
    }

    public boolean hasSpaceForChicken() {
        return storedChickens.size() < MAX_CHICKENS;
    }

    public void addChicken(Chicken chicken) {
        if (this.level == null || this.level.isClientSide()) return;
        if (!hasSpaceForChicken()) return;

        chicken.removeAllEffects();

        net.minecraft.world.level.storage.TagValueOutput _out = net.minecraft.world.level.storage.TagValueOutput.createWithContext(net.minecraft.util.ProblemReporter.DISCARDING, chicken.registryAccess());
        chicken.save(_out);
        CompoundTag tag = _out.buildResult();

        UUID u = chicken.getUUID();
        tag.putString(KEY_CAPTURED_UUID, u.toString());
        tag.remove(KEY_LEASH);
        tag.remove(KEY_UUID);
        tag.remove(KEY_UUID_MOST);
        tag.remove(KEY_UUID_LEAST);
        tag.putInt(KEY_COOP_TIME, 200 + chicken.level().getRandom().nextInt(200));
        storedChickens.add(tag);

        boolean hadLeash = chicken.getLeashHolder() != null;
        if (hadLeash) {
            chicken.dropLeash();
            if (chicken.level() instanceof net.minecraft.server.level.ServerLevel _sl) chicken.spawnAtLocation(_sl, Items.LEAD);
        }

        chicken.stopRiding();
        chicken.ejectPassengers();
        chicken.setNoAi(true);
        chicken.setSilent(true);
        chicken.setInvisible(true);
        chicken.discard();

        if (!hadLeash) {
            addEgg();
        }

        this.setChanged();
        this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
    }

    public void releaseAllChickens() {
        if (this.level == null || this.level.isClientSide()) return;
        for (CompoundTag tag : new ArrayList<>(storedChickens)) {
            tag.remove(KEY_COOP_TIME);
            Entity chicken = EntityType.create(EntityTypes.CHICKEN, net.minecraft.world.level.storage.TagValueInput.create(net.minecraft.util.ProblemReporter.DISCARDING, level.registryAccess(), tag), level, net.minecraft.world.entity.EntitySpawnReason.TRIGGERED).orElse(null);
            if (chicken instanceof Chicken spawned) {
                int coopCooldown = 20 * 60 * (4 + level.getRandom().nextInt(2));
                CompoundTag toLoad = tag.copy();
                toLoad.remove(KEY_UUID);
                toLoad.remove(KEY_UUID_MOST);
                toLoad.remove(KEY_UUID_LEAST);
                toLoad.remove(KEY_CAPTURED_UUID);
                spawned.load(net.minecraft.world.level.storage.TagValueInput.create(net.minecraft.util.ProblemReporter.DISCARDING, level.registryAccess(), toLoad));
                spawned.setHealth(spawned.getMaxHealth());
                spawned.setInvisible(false);
                spawned.setNoAi(false);
                spawned.setSilent(false);
                if (spawned instanceof ChickenCoopAccess coopChicken) {
                    coopChicken.farmAndCharm$setCoopCooldown(coopCooldown);
                }
                BlockPos spawnPos = findSafeSpawnPosition(level, worldPosition);
                spawned.setPos(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5);
                level.addFreshEntity(spawned);
            }
        }
        storedChickens.clear();
        setChanged();
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    public boolean containsChicken(Chicken chicken) {
        UUID u = chicken.getUUID();
        for (CompoundTag tag : storedChickens) {
            if (tag.contains(KEY_CAPTURED_UUID) && tag.getString(KEY_CAPTURED_UUID).map(s -> s.equals(u.toString())).orElse(false)) return true;
        }
        return false;
    }

    public void addEgg() {
        if (eggCount < MAX_EGGS) {
            eggCount++;
            setChanged();
        }
    }

    public int getEggCount() {
        return eggCount;
    }

    public void clearEggs() {
        eggCount = 0;
        setChanged();
    }

    public List<CompoundTag> getStoredChickens() {
        return this.storedChickens;
    }

    public void saveTo(net.minecraft.world.level.storage.ValueOutput output) {
        saveAdditional(output);
    }

    @Override
    protected void saveAdditional(net.minecraft.world.level.storage.ValueOutput output) {
        super.saveAdditional(output);
        ListTag chickenList = new ListTag();
        for (CompoundTag chickenTag : storedChickens) {
            chickenList.add(chickenTag.copy());
        }
        output.store(KEY_CHICKENS, net.minecraft.nbt.CompoundTag.CODEC.listOf(), chickenList.stream().map(t -> (CompoundTag) t).toList());
        output.putInt(KEY_EGGS, eggCount);
    }

    @Override
    protected void loadAdditional(net.minecraft.world.level.storage.ValueInput input) {
        super.loadAdditional(input);
        storedChickens.clear();
        for (CompoundTag chickenTag : input.read(KEY_CHICKENS, net.minecraft.nbt.CompoundTag.CODEC.listOf()).orElse(java.util.List.of())) {
            storedChickens.add(chickenTag);
        }
        eggCount = input.getIntOr(KEY_EGGS, 0);
    }
}
