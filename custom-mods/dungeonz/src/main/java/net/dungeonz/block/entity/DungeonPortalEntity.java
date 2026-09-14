package net.dungeonz.block.entity;

import net.dungeonz.block.screen.DungeonPortalScreenHandler;
import net.dungeonz.dungeon.Dungeon;
import net.dungeonz.dungeon.DungeonPlacementHandler;
import net.dungeonz.init.*;
import net.dungeonz.network.DungeonServerPacket;
import net.dungeonz.network.packet.DungeonPortalPacket;
import net.dungeonz.util.DungeonHelper;
import net.dungeonz.util.InventoryHelper;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.class_1657;
import net.minecraft.class_1661;
import net.minecraft.class_1703;
import net.minecraft.class_1799;
import net.minecraft.class_1937;
import net.minecraft.class_2246;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_238;
import net.minecraft.class_2487;
import net.minecraft.class_2561;
import net.minecraft.class_2640;
import net.minecraft.class_2680;
import net.minecraft.class_2960;
import net.minecraft.class_3218;
import net.minecraft.class_3222;
import net.minecraft.class_3419;
import net.minecraft.class_3914;
import net.minecraft.class_4051;
import net.minecraft.class_7225;
import net.minecraft.class_7225.class_7874;
import net.minecraft.class_7923;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.Map.Entry;

public class DungeonPortalEntity extends class_2640 implements ExtendedScreenHandlerFactory<DungeonPortalPacket> {

    private class_2561 title = class_2561.method_43471("container.dungeon_portal");
    private String dungeonType = "";
    private String difficulty = "";
    private boolean dungeonStructureGenerated = false;
    private List<UUID> dungeonPlayerUuids = new ArrayList<UUID>();
    private List<UUID> deadDungeonPlayerUuids = new ArrayList<UUID>();
    private int maxGroupSize = 0;
    private int minGroupSize = 0;
    private List<UUID> waitingUuids = new ArrayList<UUID>();
    private int cooldownTime = 0;
    private int autoKickTime = 0;
    private boolean privateGroup = false;
    private HashMap<Integer, ArrayList<class_2338>> blockBlockPosMap = new HashMap<Integer, ArrayList<class_2338>>();
    private List<class_2338> chestPosList = new ArrayList<class_2338>();
    private List<class_2338> exitPosList = new ArrayList<class_2338>();
    private List<class_2338> gatePosList = new ArrayList<class_2338>();
    private Map<class_2338, Integer> movingBlockMap = new HashMap<>();
    private Map<class_2338, Powered> poweredBlockMap = new HashMap<>();
    private class_2338 bossBlockPos = new class_2338(0, 0, 0);
    private class_2338 bossLootBlockPos = new class_2338(0, 0, 0);
    private HashMap<class_2338, Integer> spawnerPosEntityIdMap = new HashMap<class_2338, Integer>();
    private HashMap<class_2338, Integer> replacePosBlockIdMap = new HashMap<class_2338, Integer>();
    private List<Integer> dungeonEdgeList = new ArrayList<Integer>();
    private int dungeonTeleportCountdown = 0;

    public DungeonPortalEntity(class_2338 pos, class_2680 state) {
        super(BlockInit.DUNGEON_PORTAL_ENTITY, pos, state);
    }

