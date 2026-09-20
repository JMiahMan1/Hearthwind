package net.dungeonz.block.entity;

import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.dungeonz.block.screen.DungeonPortalScreenHandler;
import net.dungeonz.dungeon.Dungeon;
import net.dungeonz.dungeon.DungeonPlacementHandler;
import net.dungeonz.init.*;
import net.dungeonz.network.DungeonServerPacket;
import net.dungeonz.network.packet.DungeonPortalPacket;
import net.dungeonz.util.DungeonHelper;
import net.dungeonz.util.InventoryHelper;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Block;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.TheEndPortalBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.Map.Entry;

public class DungeonPortalEntity extends TheEndPortalBlockEntity implements ExtendedMenuProvider<DungeonPortalPacket> {

    private Component title = Component.translatable("container.dungeon_portal");
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
    private HashMap<Integer, ArrayList<BlockPos>> blockBlockPosMap = new HashMap<Integer, ArrayList<BlockPos>>();
    private List<BlockPos> chestPosList = new ArrayList<BlockPos>();
    private List<BlockPos> exitPosList = new ArrayList<BlockPos>();
    private List<BlockPos> gatePosList = new ArrayList<BlockPos>();
    private Map<BlockPos, Integer> movingBlockMap = new HashMap<>();
    private Map<BlockPos, Powered> poweredBlockMap = new HashMap<>();
    private BlockPos bossBlockPos = new BlockPos(0, 0, 0);
    private BlockPos bossLootBlockPos = new BlockPos(0, 0, 0);
    private HashMap<BlockPos, Integer> spawnerPosEntityIdMap = new HashMap<BlockPos, Integer>();
    private HashMap<BlockPos, Integer> replacePosBlockIdMap = new HashMap<BlockPos, Integer>();
    private List<Integer> dungeonEdgeList = new ArrayList<Integer>();
    private int dungeonTeleportCountdown = 0;

