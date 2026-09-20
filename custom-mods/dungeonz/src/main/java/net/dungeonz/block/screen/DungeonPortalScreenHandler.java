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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.ContainerLevelAccess;

public class DungeonPortalScreenHandler extends AbstractContainerMenu {

    private final Level world;
    private final ContainerLevelAccess context;
    private final DungeonPortalEntity dungeonPortalEntity;
    private BlockPos pos;

    private List<String> difficulties = new ArrayList<String>();
    private Map<String, List<ItemStack>> possibleLootDifficultyItemStackMap = new HashMap<String, List<ItemStack>>();
    private Map<String, List<ItemStack>> requiredItemStacks = new HashMap<String, List<ItemStack>>();
    private int waitingGroupSize = 0;

    private net.dungeonz.network.packet.DungeonAdmissionPacket admission = net.dungeonz.network.packet.DungeonAdmissionPacket.empty();
    private net.minecraft.server.level.ServerPlayer viewer;

    private int requiredLevel = 0;
    private boolean allowRespawn = false;
    private boolean keepInventory = false;
    private boolean allowPositiveEffects = false;
    private boolean allowEnderPearl = false;
    private boolean allowElytra = false;

    @Nullable
    private Identifier backgroundId = null;

    public DungeonPortalScreenHandler(int syncId, Inventory playerInventory, DungeonPortalPacket packet) {
        this(syncId, playerInventory, new DungeonPortalEntity(packet.blockPos(), playerInventory.player.level().getBlockState(packet.blockPos())), ContainerLevelAccess.NULL);
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
        this.admission = packet.admission();
    }

    public DungeonPortalScreenHandler(int syncId, Inventory playerInventory, DungeonPortalEntity dungeonPortalEntity, ContainerLevelAccess context) {
        super(BlockInit.PORTAL, syncId);
        this.context = context;
        this.world = playerInventory.player.level();
        this.dungeonPortalEntity = dungeonPortalEntity;
        this.pos = dungeonPortalEntity.getBlockPos();

        if (playerInventory.player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            this.viewer = serverPlayer;
            this.admission = net.dungeonz.network.packet.DungeonAdmissionPacket.snapshot(serverPlayer, syncId);
        }
        if (!this.world.isClientSide()) {
            setDifficulties(this.dungeonPortalEntity.getDungeon().getDifficultyList());
            setRequiredItemStacks(DungeonHelper.getRequiredItemStackList(this.dungeonPortalEntity.getDungeon()));
            setPossibleLootItemStacks(DungeonHelper.getPossibleLootItemStackMap(this.dungeonPortalEntity.getDungeon(), this.world.getServer()));
        }
    }

    public net.dungeonz.network.packet.DungeonAdmissionPacket getAdmission() {
        return this.admission;
    }

    public void setAdmission(net.dungeonz.network.packet.DungeonAdmissionPacket admission) {
        this.admission = admission;
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();
        if (this.viewer != null) {
            var snapshot = net.dungeonz.network.packet.DungeonAdmissionPacket.snapshot(this.viewer, this.containerId);
            if (!snapshot.equals(this.admission)) {
                this.admission = snapshot;
                net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(this.viewer, snapshot);
            }
        }
    }

    @Override
    public ItemStack quickMoveStack(Player var1, int var2) {
        return null;
    }

    @Override
    public boolean stillValid(Player player) {
        return this.context.evaluate((world, pos) -> {
            if (!this.world.getBlockState(pos).is(BlockInit.DUNGEON_PORTAL)) {
                return false;
            }
            return player.distanceToSqr((double) pos.getX() + 0.5, (double) pos.getY() + 0.5, (double) pos.getZ() + 0.5) <= 64.0;
        }, true);
    }

    @Nullable
    public Identifier getBackgroundId() {
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

    public Map<String, List<ItemStack>> getPossibleLootDifficultyItemStackMap() {
        return this.possibleLootDifficultyItemStackMap;
    }

    public void setPossibleLootItemStacks(Map<String, List<ItemStack>> possibleLootDifficultyItemStackMap) {
        this.possibleLootDifficultyItemStackMap = possibleLootDifficultyItemStackMap;
    }

    public Map<String, List<ItemStack>> getRequiredItemStacks() {
        return this.requiredItemStacks;
    }

    public void setRequiredItemStacks(Map<String, List<ItemStack>> requiredItemStacks) {
        this.requiredItemStacks = requiredItemStacks;
    }

    public int getWaitingGroupSize() {
        return this.waitingGroupSize;
    }

    public void setWaitingGroupSize(int waitingGroupSize) {
        this.waitingGroupSize = waitingGroupSize;
    }

    public BlockPos getPos() {
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