    @Override
    public void method_11014(class_2487 nbt, class_7225.class_7874 registryLookup) {
        super.method_11014(nbt, registryLookup);
        this.dungeonType = nbt.method_10558("DungeonType");
        this.difficulty = nbt.method_10558("Difficulty");
        this.dungeonStructureGenerated = nbt.method_10577("DungeonStructureGenerated");
        this.dungeonPlayerUuids.clear();
        for (int i = 0; i < nbt.method_10550("DungeonPlayerCount"); i++) {
            this.dungeonPlayerUuids.add(nbt.method_25926("PlayerUUID" + i));
        }
        this.deadDungeonPlayerUuids.clear();
        for (int i = 0; i < nbt.method_10550("DeadDungeonPlayerCount"); i++) {
            this.deadDungeonPlayerUuids.add(nbt.method_25926("DeadPlayerUUID" + i));
        }
        this.maxGroupSize = nbt.method_10550("MaxGroupSize");
        this.minGroupSize = nbt.method_10550("MinGroupSize");
        this.cooldownTime = nbt.method_10550("CooldownTime");
        this.autoKickTime = nbt.method_10550("AutoKickTime");
        this.privateGroup = nbt.method_10577("PrivateGroup");
        this.blockBlockPosMap.clear();
        if (nbt.method_10550("BlockMapSize") > 0) {
            for (int i = 0; i < nbt.method_10550("BlockMapSize"); i++) {
                ArrayList<class_2338> posList = new ArrayList<>();
                for (int u = 0; u < nbt.method_10550("BlockListSize" + i); u++) {
                    int[] blockPos = nbt.method_10561("BlockPos" + i + "" + u);
                    posList.add(new class_2338(blockPos[0], blockPos[1], blockPos[2]));
                }
                this.blockBlockPosMap.put(nbt.method_10550("BlockId" + i), posList);
            }
        }

        int[] bossPos = nbt.method_10561("BossPos");
        if (bossPos.length > 0) {
            this.bossBlockPos = new class_2338(bossPos[0], bossPos[1], bossPos[2]);
        }
        int[] bossLootPos = nbt.method_10561("BossLootPos");
        if (bossLootPos.length > 0) {
            this.bossLootBlockPos = new class_2338(bossLootPos[0], bossLootPos[1], bossLootPos[2]);
        }

        if (nbt.method_10550("ChestListSize") > 0) {
            this.chestPosList.clear();
            for (int i = 0; i < nbt.method_10550("ChestListSize"); i++) {
                int[] chestPos = nbt.method_10561("ChestPos" + i);
                this.chestPosList.add(new class_2338(chestPos[0], chestPos[1], chestPos[2]));
            }
        }

        if (nbt.method_10550("ExitListSize") > 0) {
            this.exitPosList.clear();
            for (int i = 0; i < nbt.method_10550("ExitListSize"); i++) {
                int[] exitPos = nbt.method_10561("ExitPos" + i);
                this.exitPosList.add(new class_2338(exitPos[0], exitPos[1], exitPos[2]));
            }
        }

        if (nbt.method_10550("SpawnerMapSize") > 0) {
            this.spawnerPosEntityIdMap.clear();
            for (int i = 0; i < nbt.method_10550("SpawnerListSize"); i++) {
                int[] spawnerPos = nbt.method_10561("SpawnerPos" + i);
                this.spawnerPosEntityIdMap.put(new class_2338(spawnerPos[0], spawnerPos[1], spawnerPos[2]), spawnerPos[3]);
            }
        }

        if (nbt.method_10550("ReplacePosSize") > 0) {
            this.replacePosBlockIdMap.clear();
            for (int i = 0; i < nbt.method_10550("ReplacePosSize"); i++) {
                int[] replacePos = nbt.method_10561("ReplacePos" + i);
                this.replacePosBlockIdMap.put(new class_2338(replacePos[0], replacePos[1], replacePos[2]), replacePos[3]);
            }
        }

        if (nbt.method_10550("MovingPosSize") > 0) {
            this.movingBlockMap.clear();
            for (int i = 0; i < nbt.method_10550("MovingPosSize"); i++) {
                int[] movingPos = nbt.method_10561("MovingPos" + i);
                this.movingBlockMap.put(new class_2338(movingPos[0], movingPos[1], movingPos[2]), movingPos[3]);
            }
        }

        if (nbt.method_10550("PoweredPosSize") > 0) {
            this.poweredBlockMap.clear();
            for (int i = 0; i < nbt.method_10550("PoweredPosSize"); i++) {
                int[] poweredPos = nbt.method_10561("PoweredPos" + i);
                boolean isPowered = poweredPos[4] == 1;
                this.poweredBlockMap.put(new class_2338(poweredPos[0], poweredPos[1], poweredPos[2]), new Powered(poweredPos[3], isPowered, poweredPos[5], poweredPos[6]));
            }
        }

        if (nbt.method_10550("DungeonEdgeSize") > 0) {
            this.dungeonEdgeList.clear();
            for (int i = 0; i < nbt.method_10550("DungeonEdgeSize") / 3; i++) {
                int[] dungeonEdgePos = nbt.method_10561("DungeonEdge" + i);
                this.dungeonEdgeList.add(dungeonEdgePos[0]);
                this.dungeonEdgeList.add(dungeonEdgePos[1]);
                this.dungeonEdgeList.add(dungeonEdgePos[2]);
            }
        }

        if (nbt.method_10550("GateListSize") > 0) {
            this.gatePosList.clear();
            for (int i = 0; i < nbt.method_10550("GateListSize"); i++) {
                int[] gatePos = nbt.method_10561("GatePos" + i);
                this.gatePosList.add(new class_2338(gatePos[0], gatePos[1], gatePos[2]));
            }
        }
    }

