package net.dungeonz.block.screen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import net.dungeonz.block.entity.DungeonPortalEntity;
import net.dungeonz.init.BlockInit;
import net.dungeonz.network.packet.DungeonPortalPacket;
import net.dungeonz.util.DungeonHelper;
import net.minecraft.class_1657;
import net.minecraft.class_1661;
import net.minecraft.class_1703;
import net.minecraft.class_1799;
import net.minecraft.class_1937;
import net.minecraft.class_2338;
import net.minecraft.class_2960;
import net.minecraft.class_3914;

public class DungeonPortalScreenHandler extends class_1703 {

    private final class_1937 world;
    private final class_3914 context;
    private final DungeonPortalEntity dungeonPortalEntity;
    private class_2338 pos;

    private List<String> difficulties = new ArrayList<String>();
    private Map<String, List<class_1799>> possibleLootDifficultyItemStackMap = new HashMap<String, List<class_1799>>();
    private Map<String, List<class_1799>> requiredItemStacks = new HashMap<String, List<class_1799>>();
    private int waitingGroupSize = 0;

    private int requiredLevel = 0;
    private boolean allowRespawn = false;
    private boolean keepInventory = false;
    private boolean allowPositiveEffects = false;
    private boolean allowEnderPearl = false;
    private boolean allowElytra = false;

    @Nullable
    private class_2960 backgroundId = null;

    public DungeonPortalScreenHandler(int syncId, class_1661 playerInventory, DungeonPortalPacket packet) {
        this(syncId, playerInventory, new DungeonPortalEntity(packet.blockPos(), playerInventory.field_7546.method_37908().method_8320(packet.blockPos())), class_3914.field_17304);
        this.getDungeonPortalEntity().setDungeonType(packet.dungeonType());
        this.pos = packet.blockPos();

        this.getDungeonPortalEntity().setDungeonPlayerUuids(packet.playerUuids());
        this.getDungeonPortalEntity().setDeadDungeonPlayerUuids(packet.deadPlayerUuids());
        this.setDifficulties(packet.difficulties());

        this.setPossibleLootItemStacks(packet.possibleLoot());
        this.setRequiredItemStacks(packet.requiredItemStacks());

        this.getDungeonPortalEntity().setMaxGroupSize(packet.maxGroupSize());
        this.getDungeonPortalEntity().setMinGroupSize(packet.minGroupSize());
        this.setWaitingGroupSize(packet.waitingPlayerCount());
        this.requiredLevel = packet.requiredLevel();
        this.getDungeonPortalEntity().setCooldownTime(packet.cooldownTime());
        this.getDungeonPortalEntity().setDifficulty(packet.difficulty());
        
        this.allowEnderPearl = packet.allowEnderPearl();
        this.allowPositiveEffects = packet.allowPositiveEffects();
        this.allowElytra = packet.allowElytra();
        this.allowRespawn = packet.allowRespawn();
        this.keepInventory = packet.keepInventory();
        this.getDungeonPortalEntity().setPrivateGroup(packet.privateGroup());
        this.backgroundId = packet.backgroundId().orElse(null);
    }

    public DungeonPortalScreenHandler(int syncId, class_1661 playerInventory, DungeonPortalEntity dungeonPortalEntity, class_3914 context) {
        super(BlockInit.PORTAL, syncId);
        this.context = context;
        this.world = playerInventory.field_7546.method_37908();
        this.dungeonPortalEntity = dungeonPortalEntity;
        this.pos = dungeonPortalEntity.method_11016();

        if (!this.world.method_8608()) {
            setDifficulties(this.dungeonPortalEntity.getDungeon().getDifficultyList());
            setRequiredItemStacks(DungeonHelper.getRequiredItemStackList(this.dungeonPortalEntity.getDungeon()));
            setPossibleLootItemStacks(DungeonHelper.getPossibleLootItemStackMap(this.dungeonPortalEntity.getDungeon(), this.world.method_8503()));
        }
    }

    @Override
    public class_1799 method_7601(class_1657 var1, int var2) {
        return null;
    }

    @Override
    public boolean method_7597(class_1657 player) {
        return this.context.method_17396((world, pos) -> {
            if (!this.world.method_8320(pos).method_27852(BlockInit.DUNGEON_PORTAL)) {
                return false;
            }
            return player.method_5649((double) pos.method_10263() + 0.5, (double) pos.method_10264() + 0.5, (double) pos.method_10260() + 0.5) <= 64.0;
        }, true);
    }

    @Nullable
    public class_2960 getBackgroundId() {
        return this.backgroundId;
    }

    public DungeonPortalEntity getDungeonPortalEntity() {
        return this.dungeonPortalEntity;
    }

    public List<String> getDifficulties() {
        return this.difficulties;
    }

    public void setDifficulties(List<String> difficulties) {
        this.difficulties = difficulties;
    }

    public Map<String, List<class_1799>> getPossibleLootDifficultyItemStackMap() {
        return this.possibleLootDifficultyItemStackMap;
    }

    public void setPossibleLootItemStacks(Map<String, List<class_1799>> possibleLootDifficultyItemStackMap) {
        this.possibleLootDifficultyItemStackMap = possibleLootDifficultyItemStackMap;
    }

    public Map<String, List<class_1799>> getRequiredItemStacks() {
        return this.requiredItemStacks;
    }

    public void setRequiredItemStacks(Map<String, List<class_1799>> requiredItemStacks) {
        this.requiredItemStacks = requiredItemStacks;
    }

    public int getWaitingGroupSize() {
        return this.waitingGroupSize;
    }

    public void setWaitingGroupSize(int waitingGroupSize) {
        this.waitingGroupSize = waitingGroupSize;
    }

    public class_2338 getPos() {
        return this.pos;
    }

    public int getRequiredLevel() {
        return this.requiredLevel;
    }

    public boolean isAllowRespawn() {
        return allowRespawn;
    }

    public boolean isKeepInventory() {
        return keepInventory;
    }

    public boolean isAllowPositiveEffects() {
        return allowPositiveEffects;
    }

    public boolean isAllowEnderPearl() {
        return allowEnderPearl;
    }

    public boolean isAllowElytra() {
        return allowElytra;
    }
}