    public DungeonPortalEntity(BlockPos pos, BlockState state) {
        super(BlockInit.DUNGEON_PORTAL_ENTITY, pos, state);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.dungeonType = input.getStringOr("DungeonType", "");
        this.difficulty = input.getStringOr("Difficulty", "");
        this.dungeonStructureGenerated = input.getBooleanOr("DungeonStructureGenerated", false);
        this.dungeonPlayerUuids.clear();
        for (int i = 0; i < input.getIntOr("DungeonPlayerCount", 0); i++) {
            // 26.x: no ValueInput.getUUID; UUIDs go through UUIDUtil.CODEC.
            input.read("PlayerUUID" + i, UUIDUtil.CODEC).ifPresent(this.dungeonPlayerUuids::add);
        }
        this.deadDungeonPlayerUuids.clear();
        for (int i = 0; i < input.getIntOr("DeadDungeonPlayerCount", 0); i++) {
            input.read("DeadPlayerUUID" + i, UUIDUtil.CODEC).ifPresent(this.deadDungeonPlayerUuids::add);
        }
        this.maxGroupSize = input.getIntOr("MaxGroupSize", 0);
        this.minGroupSize = input.getIntOr("MinGroupSize", 0);
        this.cooldownTime = input.getIntOr("CooldownTime", 0);
        this.autoKickTime = input.getIntOr("AutoKickTime", 0);
        this.privateGroup = input.getBooleanOr("PrivateGroup", false);
        this.blockBlockPosMap.clear();
        if (input.getIntOr("BlockMapSize", 0) > 0) {
            for (int i = 0; i < input.getIntOr("BlockMapSize", 0); i++) {
                ArrayList<BlockPos> posList = new ArrayList<>();
                for (int u = 0; u < input.getIntOr("BlockListSize" + i, 0); u++) {
                    int[] blockPos = input.getIntArray("BlockPos" + i + "" + u).orElse(new int[0]);
                    if (blockPos.length >= 3) {
                        posList.add(new BlockPos(blockPos[0], blockPos[1], blockPos[2]));
                    }
                }
                this.blockBlockPosMap.put(input.getIntOr("BlockId" + i, 0), posList);
            }
        }

        int[] bossPos = input.getIntArray("BossPos").orElse(new int[0]);
        if (bossPos.length >= 3) {
            this.bossBlockPos = new BlockPos(bossPos[0], bossPos[1], bossPos[2]);
        }
        int[] bossLootPos = input.getIntArray("BossLootPos").orElse(new int[0]);
        if (bossLootPos.length >= 3) {
            this.bossLootBlockPos = new BlockPos(bossLootPos[0], bossLootPos[1], bossLootPos[2]);
        }

        if (input.getIntOr("ChestListSize", 0) > 0) {
            this.chestPosList.clear();
            for (int i = 0; i < input.getIntOr("ChestListSize", 0); i++) {
                int[] chestPos = input.getIntArray("ChestPos" + i).orElse(new int[0]);
                if (chestPos.length >= 3) {
                    this.chestPosList.add(new BlockPos(chestPos[0], chestPos[1], chestPos[2]));
                }
            }
        }

        if (input.getIntOr("ExitListSize", 0) > 0) {
            this.exitPosList.clear();
            for (int i = 0; i < input.getIntOr("ExitListSize", 0); i++) {
                int[] exitPos = input.getIntArray("ExitPos" + i).orElse(new int[0]);
                if (exitPos.length >= 3) {
                    this.exitPosList.add(new BlockPos(exitPos[0], exitPos[1], exitPos[2]));
                }
            }
        }

        if (input.getIntOr("SpawnerMapSize", 0) > 0) {
            this.spawnerPosEntityIdMap.clear();
            for (int i = 0; i < input.getIntOr("SpawnerMapSize", 0); i++) {
                int[] spawnerPos = input.getIntArray("SpawnerPos" + i).orElse(new int[0]);
                if (spawnerPos.length >= 4) {
                    this.spawnerPosEntityIdMap.put(new BlockPos(spawnerPos[0], spawnerPos[1], spawnerPos[2]), spawnerPos[3]);
                }
            }
        }

        if (input.getIntOr("ReplacePosSize", 0) > 0) {
            this.replacePosBlockIdMap.clear();
            for (int i = 0; i < input.getIntOr("ReplacePosSize", 0); i++) {
                int[] replacePos = input.getIntArray("ReplacePos" + i).orElse(new int[0]);
                if (replacePos.length >= 4) {
                    this.replacePosBlockIdMap.put(new BlockPos(replacePos[0], replacePos[1], replacePos[2]), replacePos[3]);
                }
            }
        }

        if (input.getIntOr("MovingPosSize", 0) > 0) {
            this.movingBlockMap.clear();
            for (int i = 0; i < input.getIntOr("MovingPosSize", 0); i++) {
                int[] movingPos = input.getIntArray("MovingPos" + i).orElse(new int[0]);
                if (movingPos.length >= 4) {
                    this.movingBlockMap.put(new BlockPos(movingPos[0], movingPos[1], movingPos[2]), movingPos[3]);
                }
            }
        }

        if (input.getIntOr("PoweredPosSize", 0) > 0) {
            this.poweredBlockMap.clear();
            for (int i = 0; i < input.getIntOr("PoweredPosSize", 0); i++) {
                int[] poweredPos = input.getIntArray("PoweredPos" + i).orElse(new int[0]);
                if (poweredPos.length >= 7) {
                    boolean isPowered = poweredPos[4] == 1;
                    this.poweredBlockMap.put(new BlockPos(poweredPos[0], poweredPos[1], poweredPos[2]), new Powered(poweredPos[3], isPowered, poweredPos[5], poweredPos[6]));
                }
            }
        }

        if (input.getIntOr("DungeonEdgeSize", 0) > 0) {
            this.dungeonEdgeList.clear();
            for (int i = 0; i < input.getIntOr("DungeonEdgeSize", 0) / 3; i++) {
                int[] dungeonEdgePos = input.getIntArray("DungeonEdge" + i).orElse(new int[0]);
                if (dungeonEdgePos.length >= 3) {
                    this.dungeonEdgeList.add(dungeonEdgePos[0]);
                    this.dungeonEdgeList.add(dungeonEdgePos[1]);
                    this.dungeonEdgeList.add(dungeonEdgePos[2]);
                }
            }
        }

        if (input.getIntOr("GateListSize", 0) > 0) {
            this.gatePosList.clear();
            for (int i = 0; i < input.getIntOr("GateListSize", 0); i++) {
                int[] gatePos = input.getIntArray("GatePos" + i).orElse(new int[0]);
                if (gatePos.length >= 3) {
                    this.gatePosList.add(new BlockPos(gatePos[0], gatePos[1], gatePos[2]));
                }
            }
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putString("DungeonType", this.dungeonType);
        output.putString("Difficulty", this.difficulty);
        output.putBoolean("DungeonStructureGenerated", this.dungeonStructureGenerated);
        output.putInt("DungeonPlayerCount", this.dungeonPlayerUuids.size());
        for (int i = 0; i < this.dungeonPlayerUuids.size(); i++) {
            output.store("PlayerUUID" + i, UUIDUtil.CODEC, this.dungeonPlayerUuids.get(i));
        }
        output.putInt("DeadDungeonPlayerCount", this.deadDungeonPlayerUuids.size());
        for (int i = 0; i < this.deadDungeonPlayerUuids.size(); i++) {
            output.store("DeadPlayerUUID" + i, UUIDUtil.CODEC, this.deadDungeonPlayerUuids.get(i));
        }
        output.putInt("MaxGroupSize", this.maxGroupSize);
        output.putInt("MinGroupSize", this.minGroupSize);
        output.putInt("CooldownTime", this.cooldownTime);
        output.putInt("AutoKickTime", this.autoKickTime);
        output.putBoolean("PrivateGroup", this.privateGroup);

        output.putInt("BlockMapSize", this.blockBlockPosMap.size());
        if (!this.blockBlockPosMap.isEmpty()) {
            int blockCount = 0;
            for (Entry<Integer, ArrayList<BlockPos>> entry : this.blockBlockPosMap.entrySet()) {
                output.putInt("BlockId" + blockCount, entry.getKey());
                output.putInt("BlockListSize" + blockCount, entry.getValue().size());
                for (int i = 0; i < entry.getValue().size(); i++) {
                    output.putIntArray("BlockPos" + blockCount + "" + i, new int[]{entry.getValue().get(i).getX(), entry.getValue().get(i).getY(), entry.getValue().get(i).getZ()});
                }
                blockCount++;
            }
        }
        output.putIntArray("BossPos", new int[]{this.bossBlockPos.getX(), this.bossBlockPos.getY(), this.bossBlockPos.getZ()});
        output.putIntArray("BossLootPos", new int[]{this.bossLootBlockPos.getX(), this.bossLootBlockPos.getY(), this.bossLootBlockPos.getZ()});

        output.putInt("ChestListSize", this.chestPosList.size());
        if (!this.chestPosList.isEmpty()) {
            for (int i = 0; i < this.chestPosList.size(); i++) {
                output.putIntArray("ChestPos" + i, new int[]{this.chestPosList.get(i).getX(), this.chestPosList.get(i).getY(), this.chestPosList.get(i).getZ()});
            }
        }

        output.putInt("ExitListSize", this.exitPosList.size());
        if (!this.exitPosList.isEmpty()) {
            for (int i = 0; i < this.exitPosList.size(); i++) {
                output.putIntArray("ExitPos" + i, new int[]{this.exitPosList.get(i).getX(), this.exitPosList.get(i).getY(), this.exitPosList.get(i).getZ()});
            }
        }

        output.putInt("SpawnerMapSize", this.spawnerPosEntityIdMap.size());
        if (!this.spawnerPosEntityIdMap.isEmpty()) {
            Iterator<Entry<BlockPos, Integer>> iterator = this.spawnerPosEntityIdMap.entrySet().iterator();
            int count = 0;
            while (iterator.hasNext()) {
                Entry<BlockPos, Integer> entry = iterator.next();
                output.putIntArray("SpawnerPos" + count, new int[]{entry.getKey().getX(), entry.getKey().getY(), entry.getKey().getZ(), entry.getValue()});
                count++;
            }
        }

        output.putInt("ReplacePosSize", this.replacePosBlockIdMap.size());
        if (!this.replacePosBlockIdMap.isEmpty()) {
            Iterator<Entry<BlockPos, Integer>> iterator = this.replacePosBlockIdMap.entrySet().iterator();
            int count = 0;
            while (iterator.hasNext()) {
                Entry<BlockPos, Integer> entry = iterator.next();
                output.putIntArray("ReplacePos" + count, new int[]{entry.getKey().getX(), entry.getKey().getY(), entry.getKey().getZ(), entry.getValue()});
                count++;
            }
        }

        output.putInt("MovingPosSize", this.movingBlockMap.size());
        if (!this.movingBlockMap.isEmpty()) {
            Iterator<Entry<BlockPos, Integer>> iterator = this.movingBlockMap.entrySet().iterator();
            int count = 0;
            while (iterator.hasNext()) {
                Entry<BlockPos, Integer> entry = iterator.next();
                output.putIntArray("MovingPos" + count, new int[]{entry.getKey().getX(), entry.getKey().getY(), entry.getKey().getZ(), entry.getValue()});
                count++;
            }
        }

        output.putInt("PoweredPosSize", this.poweredBlockMap.size());
        if (!this.poweredBlockMap.isEmpty()) {
            Iterator<Entry<BlockPos, Powered>> iterator = this.poweredBlockMap.entrySet().iterator();
            int count = 0;
            while (iterator.hasNext()) {
                Entry<BlockPos, Powered> entry = iterator.next();
                int isPowered = entry.getValue().getPowered() ? 1 : 0;
                output.putIntArray("PoweredPos" + count, new int[]{entry.getKey().getX(), entry.getKey().getY(), entry.getKey().getZ(), entry.getValue().getBlockId(), isPowered, entry.getValue().getFacing(), entry.getValue().getBlockFacing()});
                count++;
            }
        }

        output.putInt("DungeonEdgeSize", this.dungeonEdgeList.size());
        if (!this.dungeonEdgeList.isEmpty()) {
            for (int i = 0; i < this.dungeonEdgeList.size() / 3; i++) {
                output.putIntArray("DungeonEdge" + i, new int[]{this.dungeonEdgeList.get(3 * i), this.dungeonEdgeList.get(1 + 3 * i), this.dungeonEdgeList.get(2 + 3 * i)});
            }
        }

        output.putInt("GateListSize", this.gatePosList.size());
        if (!this.gatePosList.isEmpty()) {
            for (int i = 0; i < this.gatePosList.size(); i++) {
                output.putIntArray("GatePos" + i, new int[]{this.gatePosList.get(i).getX(), this.gatePosList.get(i).getY(), this.gatePosList.get(i).getZ()});
            }
        }
    }

    public static void clientTick(Level world, BlockPos pos, BlockState state, DungeonPortalEntity blockEntity) {
    }

    public static void serverTick(Level world, BlockPos pos, BlockState state, DungeonPortalEntity blockEntity) {
        if (blockEntity.getDungeonPlayerCount() > 0) {
            if (blockEntity.autoKickTime == 0) {
                blockEntity.autoKickTime = (int) world.getGameTime() + 432000;
            } else if (blockEntity.autoKickTime < (int) world.getGameTime()) {
                if (blockEntity.getDungeon() != null) {
                    blockEntity.setCooldownTime(blockEntity.getDungeon().getCooldown() + (int) blockEntity.getLevel().getGameTime());
                    for (int i = 0; i < blockEntity.getDungeonPlayerUuids().size(); i++) {
                        ServerPlayer player = (ServerPlayer) world.getPlayerByUUID(blockEntity.getDungeonPlayerUuids().get(i));
                        if (DungeonHelper.getCurrentDungeon(player) != null) {
                            DungeonHelper.teleportOutOfDungeon(player);
                            player.sendSystemMessage(Component.translatable("text.dungeonz.dungeon_autokick"));
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
                    if (((ServerLevel) blockEntity.getLevel()).getEntity(blockEntity.getWaitingUuids().get(i)) != null
                            && ((ServerLevel) blockEntity.getLevel()).getEntity(blockEntity.getWaitingUuids().get(i)) instanceof ServerPlayer serverPlayerEntity) {
                        DungeonServerPacket.writeS2CDungeonTeleportCountdown(serverPlayerEntity, blockEntity.dungeonTeleportCountdown);
                    }
                }

            }
            blockEntity.dungeonTeleportCountdown--;

            if (blockEntity.dungeonTeleportCountdown == (ConfigInit.CONFIG.defaultDungeonTeleportCountdown / 2)) {
//                CompletableFuture.runAsync(() -> DungeonPlacementHandler.refreshDungeon(((ServerWorld) blockEntity.getWorld()).getServer(), blockEntity.getWorld().getServer().getWorld(DimensionInit.DUNGEON_WORLD), blockEntity,
//                        blockEntity.getDungeon(), blockEntity.getDifficulty(), blockEntity.getDisableEffects()));
                DungeonPlacementHandler.refreshDungeon(((ServerLevel) blockEntity.getLevel()).getServer(), blockEntity.getLevel().getServer().getLevel(DimensionInit.DUNGEON_WORLD), blockEntity,
                        blockEntity.getDungeon(), blockEntity.getDifficulty());
            }

            if (blockEntity.dungeonTeleportCountdown == 0) {
                for (int i = 0; i < blockEntity.getWaitingUuids().size(); i++) {
                    if (((ServerLevel) blockEntity.getLevel()).getEntity(blockEntity.getWaitingUuids().get(i)) != null
                            && ((ServerLevel) blockEntity.getLevel()).getEntity(blockEntity.getWaitingUuids().get(i)) instanceof ServerPlayer serverPlayerEntity) {
                        DungeonHelper.teleportPlayer(serverPlayerEntity, blockEntity.getLevel().getServer().getLevel(DimensionInit.DUNGEON_WORLD), blockEntity, blockEntity.getBlockPos());
                    }
                }
                blockEntity.getWaitingUuids().clear();
            }
        }
    }

    @Override
    public Component getDisplayName() {
        if (this.getDungeon() != null) {
            return Component.translatable("dungeon." + this.getDungeonType());
        }
        return title;
    }

    @Override
    public CompoundTag getUpdateTag(Provider registryLookup) {
        return this.saveWithoutMetadata(registryLookup);
    }

    @Override
    public AbstractContainerMenu createMenu(int syncId, Inventory playerInventory, Player playerEntity) {
        return new DungeonPortalScreenHandler(syncId, playerInventory, this, ContainerLevelAccess.create(level, worldPosition));
    }

    @Override
    public boolean shouldRenderFace(Direction direction) {
        return true;
    }

    @Override
    public DungeonPortalPacket getScreenOpeningData(ServerPlayer player) {
        List<String> difficulties = new ArrayList<String>();
        Map<String, List<ItemStack>> possibleLoot = new HashMap<>();
        Map<String, List<ItemStack>> requiredItemStacks = new HashMap<>();
        Optional<Identifier> backgroundId = Optional.empty();

        int requiredLevel = 0;
        boolean allowRespawn = false;
        boolean keepInventory = false;
        boolean allowPositiveEffects = false;
        boolean allowEnderPearl = false;
        boolean allowElytra = false;
        if (this.getDungeon() instanceof Dungeon dungeon) {
            difficulties = dungeon.getDifficultyList();
            possibleLoot = DungeonHelper.getPossibleLootItemStackMap(dungeon, player.level().getServer());
            requiredItemStacks = DungeonHelper.getRequiredItemStackList(dungeon);
            backgroundId = Optional.ofNullable(dungeon.getBackgroundId());
            requiredLevel = dungeon.getRequiredLevel();
            allowEnderPearl = dungeon.isEnderPearlAllowed();
            allowPositiveEffects = dungeon.isPositiveEffectsAllowed();
            allowRespawn = dungeon.isRespawnAllowed();
            keepInventory = dungeon.isKeepInventory();
            allowElytra = dungeon.isElytraAllowed();
        }

        return new DungeonPortalPacket(this.getDungeonType(), this.worldPosition, this.getDungeonPlayerUuids(), this.getDeadDungeonPlayerUUIDs(), difficulties, possibleLoot, requiredItemStacks, this.getMaxGroupSize(),
                this.getMinGroupSize(), this.getWaitingUuids().size(), requiredLevel, this.getCooldownTime(), this.getDifficulty(), allowEnderPearl, allowPositiveEffects, allowElytra, allowRespawn, keepInventory, this.getPrivateGroup(), backgroundId, net.dungeonz.network.packet.DungeonAdmissionPacket.snapshot(player, 0));
    }

    public void finishDungeon(ServerLevel world, BlockPos pos) {
        List<Player> players = world.getNearbyPlayers(TargetingConditions.forCombat().range(64.0), null, new AABB(pos).inflate(64.0, 64.0, 64.0));
        for (Player player : players) {
            CriteriaInit.DUNGEON_COMPLETION.trigger((ServerPlayer) player, this.getDungeonType(), this.getDifficulty());
        }
        world.playSound(null, pos, SoundInit.DUNGEON_COMPLETION_EVENT, SoundSource.BLOCKS, 1.0f, 0.9f + world.getRandom().nextFloat() * 0.2f);

        for (int i = 0; i < this.getExitPosList().size(); i++) {
            world.setBlock(this.getExitPosList().get(i), BlockInit.DUNGEON_PORTAL.defaultBlockState(), 3);
        }

        world.setBlock(this.getBossLootBlockPos(), Blocks.CHEST.defaultBlockState(), 3);
        InventoryHelper.fillInventoryWithLoot(world.getServer(), world, this.getBossLootBlockPos(), this.getDungeon().getDifficultyBossLootTableMap().get(this.getDifficulty()));

        this.setCooldownTime(this.getDungeon().getCooldown() + (int) this.getLevel().getGameTime());
        setChanged();
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
    public void setBlockMap(HashMap<Integer, ArrayList<BlockPos>> map) {
        this.blockBlockPosMap = map;
    }

    public HashMap<Integer, ArrayList<BlockPos>> getBlockMap() {
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

    public void setBossBlockPos(BlockPos pos) {
        this.bossBlockPos = pos;
    }

    public BlockPos getBossBlockPos() {
        return this.bossBlockPos;
    }

    public void setBossLootBlockPos(BlockPos pos) {
        this.bossLootBlockPos = pos;
    }

    public BlockPos getBossLootBlockPos() {
        return this.bossLootBlockPos;
    }

    public void setChestPosList(List<BlockPos> chestPosList) {
        this.chestPosList = chestPosList;
    }

    public List<BlockPos> getChestPosList() {
        return this.chestPosList;
    }

    public void setGatePosList(List<BlockPos> gatePosList) {
        this.gatePosList = gatePosList;
    }

    public List<BlockPos> getGatePosList() {
        return this.gatePosList;
    }

    public void setMovingBlockMap(Map<BlockPos, Integer> movingBlockMap) {
        this.movingBlockMap = movingBlockMap;
    }

    public Map<BlockPos, Integer> getMovingBlockMap() {
        return this.movingBlockMap;
    }

    public void setPoweredBlockMap(Map<BlockPos, Powered> poweredBlockMap) {
        this.poweredBlockMap = poweredBlockMap;
    }

    public Map<BlockPos, Powered> getPoweredBlockMap() {
        return this.poweredBlockMap;
    }

    public void setExitPosList(List<BlockPos> exitPosList) {
        this.exitPosList = exitPosList;
    }

    public List<BlockPos> getExitPosList() {
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

    public void setSpawnerPosEntityIdMap(HashMap<BlockPos, Integer> spawnerPosEntityIdMap) {
        this.spawnerPosEntityIdMap = spawnerPosEntityIdMap;
    }

    public HashMap<BlockPos, Integer> getSpawnerPosEntityIdMap() {
        return this.spawnerPosEntityIdMap;
    }

    public void setReplaceBlockIdMap(HashMap<BlockPos, Integer> replacePosBlockIdMap) {
        this.replacePosBlockIdMap = replacePosBlockIdMap;
    }

    public void addReplaceBlockId(BlockPos pos, Block block) {
        this.replacePosBlockIdMap.put(pos, BuiltInRegistries.BLOCK.getId(block));
    }

    public HashMap<BlockPos, Integer> getReplaceBlockIdMap() {
        return this.replacePosBlockIdMap;
    }

    public void startDungeonTeleportCountdown(ServerLevel dungeonWorld) {
        this.dungeonTeleportCountdown = ConfigInit.CONFIG.defaultDungeonTeleportCountdown;

        boolean isDungeonStructureGenerated = this.isDungeonStructureGenerated();
        if (!isDungeonStructureGenerated) {
            this.setDungeonStructureGenerated();
            DungeonPlacementHandler.generateDungeonStructure(dungeonWorld, new BlockPos(0, 0, 0).offset(this.getBlockPos().getX() * 16, 100, this.getBlockPos().getZ() * 16), this);
        } else {
            DungeonPlacementHandler.prepareDungeon(dungeonWorld, this);
        }
        this.setChanged();
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