    @Override
    public void method_11007(class_2487 nbt, class_7225.class_7874 registryLookup) {
        super.method_11007(nbt, registryLookup);
        nbt.method_10582("DungeonType", this.dungeonType);
        nbt.method_10582("Difficulty", this.difficulty);
        nbt.method_10556("DungeonStructureGenerated", this.dungeonStructureGenerated);
        nbt.method_10569("DungeonPlayerCount", this.dungeonPlayerUuids.size());
        for (int i = 0; i < this.dungeonPlayerUuids.size(); i++) {
            nbt.method_25927("PlayerUUID" + i, this.dungeonPlayerUuids.get(i));
        }
        nbt.method_10569("DeadDungeonPlayerCount", this.deadDungeonPlayerUuids.size());
        for (int i = 0; i < this.deadDungeonPlayerUuids.size(); i++) {
            nbt.method_25927("DeadPlayerUUID" + i, this.deadDungeonPlayerUuids.get(i));
        }
        nbt.method_10569("MaxGroupSize", this.maxGroupSize);
        nbt.method_10569("MinGroupSize", this.minGroupSize);
        nbt.method_10569("CooldownTime", this.cooldownTime);
        nbt.method_10569("AutoKickTime", this.autoKickTime);
        nbt.method_10556("PrivateGroup", this.privateGroup);

        nbt.method_10569("BlockMapSize", this.blockBlockPosMap.size());
        if (!this.blockBlockPosMap.isEmpty()) {
            int blockCount = 0;
            for (Entry<Integer, ArrayList<class_2338>> entry : this.blockBlockPosMap.entrySet()) {
                nbt.method_10569("BlockId" + blockCount, entry.getKey());
                nbt.method_10569("BlockListSize" + blockCount, entry.getValue().size());
                for (int i = 0; i < entry.getValue().size(); i++) {
                    nbt.method_10572("BlockPos" + blockCount + "" + i, List.of(entry.getValue().get(i).method_10263(), entry.getValue().get(i).method_10264(), entry.getValue().get(i).method_10260()));
                }
                blockCount++;
            }
        }
        nbt.method_10572("BossPos", List.of(this.bossBlockPos.method_10263(), this.bossBlockPos.method_10264(), this.bossBlockPos.method_10260()));
        nbt.method_10572("BossLootPos", List.of(this.bossLootBlockPos.method_10263(), this.bossLootBlockPos.method_10264(), this.bossLootBlockPos.method_10260()));

        nbt.method_10569("ChestListSize", this.chestPosList.size());
        if (!this.chestPosList.isEmpty()) {
            for (int i = 0; i < this.chestPosList.size(); i++) {
                nbt.method_10572("ChestPos" + i, List.of(this.chestPosList.get(i).method_10263(), this.chestPosList.get(i).method_10264(), this.chestPosList.get(i).method_10260()));
            }
        }

        nbt.method_10569("ExitListSize", this.exitPosList.size());
        if (!this.exitPosList.isEmpty()) {
            for (int i = 0; i < this.exitPosList.size(); i++) {
                nbt.method_10572("ExitPos" + i, List.of(this.exitPosList.get(i).method_10263(), this.exitPosList.get(i).method_10264(), this.exitPosList.get(i).method_10260()));
            }
        }

        nbt.method_10569("SpawnerMapSize", this.spawnerPosEntityIdMap.size());
        if (!this.spawnerPosEntityIdMap.isEmpty()) {
            Iterator<Entry<class_2338, Integer>> iterator = this.spawnerPosEntityIdMap.entrySet().iterator();
            int count = 0;
            while (iterator.hasNext()) {
                Entry<class_2338, Integer> entry = iterator.next();
                nbt.method_10572("SpawnerPos" + count, List.of(entry.getKey().method_10263(), entry.getKey().method_10264(), entry.getKey().method_10260(), entry.getValue()));
                count++;
            }
        }

        nbt.method_10569("ReplacePosSize", this.replacePosBlockIdMap.size());
        if (!this.replacePosBlockIdMap.isEmpty()) {
            Iterator<Entry<class_2338, Integer>> iterator = this.replacePosBlockIdMap.entrySet().iterator();
            int count = 0;
            while (iterator.hasNext()) {
                Entry<class_2338, Integer> entry = iterator.next();
                nbt.method_10572("ReplacePos" + count, List.of(entry.getKey().method_10263(), entry.getKey().method_10264(), entry.getKey().method_10260(), entry.getValue()));
                count++;
            }
        }

        nbt.method_10569("MovingPosSize", this.movingBlockMap.size());
        if (!this.movingBlockMap.isEmpty()) {
            Iterator<Entry<class_2338, Integer>> iterator = this.movingBlockMap.entrySet().iterator();
            int count = 0;
            while (iterator.hasNext()) {
                Entry<class_2338, Integer> entry = iterator.next();
                nbt.method_10572("MovingPos" + count, List.of(entry.getKey().method_10263(), entry.getKey().method_10264(), entry.getKey().method_10260(), entry.getValue()));
                count++;
            }
        }

        nbt.method_10569("PoweredPosSize", this.poweredBlockMap.size());
        if (!this.poweredBlockMap.isEmpty()) {
            Iterator<Entry<class_2338, Powered>> iterator = this.poweredBlockMap.entrySet().iterator();
            int count = 0;
            while (iterator.hasNext()) {
                Entry<class_2338, Powered> entry = iterator.next();
                int isPowered = entry.getValue().getPowered() ? 1 : 0;
                nbt.method_10572("PoweredPos" + count, List.of(entry.getKey().method_10263(), entry.getKey().method_10264(), entry.getKey().method_10260(), entry.getValue().getBlockId(), isPowered, entry.getValue().getFacing(), entry.getValue().getBlockFacing()));
                count++;
            }
        }

        nbt.method_10569("DungeonEdgeSize", this.dungeonEdgeList.size());
        if (!this.dungeonEdgeList.isEmpty()) {
            for (int i = 0; i < this.dungeonEdgeList.size() / 3; i++) {
                nbt.method_10572("DungeonEdge" + i, List.of(this.dungeonEdgeList.get(3 * i), this.dungeonEdgeList.get(1 + 3 * i), this.dungeonEdgeList.get(2 + 3 * i)));
            }
        }

        nbt.method_10569("GateListSize", this.gatePosList.size());
        if (!this.gatePosList.isEmpty()) {
            for (int i = 0; i < this.gatePosList.size(); i++) {
                nbt.method_10572("GatePos" + i, List.of(this.gatePosList.get(i).method_10263(), this.gatePosList.get(i).method_10264(), this.gatePosList.get(i).method_10260()));
            }
        }
    }

    public static void clientTick(class_1937 world, class_2338 pos, class_2680 state, DungeonPortalEntity blockEntity) {
    }

    public static void serverTick(class_1937 world, class_2338 pos, class_2680 state, DungeonPortalEntity blockEntity) {
        if (blockEntity.getDungeonPlayerCount() > 0) {
            if (blockEntity.autoKickTime == 0) {
                blockEntity.autoKickTime = (int) world.method_8510() + 432000;
            } else if (blockEntity.autoKickTime < (int) world.method_8510()) {
                if (blockEntity.getDungeon() != null) {
                    blockEntity.setCooldownTime(blockEntity.getDungeon().getCooldown() + (int) blockEntity.method_10997().method_8510());
                    for (int i = 0; i < blockEntity.getDungeonPlayerUuids().size(); i++) {
                        class_3222 player = (class_3222) world.method_18470(blockEntity.getDungeonPlayerUuids().get(i));
                        if (DungeonHelper.getCurrentDungeon(player) != null) {
                            DungeonHelper.teleportOutOfDungeon(player);
                            player.method_43496(class_2561.method_43471("text.dungeonz.dungeon_autokick"));
                        }
                    }
                }
                blockEntity.getDungeonPlayerUuids().clear();
                blockEntity.getDeadDungeonPlayerUUIDs().clear();
                blockEntity.autoKickTime = 0;
            }
        } else if (blockEntity.autoKickTime != 0) {
            blockEntity.autoKickTime = 0;
        }
        if (blockEntity.dungeonTeleportCountdown >= 1) {
            if (blockEntity.dungeonTeleportCountdown % 20 == 0) {
                for (int i = 0; i < blockEntity.getWaitingUuids().size(); i++) {
                    if (((class_3218) blockEntity.method_10997()).method_14190(blockEntity.getWaitingUuids().get(i)) != null
                            && ((class_3218) blockEntity.method_10997()).method_14190(blockEntity.getWaitingUuids().get(i)) instanceof class_3222 serverPlayerEntity) {
                        DungeonServerPacket.writeS2CDungeonTeleportCountdown(serverPlayerEntity, blockEntity.dungeonTeleportCountdown);
                    }
                }

            }
            blockEntity.dungeonTeleportCountdown--;

            if (blockEntity.dungeonTeleportCountdown == (ConfigInit.CONFIG.defaultDungeonTeleportCountdown / 2)) {
//                CompletableFuture.runAsync(() -> DungeonPlacementHandler.refreshDungeon(((ServerWorld) blockEntity.getWorld()).getServer(), blockEntity.getWorld().getServer().getWorld(DimensionInit.DUNGEON_WORLD), blockEntity,
//                        blockEntity.getDungeon(), blockEntity.getDifficulty(), blockEntity.getDisableEffects()));
                DungeonPlacementHandler.refreshDungeon(((class_3218) blockEntity.method_10997()).method_8503(), blockEntity.method_10997().method_8503().method_3847(DimensionInit.DUNGEON_WORLD), blockEntity,
                        blockEntity.getDungeon(), blockEntity.getDifficulty());
            }

            if (blockEntity.dungeonTeleportCountdown == 0) {
                for (int i = 0; i < blockEntity.getWaitingUuids().size(); i++) {
                    if (((class_3218) blockEntity.method_10997()).method_14190(blockEntity.getWaitingUuids().get(i)) != null
                            && ((class_3218) blockEntity.method_10997()).method_14190(blockEntity.getWaitingUuids().get(i)) instanceof class_3222 serverPlayerEntity) {
                        DungeonHelper.teleportPlayer(serverPlayerEntity, blockEntity.method_10997().method_8503().method_3847(DimensionInit.DUNGEON_WORLD), blockEntity, blockEntity.method_11016());
                    }
                }
                blockEntity.getWaitingUuids().clear();
            }
        }
    }

    @Override
    public class_2561 method_5476() {
        if (this.getDungeon() != null) {
            return class_2561.method_43471("dungeon." + this.getDungeonType());
        }
        return title;
    }

    @Override
    public class_2487 method_16887(class_7874 registryLookup) {
        return this.method_38244(registryLookup);
    }

    @Override
    public class_1703 createMenu(int syncId, class_1661 playerInventory, class_1657 playerEntity) {
        return new DungeonPortalScreenHandler(syncId, playerInventory, this, class_3914.method_17392(field_11863, field_11867));
    }

    @Override
    public boolean method_11400(class_2350 direction) {
        return true;
    }

    @Override
    public DungeonPortalPacket getScreenOpeningData(class_3222 player) {
        List<String> difficulties = new ArrayList<String>();
        Map<String, List<class_1799>> possibleLoot = new HashMap<>();
        Map<String, List<class_1799>> requiredItemStacks = new HashMap<>();
        Optional<class_2960> backgroundId = Optional.empty();

        int requiredLevel = 0;
        boolean allowRespawn = false;
        boolean keepInventory = false;
        boolean allowPositiveEffects = false;
        boolean allowEnderPearl = false;
        boolean allowElytra = false;
        if (this.getDungeon() instanceof Dungeon dungeon) {
            difficulties = dungeon.getDifficultyList();
            possibleLoot = DungeonHelper.getPossibleLootItemStackMap(dungeon, player.method_5682());
            requiredItemStacks = DungeonHelper.getRequiredItemStackList(dungeon);
            backgroundId = Optional.ofNullable(dungeon.getBackgroundId());
            requiredLevel = dungeon.getRequiredLevel();
            allowEnderPearl = dungeon.isEnderPearlAllowed();
            allowPositiveEffects = dungeon.isPositiveEffectsAllowed();
            allowRespawn = dungeon.isRespawnAllowed();
            keepInventory = dungeon.isKeepInventory();
            allowElytra = dungeon.isElytraAllowed();
        }

        return new DungeonPortalPacket(this.getDungeonType(), this.field_11867, this.getDungeonPlayerUuids(), this.getDeadDungeonPlayerUUIDs(), difficulties, possibleLoot, requiredItemStacks, this.getMaxGroupSize(),
                this.getMinGroupSize(), this.getWaitingUuids().size(), requiredLevel, this.getCooldownTime(), this.getDifficulty(), allowEnderPearl, allowPositiveEffects, allowElytra, allowRespawn, keepInventory, this.getPrivateGroup(), backgroundId);
    }

    public void finishDungeon(class_3218 world, class_2338 pos) {
        List<class_1657> players = world.method_18464(class_4051.method_36625().method_18418(64.0), null, new class_238(pos).method_1009(64.0, 64.0, 64.0));
        for (class_1657 player : players) {
            CriteriaInit.DUNGEON_COMPLETION.trigger((class_3222) player, this.getDungeonType(), this.getDifficulty());
        }
        world.method_8396(null, pos, SoundInit.DUNGEON_COMPLETION_EVENT, class_3419.field_15245, 1.0f, 0.9f + world.method_8409().method_43057() * 0.2f);

        for (int i = 0; i < this.getExitPosList().size(); i++) {
            world.method_8652(this.getExitPosList().get(i), BlockInit.DUNGEON_PORTAL.method_9564(), 3);
        }

        world.method_8652(this.getBossLootBlockPos(), class_2246.field_10034.method_9564(), 3);
        InventoryHelper.fillInventoryWithLoot(world.method_8503(), world, this.getBossLootBlockPos(), this.getDungeon().getDifficultyBossLootTableMap().get(this.getDifficulty()));

        this.setCooldownTime(this.getDungeon().getCooldown() + (int) this.method_10997().method_8510());
        method_5431();
    }

    @Nullable
    public Dungeon getDungeon() {
        return Dungeon.getDungeon(this.dungeonType);
    }

    public void setDungeonType(String dungeonType) {
        this.dungeonType = dungeonType;
    }

    public String getDungeonType() {
        return this.dungeonType;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public String getDifficulty() {
        return this.difficulty;
    }

    public void setDungeonStructureGenerated() {
        this.dungeonStructureGenerated = true;
    }

    public boolean isDungeonStructureGenerated() {
        return this.dungeonStructureGenerated;
    }

    public void joinDungeon(UUID playerUuid) {
        if (!this.dungeonPlayerUuids.contains(playerUuid)) {
            this.dungeonPlayerUuids.add(playerUuid);
        }
    }

    public void leaveDungeon(UUID playerUuid) {
        this.dungeonPlayerUuids.remove(playerUuid);
    }

    public int getDungeonPlayerCount() {
        return this.dungeonPlayerUuids.size();
    }

    public void setDungeonPlayerUuids(List<UUID> dungeonPlayerUuids) {
        this.dungeonPlayerUuids = dungeonPlayerUuids;
    }

    public List<UUID> getDungeonPlayerUuids() {
        return this.dungeonPlayerUuids;
    }

    public void addDeadDungeonPlayerUuids(UUID deadDungeonPlayerUuids) {
        this.deadDungeonPlayerUuids.add(deadDungeonPlayerUuids);
    }

    public void setDeadDungeonPlayerUuids(List<UUID> deadDungeonPlayerUuids) {
        this.deadDungeonPlayerUuids = deadDungeonPlayerUuids;
    }

    public List<UUID> getDeadDungeonPlayerUUIDs() {
        return this.deadDungeonPlayerUuids;
    }

    // Might lead to issues if using "="
    public void setBlockMap(HashMap<Integer, ArrayList<class_2338>> map) {
        this.blockBlockPosMap = map;
    }

    public HashMap<Integer, ArrayList<class_2338>> getBlockMap() {
        return this.blockBlockPosMap;
    }

    public void setCooldownTime(int cooldownTime) {
        this.cooldownTime = cooldownTime;
    }

    public int getCooldownTime() {
        return this.cooldownTime;
    }

    public boolean isOnCooldown(int currentTime) {
        if (this.cooldownTime <= currentTime) {
            return false;
        }
        return true;
    }

    public void setMaxGroupSize(int maxGroupSize) {
        this.maxGroupSize = maxGroupSize;
    }

    public void setMinGroupSize(int minGroupSize) {
        this.minGroupSize = minGroupSize;
    }

    public List<UUID> getWaitingUuids() {
        return this.waitingUuids;
    }

    public void addWaitingUuid(UUID uuid) {
        if (!this.waitingUuids.contains(uuid)) {
            this.waitingUuids.add(uuid);
        }
    }

    public int getMaxGroupSize() {
        return this.maxGroupSize;
    }

    public int getMinGroupSize() {
        return this.minGroupSize;
    }

    public void setPrivateGroup(boolean privateGroup) {
        this.privateGroup = privateGroup;
    }

    public boolean getPrivateGroup() {
        return this.privateGroup;
    }

    public void setBossBlockPos(class_2338 pos) {
        this.bossBlockPos = pos;
    }

    public class_2338 getBossBlockPos() {
        return this.bossBlockPos;
    }

    public void setBossLootBlockPos(class_2338 pos) {
        this.bossLootBlockPos = pos;
    }

    public class_2338 getBossLootBlockPos() {
        return this.bossLootBlockPos;
    }

    public void setChestPosList(List<class_2338> chestPosList) {
        this.chestPosList = chestPosList;
    }

    public List<class_2338> getChestPosList() {
        return this.chestPosList;
    }

    public void setGatePosList(List<class_2338> gatePosList) {
        this.gatePosList = gatePosList;
    }

    public List<class_2338> getGatePosList() {
        return this.gatePosList;
    }

    public void setMovingBlockMap(Map<class_2338, Integer> movingBlockMap) {
        this.movingBlockMap = movingBlockMap;
    }

    public Map<class_2338, Integer> getMovingBlockMap() {
        return this.movingBlockMap;
    }

    public void setPoweredBlockMap(Map<class_2338, Powered> poweredBlockMap) {
        this.poweredBlockMap = poweredBlockMap;
    }

    public Map<class_2338, Powered> getPoweredBlockMap() {
        return this.poweredBlockMap;
    }

    public void setExitPosList(List<class_2338> exitPosList) {
        this.exitPosList = exitPosList;
    }

    public List<class_2338> getExitPosList() {
        return this.exitPosList;
    }

    public void addDungeonEdge(int edgeX, int edgeY, int edgeZ) {
        this.dungeonEdgeList.add(edgeX);
        this.dungeonEdgeList.add(edgeY);
        this.dungeonEdgeList.add(edgeZ);
    }

    public List<Integer> getDungeonEdgeList() {
        return this.dungeonEdgeList;
    }

    public void setSpawnerPosEntityIdMap(HashMap<class_2338, Integer> spawnerPosEntityIdMap) {
        this.spawnerPosEntityIdMap = spawnerPosEntityIdMap;
    }

    public HashMap<class_2338, Integer> getSpawnerPosEntityIdMap() {
        return this.spawnerPosEntityIdMap;
    }

    public void setReplaceBlockIdMap(HashMap<class_2338, Integer> replacePosBlockIdMap) {
        this.replacePosBlockIdMap = replacePosBlockIdMap;
    }

    public void addReplaceBlockId(class_2338 pos, class_2248 block) {
        this.replacePosBlockIdMap.put(pos, class_7923.field_41175.method_10206(block));
    }

    public HashMap<class_2338, Integer> getReplaceBlockIdMap() {
        return this.replacePosBlockIdMap;
    }

    public void startDungeonTeleportCountdown(class_3218 dungeonWorld) {
        this.dungeonTeleportCountdown = ConfigInit.CONFIG.defaultDungeonTeleportCountdown;

        boolean isDungeonStructureGenerated = this.isDungeonStructureGenerated();
        if (!isDungeonStructureGenerated) {
            this.setDungeonStructureGenerated();
            DungeonPlacementHandler.generateDungeonStructure(dungeonWorld, new class_2338(0, 0, 0).method_10069(this.method_11016().method_10263() * 16, 100, this.method_11016().method_10260() * 16), this);
        } else {
            DungeonPlacementHandler.prepareDungeon(dungeonWorld, this);
        }
        this.method_5431();
    }

    public int getdungeonTeleportCountdown() {
        return this.dungeonTeleportCountdown;
    }

    public static class Powered {
        private final int blockId;
        private final boolean powered;
        private final int facing;
        private final int blockFacing;

        public Powered(int blockId, boolean powered, int facing, int blockFacing) {
            this.blockId = blockId;
            this.powered = powered;
            this.facing = facing;
            this.blockFacing = blockFacing;
        }

        public int getBlockId() {
            return blockId;
        }

        public boolean getPowered() {
            return powered;
        }

        // Horizontal facing
        public int getFacing() {
            return facing;
        }

        // Block facing for example: cealing
        // 0 = none, 1 = ("floor"), 2 = WALL("wall"), 3 = CEILING("ceiling");
        public int getBlockFacing() {
            return blockFacing;
        }
    }

}
